package com.javiluli.extendedbeaconrange.mixin.effecthud;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeaconMenu.class)
public class BeaconEffectSyncMixin {
	@Shadow @Final private ContainerLevelAccess access;

	/** Sincroniza las potencias elegidas con el paquete vanilla de la entidad de bloque. */
	@Inject(method = "updateEffects", at = @At("RETURN"))
	private void extendedbeaconrange$syncBeaconPowers(Optional<Holder<MobEffect>> primary,
			Optional<Holder<MobEffect>> secondary, CallbackInfoReturnable<Boolean> ci) {
		if (ci.getReturnValue()) {
			access.execute((level, pos) -> {
				var state = level.getBlockState(pos);
				level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
			});
		}
	}
}
