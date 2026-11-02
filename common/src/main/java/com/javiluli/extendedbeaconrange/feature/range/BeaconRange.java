package com.javiluli.extendedbeaconrange.feature.range;

/** Regla de alcance compartida por los efectos del servidor y su visualizacion. */
public final class BeaconRange {
	private static final double MULTIPLIER = 3.0D;
	private BeaconRange() { }

	/** Aplica el multiplicador al radio vanilla sin depender del cliente. */
	public static double extend(double vanillaRadius) {
		return vanillaRadius * MULTIPLIER;
	}

	/** Devuelve el radio visual de una piramide activa, o cero si no tiene niveles. */
	public static int effectRadius(int levels) {
		return levels <= 0 ? 0 : (int) Math.round(extend((levels + 1) * 10.0));
	}
}
