package com.javiluli.extendedbeaconrange.feature.perimeter.client;

import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Renderiza paredes con fade y postes opacos; reutiliza sus descriptores entre frames. */
public final class BeaconPerimeterRenderer {
	private static final List<Geometry> geometries = new ArrayList<>();
	private BeaconPerimeterRenderer() { }

	/** Reutiliza los descriptores existentes y sustituye solo las entradas que cambian. */
	public static void renderAll(List<BeaconRenderEntry> entries, Matrix4f matrix, VertexConsumer vertices, Vec3 camera) {
		while (geometries.size() > entries.size()) { geometries.removeLast(); }
		// debugQuads ordena todos los quads al subirlos; OIT usa sus etapas sin esta ordenacion.
		for (int i = 0; i < entries.size(); i++) {
			BeaconRenderEntry entry = entries.get(i);
			Geometry g = i < geometries.size() ? geometries.get(i) : null;
			if (g == null || g.entry != entry) {
				g = new Geometry(entry);
				if (i < geometries.size()) { geometries.set(i, g); }
				else { geometries.add(g); }
			}
			g.update(camera);
			wall(vertices, matrix, g, g.minX, g.minZ, g.maxX, g.minZ);
			wall(vertices, matrix, g, g.maxX, g.minZ, g.maxX, g.maxZ);
			wall(vertices, matrix, g, g.maxX, g.maxZ, g.minX, g.maxZ);
			wall(vertices, matrix, g, g.minX, g.maxZ, g.minX, g.minZ);
			corner(vertices, matrix, g, g.minX, g.minZ);
			corner(vertices, matrix, g, g.maxX, g.minZ);
			corner(vertices, matrix, g, g.maxX, g.maxZ);
			corner(vertices, matrix, g, g.minX, g.maxZ);
		}
	}

	/** Emite una pared constante bajo el beacon y una segunda con alpha decreciente. */
	private static void wall(VertexConsumer v, Matrix4f m, Geometry g, float x1, float z1, float x2, float z2) {
		if (g.fadeY > g.minY) { verticalQuad(v, m, x1, z1, x2, z2, g.minY, g.fadeY, g.wallColor, g.wallColor); }
		if (g.maxY > g.fadeY) { verticalQuad(v, m, x1, z1, x2, z2, g.fadeY, g.maxY, g.wallColor, g.fadeColor); }
	}

	/** Cierra cada esquina con un poste de seis caras, del mismo color opaco que el beacon. */
	private static void corner(VertexConsumer v, Matrix4f m, Geometry g, float x, float z) {
		float half = BeaconAreaSettings.FULL_WALL_CORNER_WIDTH * 0.5f;
		float minX = x-half, maxX = x+half, minZ = z-half, maxZ = z+half;
		int color = g.cornerColor;
		verticalQuad(v, m, maxX, maxZ, maxX, minZ, g.minY, g.maxY, color, color);
		verticalQuad(v, m, minX, minZ, minX, maxZ, g.minY, g.maxY, color, color);
		verticalQuad(v, m, maxX, minZ, minX, minZ, g.minY, g.maxY, color, color);
		verticalQuad(v, m, minX, maxZ, maxX, maxZ, g.minY, g.maxY, color, color);
		quad(v, m, minX, g.maxY, minZ, minX, g.maxY, maxZ, maxX, g.maxY, maxZ, maxX, g.maxY, minZ, color, color);
		quad(v, m, minX, g.minY, minZ, maxX, g.minY, minZ, maxX, g.minY, maxZ, minX, g.minY, maxZ, color, color);
	}

	/** Comparte la emision de caras verticales entre paredes y postes. */
	private static void verticalQuad(VertexConsumer v, Matrix4f m, float x1, float z1, float x2, float z2,
			float minY, float maxY, int bottom, int top) {
		quad(v, m, x1, minY, z1, x2, minY, z2, x2, maxY, z2, x1, maxY, z1, bottom, top);
	}

	/** Envia los colores ARGB preparados sin convertir canales por cada vertice. */
	private static void quad(VertexConsumer v, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2,
			float x3, float y3, float z3, float x4, float y4, float z4, int bottom, int top) {
		v.addVertex(m, x1, y1, z1).setColor(bottom);
		v.addVertex(m, x2, y2, z2).setColor(bottom);
		v.addVertex(m, x3, y3, z3).setColor(top);
		v.addVertex(m, x4, y4, z4).setColor(top);
	}

	/** Descriptores reutilizados: solo cambia su posicion relativa a la camara. */
	private static final class Geometry {
		final BeaconRenderEntry entry;
		final int wallColor, fadeColor, cornerColor;
		final double fadeStartY;
		float minX, maxX, minZ, maxZ, minY, fadeY, maxY;
		/** Prepara colores y corte del fade solo cuando cambian los datos del beacon. */
		Geometry(BeaconRenderEntry entry) {
			this.entry = entry;
			var color = entry.color();
			int rgb = ARGB.colorFromFloat(1, color.red(), color.green(), color.blue());
			wallColor = ARGB.color(BeaconAreaSettings.FULL_WALL_ALPHA, rgb);
			fadeColor = ARGB.color(0, rgb);
			cornerColor = ARGB.color(BeaconAreaSettings.FULL_WALL_CORNER_ALPHA, rgb);
			fadeStartY = Math.clamp(entry.position().getY() + (double) BeaconAreaSettings.FULL_WALL_FADE_START_HEIGHT,
					entry.bounds().minY, entry.bounds().maxY);
		}
		/** Ajusta coordenadas sin regenerar la geometria del beacon. */
		void update(Vec3 camera) {
			float offset = BeaconAreaSettings.FULL_WALL_BOX_OFFSET;
			minX = (float) (entry.position().getX() - camera.x) - entry.radius() + offset;
			maxX = (float) (entry.position().getX() - camera.x) + entry.radius() + 1 - offset;
			minZ = (float) (entry.position().getZ() - camera.z) - entry.radius() + offset;
			maxZ = (float) (entry.position().getZ() - camera.z) + entry.radius() + 1 - offset;
			minY = (float) (entry.bounds().minY - camera.y);
			fadeY = (float) (fadeStartY - camera.y);
			maxY = (float) (entry.bounds().maxY - camera.y);
		}
	}

}
