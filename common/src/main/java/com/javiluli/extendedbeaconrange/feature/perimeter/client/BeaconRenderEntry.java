package com.javiluli.extendedbeaconrange.feature.perimeter.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import com.javiluli.extendedbeaconrange.feature.perimeter.client.BeaconOverlayColors.OverlayColor;

/**
 * Datos inmutables sin referencias a chunks; limites completos incluyendo esquinas.
 *
 * @param position posicion absoluta del beacon.
 * @param radius radio efectivo ya calculado.
 * @param color color del rayo, de la paleta por posicion o blanco, segun las opciones del cliente.
 * @param bounds volumen de descarte desde la Y minima de la dimension.
 */
public record BeaconRenderEntry(BlockPos position, int radius, OverlayColor color, AABB bounds) {
	/** Prepara el volumen real desde el fondo del mundo hasta el final del degradado. */
	public static BeaconRenderEntry create(BlockPos position, int radius, OverlayColor color, int minY, int maxY) {
		double margin = BeaconAreaSettings.FULL_WALL_CORNER_WIDTH * 0.5 - BeaconAreaSettings.FULL_WALL_BOX_OFFSET;
		AABB bounds = new AABB(position.getX() - radius - margin, minY, position.getZ() - radius - margin,
				position.getX() + radius + 1 + margin, Math.min(position.getY() + BeaconAreaSettings.FULL_WALL_HEIGHT, maxY),
				position.getZ() + radius + 1 + margin);
		return new BeaconRenderEntry(position.immutable(), radius, color, bounds);
	}
}
