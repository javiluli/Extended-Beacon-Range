package com.javiluli.extendedbeaconrange.feature.effecthud.client;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import com.javiluli.extendedbeaconrange.feature.range.BeaconRange;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BeaconEffectCoverageTest {
	@BeforeAll
	static void bootstrapMinecraft() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@ParameterizedTest
	@ValueSource(ints = {1, 2, 3, 4})
	void recognizesNativeRefreshDurationsAndStopsTrustingAMissingRefresh(int levels) {
		int duration = (9 + levels * 2) * 20;
		assertTrue(BeaconEffectHud.maintains(new MobEffectInstance(MobEffects.SPEED, duration, 0, true, true),
				levels, MobEffects.SPEED, null));
		assertTrue(BeaconEffectHud.maintains(new MobEffectInstance(MobEffects.SPEED, duration - 80, 0, true, true),
				levels, MobEffects.SPEED, null));
		assertTrue(BeaconEffectHud.maintains(new MobEffectInstance(MobEffects.SPEED, 340, 0, true, true),
				levels, MobEffects.SPEED, null), "A smaller pyramid can maintain the same amplifier after leaving a larger one");
		assertFalse(BeaconEffectHud.maintains(new MobEffectInstance(MobEffects.SPEED, duration - 83, 0, true, true),
				levels, MobEffects.SPEED, null));
	}

	@Test
	void weakerBeaconDoesNotFreezeTheStrongerEffect() {
		var speedII = new MobEffectInstance(MobEffects.SPEED, 300, 1, true, true);
		assertFalse(BeaconEffectHud.maintains(speedII, 4, MobEffects.SPEED, MobEffects.REGENERATION));
		assertTrue(BeaconEffectHud.maintains(speedII, 4, MobEffects.SPEED, MobEffects.SPEED));
		assertFalse(BeaconEffectHud.maintains(speedII, 3, MobEffects.SPEED, MobEffects.SPEED));
		assertFalse(BeaconEffectHud.maintains(speedII, 4, MobEffects.STRENGTH, MobEffects.STRENGTH));
	}

	@Test
	void secondaryRequiresAFourLevelPyramidAndDoesNotPromoteItsLevel() {
		var regeneration = new MobEffectInstance(MobEffects.REGENERATION, 300, 0, true, true);
		assertTrue(BeaconEffectHud.maintains(regeneration, 4, MobEffects.SPEED, MobEffects.REGENERATION));
		assertFalse(BeaconEffectHud.maintains(regeneration, 3, MobEffects.SPEED, MobEffects.REGENERATION));
		assertFalse(BeaconEffectHud.maintains(new MobEffectInstance(MobEffects.REGENERATION, 300, 1, true, true),
				4, MobEffects.SPEED, MobEffects.REGENERATION));
	}

	@Test
	void neverClaimsUnconfirmedLongPotionOrInfiniteDurationsAreBeaconRefreshes() {
		assertFalse(BeaconEffectHud.maintains(new MobEffectInstance(MobEffects.SPEED, 1200, 0, true, true),
				4, MobEffects.SPEED, null));
		assertFalse(BeaconEffectHud.maintains(new MobEffectInstance(MobEffects.SPEED, 300),
				4, MobEffects.SPEED, null));
		assertFalse(BeaconEffectHud.maintains(new MobEffectInstance(MobEffects.SPEED, -1, 0, true, true),
				4, MobEffects.SPEED, null));
		assertFalse(BeaconEffectHud.maintains(new MobEffectInstance(MobEffects.SPEED, 300, 0, true, true),
				4, null, null));
	}

	@Test
	void onlyIncludesTheSixNativeBeaconEffectTypes() {
		for (var type : List.of(MobEffects.SPEED, MobEffects.HASTE, MobEffects.RESISTANCE,
				MobEffects.JUMP_BOOST, MobEffects.STRENGTH, MobEffects.REGENERATION)) {
			assertTrue(BeaconEffectHud.supports(new MobEffectInstance(type, 100)));
		}
		assertFalse(BeaconEffectHud.supports(new MobEffectInstance(MobEffects.POISON, 100)));
		assertFalse(BeaconEffectHud.supports(new MobEffectInstance(MobEffects.NIGHT_VISION, 100)));
	}

	@Test
	void clientOnlyDoesNotAssumeTheServerHasExtendedRange() {
		BlockPos pos = new BlockPos(0, 64, 0);
		AABB outsideVanilla = new AABB(100, 64, 0, 100.6, 65.8, .6);
		assertFalse(BeaconEffectHud.intersectsRange(pos, BeaconRange.effectRadius(4, false), 384, outsideVanilla));
		assertTrue(BeaconEffectHud.intersectsRange(pos, BeaconRange.effectRadius(4, true), 384, outsideVanilla));
		assertFalse(BeaconEffectHud.intersectsRange(pos, 0, 384, new AABB(pos)));
	}

}
