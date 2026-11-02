package com.javiluli.extendedbeaconrange.mixin.effecthud.client;

import com.javiluli.extendedbeaconrange.feature.effecthud.client.BeaconEffectHud;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntity.class)
public class BeaconEffectTrackingMixin {
	/** Reutiliza los beacons que Minecraft ya actualiza en el cliente. */
	@Inject(method = "tick", at = @At("TAIL"))
	private static void extendedbeaconrange$trackBeacon(Level level, BlockPos pos, BlockState state,
			BeaconBlockEntity beacon, CallbackInfo ci) {
		if (level.isClientSide()) { BeaconEffectHud.track(beacon); }
	}
}
