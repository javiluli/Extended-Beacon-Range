package com.javiluli.extendedbeaconrange.client;

import com.javiluli.extendedbeaconrange.client.overlay.BeaconAreaCollector;
import com.javiluli.extendedbeaconrange.client.overlay.BeaconOverlayToggle;
import com.javiluli.extendedbeaconrange.client.overlay.BeaconPerimeterRenderer;
import com.javiluli.extendedbeaconrange.client.overlay.BeaconRenderEntry;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

/**
 * Punto de entrada del overlay 3D de beacons llamado desde {@code LevelRendererMixin}.
 *
 * <p>
 * Esta clase solo valida contexto, procesa el estado global del overlay y delega el escaneo/render en clases especializadas de
 * {@code client.overlay}. La separacion mantiene estable el punto de integracion con Mixin y evita que una sola clase concentre toda la
 * logica visual.
 * </p>
 */
public final class BeaconAreaRenderer {
	/**
	 * Tipo de renderizado usado por las paredes translucidas del perimetro.
	 *
	 * <p>
	 * Se mantiene junto al punto de entrada del render porque solo este renderer solicita el buffer. El estado replica el enfoque del
	 * proyecto de referencia: shader posicion/color, mezcla translucida, profundidad de lectura activa, sin culling y sin escritura de
	 * profundidad. Con esto las paredes se ocultan tras bloques solidos, pero no tapan otros perimetros transparentes dibujados despues.
	 * </p>
	 */
	private static final RenderType PERIMETER_QUADS = new RenderType("extended_beacon_range_area", DefaultVertexFormat.POSITION_COLOR,
			VertexFormat.Mode.QUADS, 256, false, true, BeaconAreaRenderer::setupPerimeterRenderState,
			BeaconAreaRenderer::clearPerimeterRenderState) {
	};

	private BeaconAreaRenderer() {
	}

	/**
	 * Busca beacons cargados cerca del jugador y renderiza el perimetro de cada uno.
	 *
	 * <p>
	 * Se llama desde {@code LevelRendererMixin} durante el render del nivel. La transformacion se hace relativa a la camara, por lo que el
	 * overlay queda anclado al mundo y no depende de que el jugador este mirando directamente al beacon.
	 * </p>
	 *
	 * @param minecraft instancia de cliente actual.
	 * @param camera camara activa del render del nivel.
	 * @param poseStack pila de transformaciones del frame actual.
	 * @param bufferSource fuente de buffers usada por Minecraft para el render.
	 */
	public static void renderLoadedBeacons(Minecraft minecraft, Camera camera, PoseStack poseStack, MultiBufferSource bufferSource) {
		renderLoadedBeacons(minecraft, camera == null ? null : camera.getPosition(), poseStack, bufferSource);
	}

	/**
	 * Busca beacons cargados cerca del jugador y renderiza el perimetro usando una posicion de camara ya calculada.
	 *
	 * <p>
	 * Minecraft 1.21.3 mueve los overlays tardios a un pass interno que expone la posicion de camara como {@link Vec3}. Esta sobrecarga
	 * evita depender de una firma concreta de {@code Camera} y mantiene el render centralizado en una sola clase.
	 * </p>
	 *
	 * @param minecraft instancia de cliente actual.
	 * @param cameraPos posicion absoluta de la camara.
	 * @param poseStack pila de transformaciones del frame actual.
	 * @param bufferSource fuente de buffers usada por Minecraft para el render.
	 */
	public static void renderLoadedBeacons(Minecraft minecraft, Vec3 cameraPos, PoseStack poseStack, MultiBufferSource bufferSource) {
		if (hasInvalidRenderContext(minecraft, cameraPos, poseStack, bufferSource) || !BeaconOverlayToggle.updateAndHasVisibleBeacons(minecraft)) {
			return;
		}

		ClientLevel level = minecraft.level;
		List<BeaconRenderEntry> beaconsToRender = BeaconAreaCollector.collect(minecraft, cameraPos);
		if (beaconsToRender.isEmpty()) {
			return;
		}

		renderBeaconAreas(level, cameraPos, beaconsToRender, poseStack, bufferSource);
	}

	/**
	 * Comprueba que todas las dependencias del render existan antes de tocar estado de cliente.
	 *
	 * @param minecraft instancia de cliente recibida desde el mixin.
	 * @param camera camara activa.
	 * @param poseStack pila de transformaciones del frame.
	 * @param bufferSource buffers de render del frame.
	 * @return {@code true} si falta algun dato y se debe saltar el render.
	 */
	private static boolean hasInvalidRenderContext(Minecraft minecraft, Vec3 cameraPos, PoseStack poseStack, MultiBufferSource bufferSource) {
		return minecraft == null || minecraft.level == null || minecraft.player == null || cameraPos == null || poseStack == null
				|| bufferSource == null;
	}

	/**
	 * Emite todas las areas juntas para ordenar paredes transparentes entre distintos beacons.
	 *
	 * @param level mundo cliente.
	 * @param cameraPos posicion absoluta de la camara.
	 * @param entries beacons preparados para el frame actual.
	 * @param poseStack pila de transformaciones del frame.
	 * @param bufferSource buffers de render del frame.
	 */
	private static void renderBeaconAreas(ClientLevel level, Vec3 cameraPos, List<BeaconRenderEntry> entries, PoseStack poseStack,
			MultiBufferSource bufferSource) {
		Matrix4f poseMatrix = poseStack.last().pose();
		VertexConsumer faceBuffer = bufferSource.getBuffer(PERIMETER_QUADS);
		BeaconPerimeterRenderer.renderAll(level, entries, poseMatrix, faceBuffer, cameraPos);
	}

	/**
	 * Configura el estado de OpenGL/Minecraft para dibujar quads translucidos del perimetro.
	 */
	private static void setupPerimeterRenderState() {
		RenderSystem.setShader(CoreShaders.POSITION_COLOR);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		RenderSystem.depthMask(false);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.disableCull();
	}

	/**
	 * Restaura las partes del estado de render modificadas por {@link #setupPerimeterRenderState()}.
	 */
	private static void clearPerimeterRenderState() {
		RenderSystem.enableCull();
		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
	}
}
