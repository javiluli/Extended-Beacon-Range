package com.javiluli.extendedbeaconrange.feature.perimeter.client;

import java.util.stream.IntStream;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jspecify.annotations.Nullable;

/**
 * Usa el color nativo del rayo o una paleta estable por posicion X/Z.
 */
public final class BeaconOverlayColors {
	private static final OverlayColor WHITE = new OverlayColor(1.0f, 1.0f, 1.0f);
	private static final OverlayColor[] PALETTE = IntStream.range(0, BeaconAreaSettings.POSITION_COLOR_BUCKETS)
			.mapToObj(bucket -> hsvToRgb(bucket / (float) BeaconAreaSettings.POSITION_COLOR_BUCKETS,
					BeaconAreaSettings.POSITION_COLOR_SATURATION, BeaconAreaSettings.POSITION_COLOR_VALUE))
			.toArray(OverlayColor[]::new);

	private BeaconOverlayColors() {
	}

	/** El blanco tiene prioridad; reutiliza el color anterior si el tramo superior del rayo no cambia. */
	public static OverlayColor getColor(BeaconBlockEntity beacon, boolean colored, boolean matchBeam, @Nullable OverlayColor previous) {
		if (!colored || !matchBeam) {
			return getPositionColor(beacon.getBlockPos().getX(), beacon.getBlockPos().getZ(), colored);
		}
		var sections = beacon.getBeamSections();
		if (sections.isEmpty()) { return WHITE; }
		int color = sections.getLast().getColor();
		float red = ARGB.redFloat(color), green = ARGB.greenFloat(color), blue = ARGB.blueFloat(color);
		return previous != null && previous.red() == red && previous.green() == green && previous.blue() == blue
				? previous : new OverlayColor(red, green, blue);
	}

	/**
	 * Calcula un color intenso a partir de la posicion del beacon.
	 *
	 * @param worldX X absoluta del beacon usada como semilla visual.
	 * @param worldZ Z absoluta del beacon usada como semilla visual.
	 * @param colored si es false, devuelve blanco para todos los beacons.
	 * @return color RGB normalizado.
	 */
	public static OverlayColor getPositionColor(int worldX, int worldZ, boolean colored) {
		if (!colored) {
			return WHITE;
		}
		int hash = mixPositionHash(worldX, worldZ);
		int bucket = Math.floorMod(hash * BeaconAreaSettings.POSITION_COLOR_DISTINCTION_STEP,
				BeaconAreaSettings.POSITION_COLOR_BUCKETS);
		return PALETTE[bucket];
	}

	/**
	 * Mezcla X/Z en un hash estable con buena dispersion para posiciones cercanas.
	 *
	 * @param worldX X absoluta.
	 * @param worldZ Z absoluta.
	 * @return hash entero estable.
	 */
	private static int mixPositionHash(int worldX, int worldZ) {
		int hash = worldX * 0x1f1f1f1f;
		hash ^= worldZ * 0x45d9f3b;
		hash ^= hash >>> 16;
		hash *= 0x7feb352d;
		hash ^= hash >>> 15;
		hash *= 0x846ca68b;
		hash ^= hash >>> 16;
		return hash;
	}

	/**
	 * Convierte un color HSV a RGB sin depender de clases externas.
	 *
	 * @param hue tono normalizado entre {@code 0} y {@code 1}.
	 * @param saturation saturacion normalizada entre {@code 0} y {@code 1}.
	 * @param value brillo normalizado entre {@code 0} y {@code 1}.
	 * @return color RGB normalizado.
	 */
	private static OverlayColor hsvToRgb(float hue, float saturation, float value) {
		float scaledHue = hue * 6.0f;
		int sector = (int) Math.floor(scaledHue);
		float fraction = scaledHue - sector;
		float p = value * (1.0f - saturation);
		float q = value * (1.0f - (saturation * fraction));
		float t = value * (1.0f - (saturation * (1.0f - fraction)));

		return switch (Math.floorMod(sector, 6)) {
			case 0 -> new OverlayColor(value, t, p);
			case 1 -> new OverlayColor(q, value, p);
			case 2 -> new OverlayColor(p, value, t);
			case 3 -> new OverlayColor(p, q, value);
			case 4 -> new OverlayColor(t, p, value);
			default -> new OverlayColor(value, p, q);
		};
	}

	/**
	 * Color RGB normalizado usado por el overlay del perimetro.
	 *
	 * @param red canal rojo entre {@code 0} y {@code 1}.
	 * @param green canal verde entre {@code 0} y {@code 1}.
	 * @param blue canal azul entre {@code 0} y {@code 1}.
	 */
	public record OverlayColor(float red, float green, float blue) {
	}
}
