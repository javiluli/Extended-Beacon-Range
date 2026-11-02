package com.javiluli.extendedbeaconrange.feature.perimeter.client;

import static org.junit.jupiter.api.Assertions.*;
import com.javiluli.extendedbeaconrange.feature.perimeter.client.BeaconOverlayColors.OverlayColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BeaconOverlayColorsTest {
	@Test
	void whiteModeCoversEveryPositionAndColoredModeRemainsDeterministic() {
		OverlayColor white = new OverlayColor(1.0f, 1.0f, 1.0f);
		for (int x : new int[] {-1000000, -16, 0, 16, 1000000}) {
			assertEquals(white, BeaconOverlayColors.getPositionColor(x, -x, false));
			OverlayColor color = BeaconOverlayColors.getPositionColor(x, -x, true);
			assertNotEquals(white, color);
			assertEquals(color, BeaconOverlayColors.getPositionColor(x, -x, true));
		}
	}

	@ParameterizedTest
	@CsvSource({"-1000000, 0.89, 1.0, 0.12", "-16, 0.89, 0.12, 1.0", "-1, 0.12, 1.0, 1.0",
			"0, 1.0, 0.12, 0.12", "1, 0.12, 1.0, 1.0", "16, 0.89, 0.12, 1.0", "1000000, 0.89, 1.0, 0.12"})
	void palettePreservesExistingColors(int x, float red, float green, float blue) {
		OverlayColor color = BeaconOverlayColors.getPositionColor(x, -x, true);
		assertEquals(red, color.red(), 0.000001f);
		assertEquals(green, color.green(), 0.000001f);
		assertEquals(blue, color.blue(), 0.000001f);
	}
}
