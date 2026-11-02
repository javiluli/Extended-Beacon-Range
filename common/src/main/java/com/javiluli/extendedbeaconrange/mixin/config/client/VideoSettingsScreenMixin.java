package com.javiluli.extendedbeaconrange.mixin.config.client;

import com.javiluli.extendedbeaconrange.feature.config.client.BeaconConfigScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VideoSettingsScreen.class)
public abstract class VideoSettingsScreenMixin extends OptionsSubScreen {

	protected VideoSettingsScreenMixin(Screen parent, Options options, Component title) {
		super(parent, options, title);
	}

	@Inject(method = "addOptions", at = @At("TAIL"))
	private void extendedbeaconrange$addVideoSettings(CallbackInfo ci) {
		if (list != null) {
			list.addHeader(Component.translatable("config.extendedbeaconrange.section")
					.withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD));
			BeaconConfigScreen.addOptionsTo(list, this);
		}
	}
}
