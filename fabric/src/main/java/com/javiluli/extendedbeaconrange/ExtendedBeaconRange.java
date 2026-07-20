package com.javiluli.extendedbeaconrange;

import net.fabricmc.api.ModInitializer;

/**
 * Punto de entrada de Fabric.
 *
 * <p>
 * La logica del mod vive en el modulo comun mediante mixins y clases client-side; este entrypoint existe para que Fabric registre el mod.
 * </p>
 */
public class ExtendedBeaconRange implements ModInitializer {
	/**
	 * Inicializa el entrypoint de Fabric. Actualmente no requiere registro adicional.
	 */
	@Override
	public void onInitialize() {
	}
}
