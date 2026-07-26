package com.javiluli.extendedbeaconrange.mixin.client;

import com.javiluli.extendedbeaconrange.client.BeaconAreaRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Conecta el renderer del nivel vanilla con el overlay client-side de beacons.
 *
 * <p>
 * Minecraft 26.2 envia la geometria del nivel a {@code SubmitNodeCollector}. Inyectamos el overlay antes de cerrar los gizmos vanilla para
 * que las paredes del beacon entren en la misma fase translucida que el resto de geometria personalizada.
 * </p>
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	/**
	 * Registra el overlay del beacon en el collector del frame actual.
	 *
	 * @param levelRenderState estado de render del nivel, incluida la posicion de camara.
	 * @param submitNodeCollector collector donde Minecraft agrupa la geometria.
	 * @param renderOutline indica si vanilla debe enviar el outline del bloque mirado.
	 * @param callbackInfo control del callback Mixin.
	 */
	@Inject(method = "submitFeatures", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;finalizeGizmoCollection()V"))
	private void extendedbeaconrange$submitBeaconAreas(LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector,
			boolean renderOutline, CallbackInfo callbackInfo) {
		BeaconAreaRenderer.submitLoadedBeacons(Minecraft.getInstance(), levelRenderState.cameraRenderState.pos, submitNodeCollector);
	}
}
