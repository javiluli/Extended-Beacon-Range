package com.javiluli.extendedbeaconrange.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.world.level.block.entity.BeaconBlockEntity;

@Mixin(BeaconBlockEntity.class)
public class BeaconRangeMixin {
	private final static float RANGE_MULTIPLIER = 3.0f;

	@ModifyVariable(method = "applyEffects", at = @At("STORE"), ordinal = 0)
	private static double extendApplyEffects(double d) {
		return d * RANGE_MULTIPLIER;
	}

}