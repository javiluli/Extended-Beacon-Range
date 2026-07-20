package com.javiluli.extendedbeaconrange;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * Punto de entrada de NeoForge.
 *
 * <p>
 * La funcionalidad compartida se carga desde {@code common}; esta clase mantiene el registro minimo requerido por NeoForge.
 * </p>
 */
@Mod(Constants.MOD_ID)
public class ExtendedBeaconRange {

	/**
	 * Inicializa el entrypoint de NeoForge. Actualmente no requiere registro adicional.
	 *
	 * @param eventBus bus de eventos del mod entregado por NeoForge.
	 */
	public ExtendedBeaconRange(IEventBus eventBus) {
	}
}
