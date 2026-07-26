package com.javiluli.extendedbeaconrange.client.overlay;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.javiluli.extendedbeaconrange.client.overlay.BeaconOverlayColors.OverlayColor;
import org.joml.Matrix4f;

/**
 * Dibuja el perimetro del beacon como cuatro paredes verticales y cuatro esquinas opacas.
 *
 * <p>
 * Cada beacon se renderiza siempre de forma independiente: no hay jerarquia entre areas ni cambios de opacidad al entrar o salir de otro
 * perimetro. Las paredes se ordenan globalmente para mejorar la mezcla de transparencias; desde la base del mundo hasta la parte alta del
 * beacon se mantienen constantes y solo se degradan en los ultimos bloques superiores.
 * </p>
 */
public final class BeaconPerimeterRenderer {
	private static final int NORTH_WALL = 0;
	private static final int EAST_WALL = 1;
	private static final int SOUTH_WALL = 2;
	private static final int WEST_WALL = 3;
	private static final int WALLS_PER_BEACON = 4;
	private static final Comparator<WallSurface> WALL_RENDER_ORDER = Comparator.comparingDouble(WallSurface::distanceToCameraSqr)
			.reversed();

	private BeaconPerimeterRenderer() {
	}

	/**
	 * Renderiza todas las paredes visibles de todos los beacons cargados.
	 *
	 * @param level mundo cliente usado para conocer la altura construible.
	 * @param entries beacons preparados para renderizar.
	 * @param matrix matriz del frame, relativa a la camara.
	 * @param vertexConsumer buffer de quads debug.
	 * @param cameraPos posicion absoluta de la camara.
	 */
	public static void renderAll(ClientLevel level, List<BeaconRenderEntry> entries, Matrix4f matrix, VertexConsumer vertexConsumer,
			Vec3 cameraPos) {
		List<PerimeterGeometry> perimeters = new ArrayList<>(entries.size());
		List<WallSurface> walls = new ArrayList<>(entries.size() * WALLS_PER_BEACON);
		float cameraY = (float) cameraPos.y;
		float minY = level.dimensionType().minY() - cameraY;
		float worldMaxY = level.dimensionType().minY() + level.dimensionType().height() - cameraY;
		for (BeaconRenderEntry entry : entries) {
			PerimeterGeometry geometry = createGeometry(entry, cameraPos, minY, worldMaxY);
			perimeters.add(geometry);
			addWalls(geometry, walls);
		}

		walls.sort(WALL_RENDER_ORDER);
		for (WallSurface wall : walls) {
			renderWall(vertexConsumer, matrix, wall);
		}

		for (PerimeterGeometry geometry : perimeters) {
			renderCorners(vertexConsumer, matrix, geometry);
		}
	}

	/**
	 * Crea los limites de render de un beacon en coordenadas relativas a la camara.
	 *
	 * @param entry beacon preparado para render.
	 * @param cameraPos posicion absoluta de la camara.
	 * @param minY Y minima local ya calculada para la dimension.
	 * @param worldMaxY Y maxima local ya calculada para la dimension.
	 * @return geometria reutilizable para paredes y esquinas.
	 */
	private static PerimeterGeometry createGeometry(BeaconRenderEntry entry, Vec3 cameraPos, float minY, float worldMaxY) {
		BlockPos beaconPos = entry.beacon().getBlockPos();
		float offset = BeaconAreaSettings.FULL_WALL_BOX_OFFSET;
		float beaconLocalX = (float) (beaconPos.getX() - cameraPos.x);
		float beaconLocalZ = (float) (beaconPos.getZ() - cameraPos.z);
		float minX = beaconLocalX - entry.radius() + offset;
		float maxX = beaconLocalX + entry.radius() + 1.0f - offset;
		float minZ = beaconLocalZ - entry.radius() + offset;
		float maxZ = beaconLocalZ + entry.radius() + 1.0f - offset;
		float beaconY = beaconPos.getY() - (float) cameraPos.y;
		float fadeStartY = beaconY + BeaconAreaSettings.FULL_WALL_FADE_START_HEIGHT;
		float maxY = Math.min(beaconY + BeaconAreaSettings.FULL_WALL_HEIGHT, worldMaxY);
		return new PerimeterGeometry(minX, maxX, minY, fadeStartY, maxY, minZ, maxZ, entry.color());
	}

	/**
	 * Agrega las cuatro paredes planas de un perimetro a la lista ordenable del frame.
	 *
	 * @param geometry geometria preparada del beacon.
	 * @param walls lista acumuladora de paredes.
	 */
	private static void addWalls(PerimeterGeometry geometry, List<WallSurface> walls) {
		walls.add(new WallSurface(NORTH_WALL, geometry, getHorizontalDistanceSqr(geometry.centerX(), geometry.minZ())));
		walls.add(new WallSurface(EAST_WALL, geometry, getHorizontalDistanceSqr(geometry.maxX(), geometry.centerZ())));
		walls.add(new WallSurface(SOUTH_WALL, geometry, getHorizontalDistanceSqr(geometry.centerX(), geometry.maxZ())));
		walls.add(new WallSurface(WEST_WALL, geometry, getHorizontalDistanceSqr(geometry.minX(), geometry.centerZ())));
	}

	/**
	 * Dibuja una pared plana del perimetro.
	 *
	 * @param vertexConsumer buffer de quads debug.
	 * @param matrix matriz del frame.
	 * @param wall pared preparada y ordenada.
	 */
	private static void renderWall(VertexConsumer vertexConsumer, Matrix4f matrix, WallSurface wall) {
		PerimeterGeometry geometry = wall.geometry();
		switch (wall.wallIndex()) {
			case NORTH_WALL -> renderWallPlane(vertexConsumer, matrix, geometry, geometry.minX(), geometry.minZ(), geometry.maxX(),
					geometry.minZ());
			case EAST_WALL -> renderWallPlane(vertexConsumer, matrix, geometry, geometry.maxX(), geometry.minZ(), geometry.maxX(),
					geometry.maxZ());
			case SOUTH_WALL -> renderWallPlane(vertexConsumer, matrix, geometry, geometry.maxX(), geometry.maxZ(), geometry.minX(),
					geometry.maxZ());
			case WEST_WALL -> renderWallPlane(vertexConsumer, matrix, geometry, geometry.minX(), geometry.maxZ(), geometry.minX(),
					geometry.minZ());
			default -> throw new IllegalArgumentException("Indice de pared inesperado: " + wall.wallIndex());
		}
	}

	/**
	 * Dibuja una pared en dos tramos: constante hasta el inicio del fade y degradada hasta la altura maxima.
	 *
	 * @param vertexConsumer buffer de quads debug.
	 * @param matrix matriz del frame.
	 * @param geometry geometria del perimetro propietario.
	 * @param x1 X del extremo inicial.
	 * @param z1 Z del extremo inicial.
	 * @param x2 X del extremo final.
	 * @param z2 Z del extremo final.
	 */
	private static void renderWallPlane(VertexConsumer vertexConsumer, Matrix4f matrix, PerimeterGeometry geometry, float x1, float z1,
			float x2, float z2) {
		float wallAlpha = BeaconAreaSettings.FULL_WALL_ALPHA;
		float fadeStartY = Math.min(geometry.fadeStartY(), geometry.maxY());
		if (geometry.minY() < fadeStartY) {
			emitQuad(vertexConsumer, matrix, x1, geometry.minY(), z1, x2, geometry.minY(), z2, x2, fadeStartY, z2, x1, fadeStartY,
					z1, geometry.color(), wallAlpha, wallAlpha);
		}
		if (fadeStartY < geometry.maxY()) {
			emitQuad(vertexConsumer, matrix, x1, fadeStartY, z1, x2, fadeStartY, z2, x2, geometry.maxY(), z2, x1, geometry.maxY(), z1,
					geometry.color(), wallAlpha, 0.0f);
		}
	}

	/**
	 * Dibuja las cuatro esquinas opacas de un perimetro.
	 *
	 * @param vertexConsumer buffer de quads debug.
	 * @param matrix matriz del frame.
	 * @param geometry geometria preparada del beacon.
	 */
	private static void renderCorners(VertexConsumer vertexConsumer, Matrix4f matrix, PerimeterGeometry geometry) {
		renderCorner(vertexConsumer, matrix, geometry.minX(), geometry.minZ(), geometry.minY(), geometry.maxY(), geometry.color());
		renderCorner(vertexConsumer, matrix, geometry.maxX(), geometry.minZ(), geometry.minY(), geometry.maxY(), geometry.color());
		renderCorner(vertexConsumer, matrix, geometry.maxX(), geometry.maxZ(), geometry.minY(), geometry.maxY(), geometry.color());
		renderCorner(vertexConsumer, matrix, geometry.minX(), geometry.maxZ(), geometry.minY(), geometry.maxY(), geometry.color());
	}

	/**
	 * Dibuja una esquina como dos tiras verticales cruzadas.
	 *
	 * @param vertexConsumer buffer de quads debug.
	 * @param matrix matriz del frame.
	 * @param x X local de la esquina.
	 * @param z Z local de la esquina.
	 * @param minY Y minima local.
	 * @param maxY Y maxima local.
	 * @param color color unico asignado al beacon.
	 */
	private static void renderCorner(VertexConsumer vertexConsumer, Matrix4f matrix, float x, float z, float minY, float maxY,
			OverlayColor color) {
		float halfWidth = BeaconAreaSettings.FULL_WALL_CORNER_WIDTH * 0.5f;
		float alpha = BeaconAreaSettings.FULL_WALL_CORNER_ALPHA;
		emitQuad(vertexConsumer, matrix, x - halfWidth, minY, z, x + halfWidth, minY, z, x + halfWidth, maxY, z,
				x - halfWidth, maxY, z, color, alpha, alpha);
		emitQuad(vertexConsumer, matrix, x, minY, z - halfWidth, x, minY, z + halfWidth, x, maxY, z + halfWidth, x, maxY,
				z - halfWidth, color, alpha, alpha);
	}

	/**
	 * Calcula la distancia horizontal desde la camara hasta el centro aproximado de una pared.
	 *
	 * @param wallCenterX X local del centro de pared.
	 * @param wallCenterZ Z local del centro de pared.
	 * @return distancia al cuadrado, sin raiz cuadrada.
	 */
	private static float getHorizontalDistanceSqr(float wallCenterX, float wallCenterZ) {
		return (wallCenterX * wallCenterX) + (wallCenterZ * wallCenterZ);
	}

	/**
	 * Emite un quad con el color y la opacidad indicados.
	 *
	 * @param vertexConsumer buffer de quads debug.
	 * @param matrix matriz del frame.
	 * @param x1 X del primer vertice.
	 * @param y1 Y del primer vertice.
	 * @param z1 Z del primer vertice.
	 * @param x2 X del segundo vertice.
	 * @param y2 Y del segundo vertice.
	 * @param z2 Z del segundo vertice.
	 * @param x3 X del tercer vertice.
	 * @param y3 Y del tercer vertice.
	 * @param z3 Z del tercer vertice.
	 * @param x4 X del cuarto vertice.
	 * @param y4 Y del cuarto vertice.
	 * @param z4 Z del cuarto vertice.
	 * @param color color unico asignado al beacon.
	 * @param bottomAlpha opacidad aplicada a los vertices inferiores.
	 * @param topAlpha opacidad aplicada a los vertices superiores.
	 */
	private static void emitQuad(VertexConsumer vertexConsumer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2,
			float z2, float x3, float y3, float z3, float x4, float y4, float z4, OverlayColor color, float bottomAlpha,
			float topAlpha) {
		emitVertex(vertexConsumer, matrix, x1, y1, z1, color, bottomAlpha);
		emitVertex(vertexConsumer, matrix, x2, y2, z2, color, bottomAlpha);
		emitVertex(vertexConsumer, matrix, x3, y3, z3, color, topAlpha);
		emitVertex(vertexConsumer, matrix, x4, y4, z4, color, topAlpha);
	}

	/**
	 * Emite un vertice coloreado en el buffer debug.
	 *
	 * @param vertexConsumer buffer de quads debug.
	 * @param matrix matriz del frame.
	 * @param x X local del vertice.
	 * @param y Y local del vertice.
	 * @param z Z local del vertice.
	 * @param color color unico asignado al beacon.
	 * @param alpha opacidad aplicada al vertice.
	 */
	private static void emitVertex(VertexConsumer vertexConsumer, Matrix4f matrix, float x, float y, float z, OverlayColor color,
			float alpha) {
		vertexConsumer.addVertex(matrix, x, y, z).setColor(color.red(), color.green(), color.blue(), alpha);
	}

	/**
	 * Geometria de un perimetro ya convertida a coordenadas relativas a la camara.
	 *
	 * @param minX X minima local.
	 * @param maxX X maxima local.
	 * @param minY Y minima local.
	 * @param fadeStartY Y local donde empieza el tramo de desvanecido superior.
	 * @param maxY Y maxima local.
	 * @param minZ Z minima local.
	 * @param maxZ Z maxima local.
	 * @param color color unico asignado al beacon.
	 */
	private record PerimeterGeometry(float minX, float maxX, float minY, float fadeStartY, float maxY, float minZ, float maxZ,
			OverlayColor color) {
		private float centerX() {
			return (minX + maxX) * 0.5f;
		}

		private float centerZ() {
			return (minZ + maxZ) * 0.5f;
		}
	}

	/**
	 * Pared preparada para ordenacion global de transparencias.
	 *
	 * @param wallIndex indice del lado: norte, este, sur u oeste.
	 * @param geometry geometria del perimetro propietario.
	 * @param distanceToCameraSqr distancia aproximada de la pared a la camara.
	 */
	private record WallSurface(int wallIndex, PerimeterGeometry geometry, float distanceToCameraSqr) {
	}
}
