package com.javiluli.extendedbeaconrange.mixin.client;

import com.javiluli.extendedbeaconrange.client.BeaconAreaRenderer;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Conecta el renderer del nivel vanilla con el overlay client-side de beacons.
 *
 * <p>
 * En Minecraft 1.21.3 los overlays tardios se dibujan dentro del pass {@code late_debug}. Envolvemos el runnable registrado por vanilla
 * para dibujar despues de sus overlays, cuando el target principal esta activo y se puede emitir geometria simple anclada al mundo.
 * </p>
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	/**
	 * Envuelve el runnable vanilla del pass late_debug para dibujar el overlay justo despues.
	 *
	 * @param framePass pass de render registrado por vanilla.
	 * @param vanillaRender accion original del pass late_debug.
	 * @param frameGraph grafo de render recibido por vanilla.
	 * @param cameraPos posicion absoluta de la camara.
	 * @param gpuBufferSlice uniforms de render preparados por vanilla para el pass.
	 */
	@Redirect(method = "addLateDebugPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/world/phys/Vec3;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FramePass;executes(Ljava/lang/Runnable;)V"))
	private void extendedbeaconrange$renderBeaconAreas(FramePass framePass, Runnable vanillaRender, FrameGraphBuilder frameGraph,
			Vec3 cameraPos, GpuBufferSlice gpuBufferSlice) {
		framePass.executes(() -> {
			vanillaRender.run();
			renderBeaconAreas(cameraPos);
		});
	}

	/**
	 * Dibuja el overlay en el buffer principal del cliente y fuerza el vaciado del batch.
	 *
	 * @param cameraPos posicion absoluta de la camara.
	 */
	private static void renderBeaconAreas(Vec3 cameraPos) {
		Minecraft minecraft = Minecraft.getInstance();
		PoseStack poseStack = new PoseStack();
		MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
		BeaconAreaRenderer.renderLoadedBeacons(minecraft, cameraPos, poseStack, bufferSource);
		bufferSource.endBatch();
	}
}
