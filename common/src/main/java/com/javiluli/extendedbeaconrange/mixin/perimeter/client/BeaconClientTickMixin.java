package com.javiluli.extendedbeaconrange.mixin.perimeter.client;

import com.javiluli.extendedbeaconrange.feature.perimeter.client.BeaconOverlayToggle;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class BeaconClientTickMixin {
	/** Actualiza seleccion y deteccion en ticks, independientemente de los FPS. */
	@Inject(method = "tick", at = @At("TAIL"))
	private void extendedbeaconrange$updatePerimeters(CallbackInfo ci) {
		BeaconOverlayToggle.tick((Minecraft) (Object) this);
	}
}
