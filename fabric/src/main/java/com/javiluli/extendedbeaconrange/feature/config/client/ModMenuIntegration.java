package com.javiluli.extendedbeaconrange.feature.config.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Adaptador opcional de Fabric hacia la pantalla comun de configuracion. */
public final class ModMenuIntegration implements ModMenuApi {

	/** Proporciona la misma pantalla que Video Settings. */
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return BeaconConfigScreen::new;
	}
}
