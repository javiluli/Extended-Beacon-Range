package com.javiluli.extendedbeaconrange.mixin.range;

import com.javiluli.extendedbeaconrange.feature.range.BeaconRange;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeaconBlockEntity.class)
public class BeaconRangeMixin implements BeaconRange.Source {
	@Unique private boolean extendedbeaconrange$extendedRange;

	/** Anuncia el alcance con el paquete vanilla, sin exigir el mod a otros clientes. */
	@Inject(method = "getUpdateTag", at = @At("RETURN"))
	private void extendedbeaconrange$announceRange(HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> ci) {
		var level = ((BeaconBlockEntity) (Object) this).getLevel();
		if (level != null && !level.isClientSide()) {
			ci.getReturnValue().putBoolean(BeaconRange.SYNC_TAG, true);
		}
	}

	/** La ausencia de la marca conserva la compatibilidad con servidores vanilla o anteriores. */
	@Inject(method = "loadAdditional", at = @At("TAIL"))
	private void extendedbeaconrange$readRange(ValueInput input, CallbackInfo ci) {
		extendedbeaconrange$extendedRange = input.getBooleanOr(BeaconRange.SYNC_TAG, false);
	}

	@Override
	public boolean extendedbeaconrange$usesExtendedRange() { return extendedbeaconrange$extendedRange; }

	/**
	 * Multiplica el radio vanilla calculado por el beacon usando el multiplicador comun del mod.
	 *
	 * @param d radio vanilla calculado por Minecraft.
	 * @return radio extendido que usara {@code applyEffects}.
	 */
	@ModifyVariable(method = "applyEffects", at = @At("STORE"), ordinal = 0)
	private static double extendApplyEffects(double d) {
		return BeaconRange.extend(d);
	}
}
