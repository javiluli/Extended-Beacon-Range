package com.javiluli.extendedbeaconrange.mixin.client;

import com.javiluli.extendedbeaconrange.feature.effecthud.client.BeaconEffectHud;
import com.javiluli.extendedbeaconrange.feature.perimeter.client.BeaconOverlayToggle;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class ClientTickMixin {
	/** Actualiza las features de cliente en ticks, independientemente de los FPS. */
	@Inject(method = "tick", at = @At("TAIL"))
	private void extendedbeaconrange$updateFeatures(CallbackInfo ci) {
		BeaconOverlayToggle.tick((Minecraft) (Object) this);
		BeaconEffectHud.tick((Minecraft) (Object) this);
	}
}
