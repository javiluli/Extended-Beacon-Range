package com.javiluli.extendedbeaconrange.mixin.client;

import com.javiluli.extendedbeaconrange.client.overlay.BeaconOverlayToggle;

import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Registra el keybind configurable antes de que Minecraft lea {@code options.txt}.
 *
 * <p>
 * Si el keybind se registra solo al entrar en un mundo, no aparece en Controles desde el menu principal y Minecraft tampoco puede cargar
 * una tecla personalizada guardada. Inyectar al inicio de {@link Options#load()} mantiene el comportamiento vanilla de ajustes.
 * </p>
 */
@Mixin(Options.class)
public class OptionsLoadMixin {
	/**
	 * Anade el keybind del perimetro antes de procesar las opciones guardadas.
	 *
	 * @param ci informacion del callback de Mixin.
	 */
	@Inject(method = "load", at = @At("HEAD"))
	private void extendedbeaconrange$registerBeaconOverlayKey(CallbackInfo ci) {
		BeaconOverlayToggle.registerKeyMapping((Options) (Object) this);
	}
}
