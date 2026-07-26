package com.javiluli.extendedbeaconrange.client;

import com.javiluli.extendedbeaconrange.client.overlay.BeaconAreaCollector;
import com.javiluli.extendedbeaconrange.client.overlay.BeaconOverlayToggle;
import com.javiluli.extendedbeaconrange.client.overlay.BeaconPerimeterRenderer;
import com.javiluli.extendedbeaconrange.client.overlay.BeaconRenderEntry;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;

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
	 * Minecraft 26.2 mueve la geometria del nivel al sistema de {@code SubmitNodeCollector}. Usamos el tipo vanilla de debug quads porque
	 * mantiene posicion/color, mezcla translucida, ordenacion en subida y profundidad normal contra bloques solidos.
	 * </p>
	 */
	private static final RenderType PERIMETER_QUADS = RenderTypes.debugQuads();

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
	 * @param cameraPos posicion absoluta de la camara.
	 * @param submitNodeCollector collector del render del nivel donde se registra la geometria.
	 */
	public static void submitLoadedBeacons(Minecraft minecraft, Vec3 cameraPos, SubmitNodeCollector submitNodeCollector) {
		if (hasInvalidRenderContext(minecraft, cameraPos, submitNodeCollector) || !BeaconOverlayToggle.updateAndHasVisibleBeacons(minecraft)) {
			return;
		}

		ClientLevel level = minecraft.level;
		List<BeaconRenderEntry> beaconsToRender = BeaconAreaCollector.collect(minecraft, cameraPos);
		if (beaconsToRender.isEmpty()) {
			return;
		}

		submitBeaconAreas(level, cameraPos, beaconsToRender, submitNodeCollector);
	}

	/**
	 * Comprueba que todas las dependencias del render existan antes de tocar estado de cliente.
	 *
	 * @param minecraft instancia de cliente recibida desde el mixin.
	 * @param cameraPos posicion absoluta de la camara.
	 * @param submitNodeCollector collector del render del nivel.
	 * @return {@code true} si falta algun dato y se debe saltar el render.
	 */
	private static boolean hasInvalidRenderContext(Minecraft minecraft, Vec3 cameraPos, SubmitNodeCollector submitNodeCollector) {
		return minecraft == null || minecraft.level == null || minecraft.player == null || cameraPos == null || submitNodeCollector == null;
	}

	/**
	 * Registra todas las areas juntas despues del terreno translucido.
	 *
	 * <p>
	 * {@code afterTerrain} mantiene el orden mas estable que expone el collector para geometria personalizada y conserva el depth test del
	 * {@link RenderType}, asi que los bloques solidos siguen ocultando el perimetro. Si otro loader cambia el collector interno, se usa el
	 * submit normal como fallback para no romper el render.
	 * </p>
	 *
	 * @param level mundo cliente.
	 * @param cameraPos posicion absoluta de la camara.
	 * @param entries beacons preparados para el frame actual.
	 * @param submitNodeCollector collector del render del nivel.
	 */
	private static void submitBeaconAreas(ClientLevel level, Vec3 cameraPos, List<BeaconRenderEntry> entries,
			SubmitNodeCollector submitNodeCollector) {
		PoseStack poseStack = new PoseStack();
		SubmitNodeCollector.CustomGeometryRenderer geometryRenderer = (pose, vertexConsumer) -> BeaconPerimeterRenderer.renderAll(level,
				entries, pose.pose(), vertexConsumer, cameraPos);

		if (submitNodeCollector instanceof SubmitNodeStorage submitNodeStorage) {
			submitNodeStorage.order(0).afterTerrain
					.submit(new CustomFeatureRenderer.Submit(poseStack.last().copy(), PERIMETER_QUADS, geometryRenderer));
			return;
		}

		submitNodeCollector.submitCustomGeometry(poseStack, PERIMETER_QUADS, geometryRenderer);
	}

}
