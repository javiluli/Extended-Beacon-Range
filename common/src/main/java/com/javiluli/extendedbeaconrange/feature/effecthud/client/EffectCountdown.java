package com.javiluli.extendedbeaconrange.feature.effecthud.client;

import java.util.Locale;
import net.minecraft.network.chat.Component;

/** Conserva la duracion observada sin confundir una renovacion con un cambio de nivel. */
final class EffectCountdown {
	private int duration;
	private int total;
	private int amplifier = -1;
	private boolean maintained;
	private int remainingSeconds;
	private int displayedSeconds = -1;
	private String time = "00:00";
	private Component level = Component.empty();

	/** Reinicia la referencia al renovarse, cambiar de nivel o salir de cobertura. */
	void update(int remaining, int newAmplifier, boolean covered, float tickRate) {
		if (newAmplifier != amplifier) {
			level = newAmplifier < 10 ? Component.translatable("enchantment.level." + (newAmplifier + 1))
					: Component.literal(Integer.toString(newAmplifier + 1));
		}
		if (newAmplifier != amplifier || remaining > duration || (maintained && !covered)) {
			total = Math.max(1, remaining);
		}
		duration = remaining;
		amplifier = newAmplifier;
		maintained = covered;
		remainingSeconds = remaining < 0 ? -1 : (int) Math.ceil(remaining / (double) tickRate);
	}

	/** Mantiene el borde completo para efectos infinitos o renovados por un beacon. */
	float remainingFraction(float partialTick) {
		return duration < 0 || maintained ? 1.0f : Math.clamp((duration - partialTick) / total, 0.0f, 1.0f);
	}

	/** Solo muestra tiempo cuando representa la caducidad del nivel activo. */
	String time() {
		if (duration < 0 || maintained) { return ""; }
		if (remainingSeconds != displayedSeconds) {
			displayedSeconds = remainingSeconds;
			time = String.format(Locale.ROOT, "%02d:%02d", remainingSeconds / 60, remainingSeconds % 60);
		}
		return time;
	}

	/** Reutiliza el nivel traducible hasta que cambie el amplificador. */
	Component level() { return level; }
}
