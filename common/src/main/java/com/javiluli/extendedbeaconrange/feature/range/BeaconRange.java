package com.javiluli.extendedbeaconrange.feature.range;

import com.javiluli.extendedbeaconrange.Constants;

/** Regla de alcance compartida por los efectos del servidor y su visualizacion. */
public final class BeaconRange {
	/** Marca de red opcional; no se guarda en los datos del mundo. */
	public static final String SYNC_TAG = Constants.MOD_ID + ":extended_range";
	private static final double MULTIPLIER = 3.0D;
	private BeaconRange() { }

	/** Aplica el multiplicador al radio vanilla sin depender del cliente. */
	public static double extend(double vanillaRadius) {
		return vanillaRadius * MULTIPLIER;
	}

	/** Devuelve el radio visual de una piramide activa, o cero si no tiene niveles. */
	public static int effectRadius(int levels) {
		return effectRadius(levels, true);
	}

	/** Usa el radio vanilla si el servidor no anuncia el alcance extendido. */
	public static int effectRadius(int levels, boolean extended) {
		int vanilla = levels <= 0 ? 0 : (levels + 1) * 10;
		return extended ? (int) Math.round(extend(vanilla)) : vanilla;
	}

	/** Expone el alcance recibido sin depender de clases de cliente en el servidor. */
	public interface Source {
		/** Indica si el servidor anuncio alcance extendido para este beacon. */
		boolean extendedbeaconrange$usesExtendedRange();
	}
}
