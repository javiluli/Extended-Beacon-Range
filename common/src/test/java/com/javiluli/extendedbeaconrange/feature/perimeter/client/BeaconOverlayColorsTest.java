package com.javiluli.extendedbeaconrange.feature.perimeter.client;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import com.javiluli.extendedbeaconrange.feature.perimeter.client.BeaconOverlayColors.OverlayColor;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBeamOwner;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BeaconOverlayColorsTest {
	@BeforeAll
	static void bootstrapMinecraft() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void matchesTheTopBeamSectionIncludingVanillaGlassMixing() {
		int mixed = ARGB.average(0xffff0000, 0xff0000ff);
		var beacon = beacon(new ArrayList<>(List.of(new BeaconBeamOwner.Section(0xffffffff),
				new BeaconBeamOwner.Section(0xffff0000), new BeaconBeamOwner.Section(mixed))));
		OverlayColor color = BeaconOverlayColors.getColor(beacon, true, true, null);
		assertEquals(ARGB.redFloat(mixed), color.red());
		assertEquals(ARGB.greenFloat(mixed), color.green());
		assertEquals(ARGB.blueFloat(mixed), color.blue());
	}

	@Test
	void whiteTakesPriorityAndDisablingBeamMatchingRestoresThePositionPalette() {
		var beacon = beacon(new ArrayList<>(List.of(new BeaconBeamOwner.Section(0xff123456))));
		OverlayColor white = new OverlayColor(1, 1, 1);
		assertEquals(white, BeaconOverlayColors.getColor(beacon, false, true, null));
		assertEquals(white, BeaconOverlayColors.getColor(beacon, false, false, null));
		assertEquals(BeaconOverlayColors.getPositionColor(16, -16, true), BeaconOverlayColors.getColor(beacon, true, false, null));
	}

	@Test
	void reusesStableColorsAndReactsToReplacedBeamSectionsWithoutChangingTheBeam() {
		var sections = new ArrayList<>(List.of(new BeaconBeamOwner.Section(0xff123456)));
		var beacon = beacon(sections);
		OverlayColor initial = BeaconOverlayColors.getColor(beacon, true, true, null);
		assertSame(initial, BeaconOverlayColors.getColor(beacon, true, true, initial));
		sections.set(0, new BeaconBeamOwner.Section(0xffabcdef));
		OverlayColor updated = BeaconOverlayColors.getColor(beacon, true, true, initial);
		assertNotEquals(initial, updated);
		assertEquals(0xffabcdef, sections.getLast().getColor());
		sections.set(0, new BeaconBeamOwner.Section(0xffffffff));
		assertEquals(new OverlayColor(1, 1, 1), BeaconOverlayColors.getColor(beacon, true, true, updated));
		sections.clear();
		assertEquals(new OverlayColor(1, 1, 1), BeaconOverlayColors.getColor(beacon, true, true, updated));
	}

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

	/** Usa secciones nativas sin necesitar un mundo o contexto grafico. */
	private static BeaconBlockEntity beacon(List<BeaconBeamOwner.Section> sections) {
		return new BeaconBlockEntity(new BlockPos(16, 64, -16), Blocks.BEACON.defaultBlockState()) {
			@Override
			public List<BeaconBeamOwner.Section> getBeamSections() { return sections; }
		};
	}
}
