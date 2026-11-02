package com.javiluli.extendedbeaconrange.feature.effecthud.client;

import static org.junit.jupiter.api.Assertions.*;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EffectCountdownTest {
	@BeforeAll
	static void bootstrapMinecraft() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void countsDownFromTheObservedDurationAndNeverLeavesItsBounds() {
		EffectCountdown timer = new EffectCountdown();
		timer.update(340, 0, false, 20);
		assertEquals("00:17", timer.time());
		assertEquals(1f, timer.remainingFraction(0));
		timer.update(170, 0, false, 20);
		assertEquals("00:09", timer.time());
		assertEquals(.5f, timer.remainingFraction(0));
		assertEquals(169.5f / 340, timer.remainingFraction(.5f));
		timer.update(0, 0, false, 20);
		assertEquals("00:00", timer.time());
		assertEquals(0, timer.remainingFraction(1));
	}

	@Test
	void freezesInCoverageAndStartsFromTheRealRemainingTimeOnExit() {
		EffectCountdown timer = new EffectCountdown();
		timer.update(340, 1, true, 20);
		timer.update(270, 1, true, 20);
		assertEquals(1f, timer.remainingFraction(.5f));
		assertEquals("", timer.time());
		timer.update(269, 1, false, 20);
		assertEquals(1f, timer.remainingFraction(0));
		assertEquals("00:14", timer.time());
		timer.update(134, 1, false, 20);
		assertEquals(134f / 269, timer.remainingFraction(0));
	}

	@Test
	void resetsForRefreshAndForADowngradeToAHiddenWeakerEffect() {
		EffectCountdown timer = new EffectCountdown();
		timer.update(340, 1, false, 20);
		timer.update(100, 1, false, 20);
		timer.update(340, 1, false, 20);
		assertEquals(1f, timer.remainingFraction(0));
		assertEquals("enchantment.level.2", ((TranslatableContents) timer.level().getContents()).getKey());
		timer.update(100, 0, true, 20);
		assertEquals("enchantment.level.1", ((TranslatableContents) timer.level().getContents()).getKey());
		assertEquals("", timer.time());
		timer.update(99, 0, false, 20);
		assertEquals(1f, timer.remainingFraction(0));
	}

	@Test
	void handlesInfiniteEffectsMinutesAndTheFinalPartialSecond() {
		EffectCountdown timer = new EffectCountdown();
		timer.update(-1, 0, false, 20);
		assertEquals(1f, timer.remainingFraction(.9f));
		assertEquals("", timer.time());
		timer.update(1201, 0, false, 20);
		assertEquals("01:01", timer.time());
		timer.update(1, 0, false, 20);
		assertEquals("00:01", timer.time());
		assertEquals(0, timer.remainingFraction(2));
		timer.update(Integer.MAX_VALUE, 255, false, 20);
		assertEquals("256", timer.level().getString());
		assertFalse(timer.time().startsWith("-"));
	}

	@Test
	void usesTheNativeTickRateWhenTheServerChangesIt() {
		EffectCountdown timer = new EffectCountdown();
		timer.update(340, 0, false, 10);
		assertEquals("00:34", timer.time());
		timer.update(340, 0, false, 40);
		assertEquals("00:09", timer.time());
	}

	@Test
	void lazilyFormatsTheCurrentTimeAndReusesItUntilTheSecondChanges() {
		EffectCountdown timer = new EffectCountdown();
		timer.update(340, 0, false, 20);
		String original = timer.time();
		timer.update(339, 0, false, 20);
		assertSame(original, timer.time());
		timer.update(300, 0, true, 20);
		assertEquals("", timer.time());
		timer.update(259, 0, false, 20);
		assertEquals("00:13", timer.time());
		timer.update(200, 0, false, 20);
		timer.update(100, 0, false, 20);
		assertEquals("00:05", timer.time(), "Enabling timers later must not reveal a stale cached value");
	}

	@Test
	void longTimersWideFontsAndHigherLevelsFitInsideTheirOwnIcon() {
		assertEquals(.75f, BeaconEffectHud.textScale(24, 22));
		for (int width : new int[] {0, 1, 8, 24, 32, 64, 512}) {
			for (int available : new int[] {18, 22}) {
				float scale = BeaconEffectHud.textScale(width, available);
				assertTrue(scale > 0 && scale <= .75f);
				assertTrue(width * scale <= available + .0001f);
			}
		}
	}
}
