package com.javiluli.extendedbeaconrange.feature.config.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

/** Pantalla vanilla compartida por Mod Menu y los menus de Forge y NeoForge. */
public final class BeaconConfigScreen extends OptionsSubScreen {

	/** Abre las opciones del mod y conserva la pantalla a la que debe regresar. */
	public BeaconConfigScreen(Screen parent) {
		super(parent, Minecraft.getInstance().options, Component.translatable("config.extendedbeaconrange.title"));
	}

	@Override
	protected void addOptions() {
		if (list != null) {
			addOptionsTo(list, this);
		}
	}

	/** Reutiliza las mismas opciones en esta pantalla y en Video Settings. */
	public static void addOptionsTo(OptionsList list, Screen parent) {
		BeaconClientConfig config = BeaconClientConfig.get();
		list.addBig(OptionInstance.createBoolean("config.extendedbeaconrange.colorVisualPerimeter",
				OptionInstance.cachedConstantTooltip(Component.translatable("config.extendedbeaconrange.colorVisualPerimeter.tooltip")),
				config.colorVisualPerimeter, value -> {
					config.colorVisualPerimeter = value;
					if (!config.save()) {
						Minecraft minecraft = Minecraft.getInstance();
						minecraft.gui.setScreen(new AlertScreen(() -> minecraft.gui.setScreen(parent),
								Component.translatable("config.extendedbeaconrange.save_failed"),
								Component.translatable("config.extendedbeaconrange.save_failed.description")));
					}
				}));
	}
}
