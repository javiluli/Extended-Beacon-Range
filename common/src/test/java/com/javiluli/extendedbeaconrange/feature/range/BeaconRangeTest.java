package com.javiluli.extendedbeaconrange.feature.range;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class BeaconRangeTest {
	@Test
	void effectAndVisualRangesMatchAllPyramidLevels() {
		assertEquals(0, BeaconRange.effectRadius(0));
		assertEquals(60, BeaconRange.effectRadius(1));
		assertEquals(90, BeaconRange.effectRadius(2));
		assertEquals(120, BeaconRange.effectRadius(3));
		assertEquals(150, BeaconRange.effectRadius(4));
		assertEquals(60.0, BeaconRange.extend(20.0));
		assertEquals(150.0, BeaconRange.extend(50.0));
	}

	@Test
	void unannouncedServersUseVanillaRangeForEveryPyramidLevel() {
		assertEquals(0, BeaconRange.effectRadius(0, false));
		for (int levels = 1; levels <= 4; levels++) {
			assertEquals((levels + 1) * 10, BeaconRange.effectRadius(levels, false));
			assertEquals(BeaconRange.effectRadius(levels), BeaconRange.effectRadius(levels, true));
		}
	}
}
