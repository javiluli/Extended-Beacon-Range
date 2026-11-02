package com.javiluli.extendedbeaconrange.feature.perimeter.client;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBeamOwner;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BeaconActivityTest {

	@BeforeAll
	static void bootstrapMinecraft() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@ParameterizedTest
	@ValueSource(ints = {1, 2, 3, 4})
	void blockedBeamHidesOverlayUntilTheBeamReturns(int pyramidLevels) throws ReflectiveOperationException {
		TestBeacon beacon = new TestBeacon(pyramidLevels);
		var sections = beacon.getBeamSections();
		sections.add(new BeaconBeamOwner.Section(0xffffffff));
		assertTrue(BeaconAreaCollector.hasEffectiveRange(beacon));

		sections.clear();
		assertEquals(pyramidLevels, beacon.extendedbeaconrange$getLevels());
		assertFalse(BeaconAreaCollector.hasEffectiveRange(beacon), "A blocked beam must hide the perimeter even if levels remain unchanged");

		sections.add(new BeaconBeamOwner.Section(0xffffffff));
		assertTrue(BeaconAreaCollector.hasEffectiveRange(beacon));
	}

	@Test
	void beaconWithoutAPyramidHasNoEffectiveRange() throws ReflectiveOperationException {
		assertFalse(BeaconAreaCollector.hasEffectiveRange(new TestBeacon(0)));
	}

	// JUnit no aplica mixins; el subtipo expone el mismo nivel fijado en el campo vanilla.
	private static final class TestBeacon extends BeaconBlockEntity {
		private final int pyramidLevels;

		TestBeacon(int pyramidLevels) throws ReflectiveOperationException {
			super(BlockPos.ZERO, Blocks.BEACON.defaultBlockState());
			this.pyramidLevels = pyramidLevels;
			var levels = BeaconBlockEntity.class.getDeclaredField("levels");
			levels.setAccessible(true);
			levels.setInt(this, pyramidLevels);
		}

		public int extendedbeaconrange$getLevels() {
			return pyramidLevels;
		}
	}
}
