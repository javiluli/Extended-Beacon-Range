package com.javiluli.extendedbeaconrange.feature.config.client;

import com.javiluli.extendedbeaconrange.Constants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Registra el acceso a la configuracion desde la lista de mods de NeoForge. */
@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public final class ExtendedBeaconRangeClient {

	/** Conecta el menu de NeoForge con la pantalla comun solo en el cliente fisico. */
	public ExtendedBeaconRangeClient(ModContainer container) {
		container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> new BeaconConfigScreen(parent));
	}
}
