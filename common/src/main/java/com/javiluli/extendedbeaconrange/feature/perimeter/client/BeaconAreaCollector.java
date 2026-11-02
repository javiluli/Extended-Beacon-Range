package com.javiluli.extendedbeaconrange.feature.perimeter.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.javiluli.extendedbeaconrange.feature.range.BeaconRange;
import com.javiluli.extendedbeaconrange.mixin.perimeter.client.BeaconBlockEntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/** Actualiza en ticks y entrega al render un snapshot estable hasta el siguiente cambio. */
public final class BeaconAreaCollector {
	private static final Map<BlockPos, BeaconRenderEntry> prepared = new HashMap<>();
	private static final Set<Long> dirtyChunks = new HashSet<>();
	private static List<BeaconRenderEntry> snapshot = List.of();
	private static ClientLevel level;
	private static boolean wasGlobal;
	private BeaconAreaCollector() { }

	/** Libera los datos del mundo anterior. */
	static void reset(ClientLevel newLevel) {
		level = newLevel;
		wasGlobal = false;
		prepared.clear();
		dirtyChunks.clear();
		snapshot = List.of();
	}

	/** Invalida solo el chunk que recibio datos o cambios de un beacon. */
	public static void chunkChanged(ClientLevel source, int x, int z) {
		if (source == level && BeaconOverlayToggle.isGlobal()) {
			dirtyChunks.add(((long) x << 32) | (z & 0xffffffffL));
		}
	}

	/** Obtiene los datos preparados sin consultar chunks desde el render. */
	public static List<BeaconRenderEntry> entries() { return snapshot; }

	/** Invalida el inicio global incluso si se alterna varias veces dentro del mismo tick. */
	static void requestGlobalDiscovery() { wasGlobal = false; }

	/** Consulta selecciones una vez por tick; solo descubre chunks al activarse o invalidarse. */
	static void update(Minecraft minecraft, BeaconSelection selection) {
		if (selection.global) {
			if (!wasGlobal) {
				int cx = minecraft.player.blockPosition().getX() >> 4;
				int cz = minecraft.player.blockPosition().getZ() >> 4;
				// El cache vanilla conserva hasta tres chunks extra sobre la distancia de vista.
				int radius = minecraft.options.getEffectiveRenderDistance() + 3;
				for (int x = cx - radius; x <= cx + radius; x++) {
					for (int z = cz - radius; z <= cz + radius; z++) { discover(x, z, selection.positions); }
				}
			} else {
				for (long chunk : dirtyChunks) { discover((int) (chunk >> 32), (int) chunk, selection.positions); }
			}
		}
		dirtyChunks.clear();
		wasGlobal = selection.global;
		if (selection.positions.isEmpty()) {
			prepared.clear();
			snapshot = List.of();
			return;
		}
		boolean changed = prepared.keySet().removeIf(pos -> !selection.positions.contains(pos));
		var iterator = selection.positions.iterator();
		boolean colored = true;
		int minY = level.dimensionType().minY();
		int maxY = minY + level.dimensionType().height();
		while (iterator.hasNext()) {
			BlockPos pos = iterator.next();
			LevelChunk chunk = loadedChunk(pos.getX() >> 4, pos.getZ() >> 4);
			BlockEntity entity = chunk == null ? null : chunk.getBlockEntities().get(pos);
			if (!(entity instanceof BeaconBlockEntity beacon)) {
				if (chunk != null || selection.global) { iterator.remove(); }
				changed |= prepared.remove(pos) != null;
				continue;
			}
			int radius = BeaconRange.effectRadius(((BeaconBlockEntityAccessor) beacon).extendedbeaconrange$getLevels());
			if (radius <= 0) {
				changed |= prepared.remove(pos) != null;
				continue;
			}
			var color = BeaconOverlayColors.getPositionColor(pos.getX(), pos.getZ(), colored);
			BeaconRenderEntry old = prepared.get(pos);
			if (old == null || old.radius() != radius || !old.color().equals(color)) {
				prepared.put(pos, BeaconRenderEntry.create(pos, radius, color, minY, maxY));
				changed = true;
			}
		}
		if (changed) { snapshot = List.copyOf(prepared.values()); }
	}

	/** Incluye beacons sin piramide para descubrir su activacion posterior sin otro escaneo. */
	private static void discover(int x, int z, Set<BlockPos> positions) {
		LevelChunk chunk = loadedChunk(x, z);
		if (chunk != null) {
			for (BlockEntity entity : chunk.getBlockEntities().values()) {
				if (entity instanceof BeaconBlockEntity) { positions.add(entity.getBlockPos().immutable()); }
			}
		}
	}

	/** false garantiza que ninguna consulta genera o carga chunks. */
	private static LevelChunk loadedChunk(int x, int z) {
		return level.getChunkSource().getChunk(x, z, ChunkStatus.FULL, false);
	}

	/** Lee el nivel efectivo sin repetir la formula de radios. */
	public static boolean hasEffectiveRange(BeaconBlockEntity beacon) {
		return ((BeaconBlockEntityAccessor) beacon).extendedbeaconrange$getLevels() > 0;
	}
}
