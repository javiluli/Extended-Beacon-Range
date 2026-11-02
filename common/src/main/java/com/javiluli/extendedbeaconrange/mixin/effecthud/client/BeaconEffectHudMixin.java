package com.javiluli.extendedbeaconrange.mixin.effecthud.client;

import com.javiluli.extendedbeaconrange.feature.effecthud.client.BeaconEffectHud;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Hud.class)
public class BeaconEffectHudMixin {
	/** Reutiliza el borde azulado, conservando la colocacion nativa de cada icono. */
	@WrapOperation(method = "extractEffects", at = @At(value = "INVOKE", target =
			"Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"), require = 2)
	private void extendedbeaconrange$effectBorder(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite,
			int x, int y, int width, int height, Operation<Void> original, @Local MobEffectInstance effect,
			@Local(argsOnly = true) DeltaTracker deltaTracker) {
		boolean supported = BeaconEffectHud.supports(effect);
		original.call(graphics, pipeline, supported ? BeaconEffectHud.BACKGROUND : sprite, x, y, width, height);
		if (supported) {
			BeaconEffectHud.extractBorder(graphics, effect, x, y, deltaTracker.getGameTimeDeltaPartialTick(false));
		}
	}

	/** Anade texto tras el icono sin reemplazar su renderizado vanilla. */
	@WrapOperation(method = "extractEffects", at = @At(value = "INVOKE", target =
			"Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V"))
	private void extendedbeaconrange$effectLabels(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite,
			int x, int y, int width, int height, int color, Operation<Void> original, @Local MobEffectInstance effect) {
		original.call(graphics, pipeline, sprite, x, y, width, height, color);
		if (BeaconEffectHud.supports(effect)) { BeaconEffectHud.extractLabels(graphics, effect, x - 3, y - 3); }
	}

	/** Deja sitio bajo la primera fila solo cuando esta feature muestra contadores. */
	@ModifyVariable(method = "extractEffects", at = @At("STORE"), ordinal = 3)
	private int extendedbeaconrange$effectRowSpacing(int y, @Local MobEffectInstance effect) {
		return !effect.getEffect().value().isBeneficial() && BeaconEffectHud.needsTimerSpace() ? y + 8 : y;
	}
}
