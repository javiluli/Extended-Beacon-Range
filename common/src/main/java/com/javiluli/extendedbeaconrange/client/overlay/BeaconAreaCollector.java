package com.javiluli.extendedbeaconrange.client.overlay;

import com.javiluli.extendedbeaconrange.Constants;
import com.javiluli.extendedbeaconrange.mixin.client.BeaconBlockEntityAccessor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;

/**
 * Localiza beacons cargados alrededor del jugador y prepara sus datos de render.
 */
public final class BeaconAreaCollector {
	private BeaconAreaCollector() {
	}

	/**
	 * Recoge beacons en chunks cargados dentro de la distancia de render del cliente.
	 *
	 * @param minecraft instancia de cliente actual.
	 * @param cameraPos posicion absoluta de la camara.
	 * @return lista ordenada de beacons de lejos a cerca para renderizar transparencias con menos artefactos.
	 */
	public static List<BeaconRenderEntry> collect(Minecraft minecraft, Vec3 cameraPos) {
		List<BeaconRenderEntry> beaconsToRender = new ArrayList<>();
		forEachLoadedBeacon(minecraft, beacon -> addRenderableBeacon(cameraPos, beaconsToRender, beacon));

		if (beaconsToRender.size() > 1) {
			beaconsToRender.sort(Comparator.comparingDouble(BeaconRenderEntry::distanceToCameraSqr).reversed());
		}

		return beaconsToRender;
	}

	/**
	 * Anade a una seleccion todos los beacons cargados alrededor del jugador.
	 *
	 * @param minecraft instancia de cliente actual.
	 * @param selectedBeacons posiciones seleccionadas a ampliar.
	 * @return cantidad de beacons nuevos anadidos.
	 */
	public static int addLoadedBeaconPositions(Minecraft minecraft, Set<BlockPos> selectedBeacons) {
		int initialSize = selectedBeacons.size();
		forEachLoadedBeacon(minecraft, beacon -> {
			if (hasEffectiveRange(beacon)) {
				selectedBeacons.add(beacon.getBlockPos().immutable());
			}
		});
		return selectedBeacons.size() - initialSize;
	}

	/**
	 * Recorre todos los beacons cargados cerca del jugador sin forzar la carga de chunks.
	 *
	 * @param minecraft instancia de cliente actual.
	 * @param consumer accion a ejecutar por cada beacon cargado.
	 */
	private static void forEachLoadedBeacon(Minecraft minecraft, LoadedBeaconConsumer consumer) {
		ClientChunkCache chunkCache = minecraft.level.getChunkSource();
		BlockPos playerPos = minecraft.player.blockPosition();
		int centerChunkX = playerPos.getX() >> BeaconAreaSettings.CHUNK_COORDINATE_SHIFT;
		int centerChunkZ = playerPos.getZ() >> BeaconAreaSettings.CHUNK_COORDINATE_SHIFT;
		int renderDistance = minecraft.options.getEffectiveRenderDistance() + BeaconAreaSettings.RENDER_DISTANCE_PADDING_CHUNKS;

		for (int chunkX = centerChunkX - renderDistance; chunkX <= centerChunkX + renderDistance; ++chunkX) {
			for (int chunkZ = centerChunkZ - renderDistance; chunkZ <= centerChunkZ + renderDistance; ++chunkZ) {
				visitBeaconsInChunk(chunkCache, consumer, chunkX, chunkZ);
			}
		}
	}

	/**
	 * Recorre las entidades de bloque de un chunk cargado.
	 *
	 * @param chunkCache cache de chunks cliente.
	 * @param consumer accion a ejecutar por cada beacon encontrado.
	 * @param chunkX coordenada X del chunk.
	 * @param chunkZ coordenada Z del chunk.
	 */
	private static void visitBeaconsInChunk(ClientChunkCache chunkCache, LoadedBeaconConsumer consumer, int chunkX, int chunkZ) {
		LevelChunk chunk = chunkCache.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
		if (chunk == null) {
			return;
		}

		for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
			if (blockEntity instanceof BeaconBlockEntity beacon) {
				consumer.accept(beacon);
			}
		}
	}

	/**
	 * Calcula radio/color/distancia de un beacon y lo agrega si tiene area efectiva.
	 *
	 * @param cameraPos posicion absoluta de la camara.
	 * @param beaconsToRender lista acumuladora del frame.
	 * @param beacon beacon detectado en un chunk cargado.
	 */
	private static void addRenderableBeacon(Vec3 cameraPos, List<BeaconRenderEntry> beaconsToRender, BeaconBlockEntity beacon) {
		int levels = ((BeaconBlockEntityAccessor) beacon).extendedbeaconrange$getLevels();
		int radius = getEffectRadius(levels);
		if (radius <= 0) {
			return;
		}

		BlockPos beaconPos = beacon.getBlockPos();
		if (!BeaconOverlayToggle.isBeaconVisible(beaconPos)) {
			return;
		}

		beaconsToRender.add(new BeaconRenderEntry(beacon, radius, BeaconOverlayColors.getPositionColor(beaconPos.getX(), beaconPos.getZ()),
				getDistanceToCameraSqr(beaconPos, cameraPos)));
	}

	/**
	 * Comprueba si el beacon tiene una piramide valida y, por tanto, area real de efecto.
	 *
	 * @param beacon beacon cargado en cliente.
	 * @return {@code true} si su nivel vanilla produce radio efectivo.
	 */
	public static boolean hasEffectiveRange(BeaconBlockEntity beacon) {
		return getEffectRadius(((BeaconBlockEntityAccessor) beacon).extendedbeaconrange$getLevels()) > 0;
	}

	/**
	 * Convierte el nivel interno del beacon en el radio efectivo que el overlay debe mostrar.
	 *
	 * <p>
	 * La formula replica el multiplicador usado por {@code BeaconRangeMixin} para que el area visual coincida con el efecto real aplicado
	 * a los jugadores.
	 * </p>
	 *
	 * @param levels niveles detectados por el beacon vanilla.
	 * @return radio efectivo, en bloques.
	 */
	private static int getEffectRadius(int levels) {
		if (levels <= 0) {
			return 0;
		}

		return (int) Math.max(0L, Math.round((levels + 1) * 10.0D * Constants.BEACON_RANGE_MULTIPLIER));
	}

	/**
	 * Calcula distancia al centro del beacon para ordenar overlays lejanos antes que cercanos.
	 *
	 * @param pos posicion absoluta del beacon.
	 * @param cameraPos posicion absoluta de la camara.
	 * @return distancia al cuadrado.
	 */
	private static double getDistanceToCameraSqr(BlockPos pos, Vec3 cameraPos) {
		double dx = (pos.getX() + 0.5D) - cameraPos.x;
		double dy = (pos.getY() + 0.5D) - cameraPos.y;
		double dz = (pos.getZ() + 0.5D) - cameraPos.z;
		return (dx * dx) + (dy * dy) + (dz * dz);
	}

	/**
	 * Accion ligera para procesar beacons cargados sin crear listas intermedias.
	 */
	@FunctionalInterface
	private interface LoadedBeaconConsumer {
		void accept(BeaconBlockEntity beacon);
	}
}
