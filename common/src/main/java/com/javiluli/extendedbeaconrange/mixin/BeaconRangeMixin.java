package com.javiluli.extendedbeaconrange.mixin;

import com.javiluli.extendedbeaconrange.Constants;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BeaconBlockEntity.class)
public class BeaconRangeMixin {
	/**
	 * Multiplica el radio vanilla calculado por el beacon usando el multiplicador comun del mod.
	 *
	 * @param d radio vanilla calculado por Minecraft.
	 * @return radio extendido que usara {@code applyEffects}.
	 */
	@ModifyVariable(method = "applyEffects", at = @At("STORE"), ordinal = 0)
	private static double extendApplyEffects(double d) {
		return d * Constants.BEACON_RANGE_MULTIPLIER;
	}
}
