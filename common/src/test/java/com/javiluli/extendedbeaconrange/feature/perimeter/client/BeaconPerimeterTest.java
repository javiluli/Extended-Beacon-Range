package com.javiluli.extendedbeaconrange.feature.perimeter.client;

import static org.junit.jupiter.api.Assertions.*;
import com.javiluli.extendedbeaconrange.feature.perimeter.client.BeaconOverlayColors.OverlayColor;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

class BeaconPerimeterTest {
	private static final OverlayColor COLOR = new OverlayColor(1, 0, 0);

	@Test
	void inactiveOverlayDoesNotReadTheWorld() {
		BeaconAreaCollector.reset(null);
		assertDoesNotThrow(() -> BeaconAreaCollector.update(null, new BeaconSelection()));
		assertTrue(BeaconAreaCollector.entries().isEmpty());
	}

	@Test
	void emptyGlobalModeRemainsEnabledForFutureBeacons() {
		BeaconSelection selection = new BeaconSelection();
		assertTrue(selection.toggle(null));
		assertTrue(selection.global);
		assertTrue(selection.positions.isEmpty());
		selection.positions.add(new BlockPos(8, 64, 8));
		assertTrue(selection.global);
		assertFalse(selection.toggle(null));
		assertFalse(selection.global);
		assertTrue(selection.positions.isEmpty());
	}

	@Test
	void individualGlobalAndDimensionTransitionsClearCorrectly() {
		BeaconSelection selection = new BeaconSelection();
		BlockPos a = new BlockPos(-16, 64, 16);
		BlockPos b = new BlockPos(16, -30, -16);
		assertTrue(selection.toggle(a));
		assertTrue(selection.toggle(b));
		assertFalse(selection.toggle(a));
		assertEquals(java.util.Set.of(b), selection.positions);
		assertFalse(selection.toggle(null));
		assertTrue(selection.positions.isEmpty());
		assertTrue(selection.toggle(null));
		assertFalse(selection.toggle(a));
		assertFalse(selection.global);
		selection.toggle(a);
		selection.clear();
		assertTrue(selection.positions.isEmpty());
		assertFalse(selection.global);
	}

	@Test
	void boundsIncludeDeepWallsAndOpaqueCornerWidth() {
		var entry = BeaconRenderEntry.create(new BlockPos(0, 200, 0), 150, COLOR, -64, 320);
		assertEquals(-64, entry.bounds().minY);
		assertEquals(204, entry.bounds().maxY);
		assertTrue(entry.bounds().minX < -150);
		assertTrue(entry.bounds().maxX > 151);
		assertTrue(entry.bounds().contains(0, -60, 0));
	}

	@Test
	void boundsClampToActualDimensionHeight() {
		var entry = BeaconRenderEntry.create(new BlockPos(0, 254, 0), 60, COLOR, 0, 256);
		assertEquals(0, entry.bounds().minY);
		assertEquals(256, entry.bounds().maxY);
		var negative = BeaconRenderEntry.create(new BlockPos(0, -100, 0), 60, COLOR, -256, 0);
		assertEquals(-256, negative.bounds().minY);
		assertEquals(-96, negative.bounds().maxY);
	}

	@Test
	void cullingUsesWholeVolumeRatherThanElevatedBeacon() {
		Frustum frustum = new Frustum(new Matrix4f(), new Matrix4f().ortho(-10, 10, -10, 10, -100, 100));
		frustum.prepare(0, 0, 0);
		var entry = BeaconRenderEntry.create(new BlockPos(0, 200, 0), 60, COLOR, -64, 320);
		assertFalse(frustum.pointInFrustum(0, 200, 0));
		assertTrue(frustum.isVisible(entry.bounds()));
		var away = BeaconRenderEntry.create(new BlockPos(1000, 200, 1000), 60, COLOR, -64, 320);
		assertFalse(frustum.isVisible(away.bounds()));
	}
}
