package com.javiluli.extendedbeaconrange.mixin.client;

import com.javiluli.extendedbeaconrange.client.BeaconAreaRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Conecta el renderer del nivel vanilla con el overlay client-side de beacons.
 *
 * <p>
 * Se inyecta al final de {@code renderDebug} porque en ese punto el render del mundo ya tiene una {@link PoseStack}, una camara y un
 * {@link MultiBufferSource} listos para dibujar geometria simple anclada al mundo.
 * </p>
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	/**
	 * Renderiza automaticamente las areas de todos los beacons cargados.
	 *
	 * @param poseStack pila de transformaciones del render del nivel.
	 * @param bufferSource buffers activos del render.
	 * @param camera camara actual del jugador.
	 * @param ci informacion del callback de Mixin.
	 */
	@Inject(method = "renderDebug", at = @At("TAIL"))
	private void extendedbeaconrange$renderBeaconAreas(PoseStack poseStack, MultiBufferSource bufferSource, Camera camera, CallbackInfo ci) {
		BeaconAreaRenderer.renderLoadedBeacons(Minecraft.getInstance(), camera, poseStack, bufferSource);
	}
}
