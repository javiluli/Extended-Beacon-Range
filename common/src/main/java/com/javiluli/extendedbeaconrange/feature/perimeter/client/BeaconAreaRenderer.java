package com.javiluli.extendedbeaconrange.feature.perimeter.client;

import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/** Descarta volumenes completos y envia geometria sin realizar deteccion desde el render. */
public final class BeaconAreaRenderer {
	private static final RenderType PERIMETER_QUADS = RenderTypes.debugQuads();
	private static final PoseStack POSE = new PoseStack();
	private static final List<BeaconRenderEntry> visible = new ArrayList<>();
	private BeaconAreaRenderer() { }

	/** Usa el frustum vanilla y los limites reales, incluidas paredes bajo el beacon. */
	public static void submitLoadedBeacons(Minecraft minecraft, CameraRenderState camera, SubmitNodeCollector collector) {
		if (minecraft == null || minecraft.level == null || minecraft.player == null || camera == null || collector == null) { return; }
		visible.clear();
		List<BeaconRenderEntry> entries = BeaconAreaCollector.entries();
		for (BeaconRenderEntry entry : entries) {
			if (camera.cullFrustum.isVisible(entry.bounds())) { visible.add(entry); }
		}
		if (!visible.isEmpty()) { submit(camera, collector); }
	}

	/** Conserva la fase afterTerrain y el depth test originales. El callback se consume en este frame. */
	private static void submit(CameraRenderState camera, SubmitNodeCollector collector) {
		SubmitNodeCollector.CustomGeometryRenderer geometry = (pose, vertices) ->
				BeaconPerimeterRenderer.renderAll(visible, pose.pose(), vertices, camera.pos);
		if (collector instanceof SubmitNodeStorage storage) {
			storage.order(0).afterTerrain.submit(new CustomFeatureRenderer.Submit(POSE.last().copy(), PERIMETER_QUADS, geometry));
		} else {
			collector.submitCustomGeometry(POSE, PERIMETER_QUADS, geometry);
		}
	}
}
