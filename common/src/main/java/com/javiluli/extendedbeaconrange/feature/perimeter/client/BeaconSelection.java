package com.javiluli.extendedbeaconrange.feature.perimeter.client;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;

/** Seleccion independiente del render, comprobable sin arrancar el cliente. */
final class BeaconSelection {
	final Set<BlockPos> positions = new HashSet<>();
	boolean global;

	/** Alterna una posicion o el modo global; global sigue activo aunque no haya beacons. */
	boolean toggle(BlockPos target) {
		if (target == null || global) {
			boolean enable = target == null && positions.isEmpty() && !global;
			clear();
			global = enable;
			return enable;
		}
		BlockPos position = target.immutable();
		if (positions.remove(position)) { return false; }
		positions.add(position);
		return true;
	}

	/** Libera la seleccion al salir del mundo o cambiar de dimension. */
	void clear() {
		positions.clear();
		global = false;
	}
}
