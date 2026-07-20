package com.javiluli.extendedbeaconrange.client.overlay;

/**
 * Genera colores intensos y estables para diferenciar beacons cercanos por posicion X/Z.
 */
public final class BeaconOverlayColors {
	private BeaconOverlayColors() {
	}

	/**
	 * Calcula un color intenso a partir de la posicion del beacon.
	 *
	 * @param worldX X absoluta del beacon usada como semilla visual.
	 * @param worldZ Z absoluta del beacon usada como semilla visual.
	 * @return color RGB normalizado.
	 */
	public static OverlayColor getPositionColor(int worldX, int worldZ) {
		int hash = mixPositionHash(worldX, worldZ);
		int bucket = Math.floorMod(hash * BeaconAreaSettings.POSITION_COLOR_DISTINCTION_STEP,
				BeaconAreaSettings.POSITION_COLOR_BUCKETS);
		float hue = bucket / (float) BeaconAreaSettings.POSITION_COLOR_BUCKETS;
		return hsvToRgb(hue, BeaconAreaSettings.POSITION_COLOR_SATURATION, BeaconAreaSettings.POSITION_COLOR_VALUE);
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
