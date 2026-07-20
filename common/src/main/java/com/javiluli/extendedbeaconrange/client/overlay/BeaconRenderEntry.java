package com.javiluli.extendedbeaconrange.client.overlay;

import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import com.javiluli.extendedbeaconrange.client.overlay.BeaconOverlayColors.OverlayColor;

/**
 * Beacon preparado para renderizar en orden estable de transparencia.
 *
 * @param beacon entidad de bloque beacon.
 * @param radius radio efectivo ya calculado.
 * @param color color unico calculado desde su posicion X/Z.
 * @param distanceToCameraSqr distancia al cuadrado respecto a la camara.
 */
public record BeaconRenderEntry(BeaconBlockEntity beacon, int radius, OverlayColor color, double distanceToCameraSqr) {
}
