package com.javiluli.extendedbeaconrange;

import com.javiluli.extendedbeaconrange.feature.config.client.BeaconConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Punto de entrada de Forge.
 *
 * <p>
 * La funcionalidad compartida se carga desde {@code common}; esta clase mantiene el registro minimo requerido por Forge.
 * </p>
 */
@Mod(Constants.MOD_ID)
public class ExtendedBeaconRange {

	/**
	 * Registra la pantalla de configuracion solo en el cliente fisico.
	 */
	public ExtendedBeaconRange(FMLJavaModLoadingContext context) {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			ClientConfig.register(context);
		}
	}

	private static final class ClientConfig {
		private static void register(FMLJavaModLoadingContext context) {
			context.registerExtensionPoint(ConfigScreenFactory.class,
					() -> new ConfigScreenFactory(BeaconConfigScreen::new));
		}
	}
}
