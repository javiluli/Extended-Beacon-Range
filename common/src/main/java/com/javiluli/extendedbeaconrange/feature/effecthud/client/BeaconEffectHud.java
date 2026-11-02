package com.javiluli.extendedbeaconrange.feature.effecthud.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.javiluli.extendedbeaconrange.feature.config.client.BeaconClientConfig;
import com.javiluli.extendedbeaconrange.feature.range.BeaconRange;
import com.javiluli.extendedbeaconrange.mixin.client.BeaconBlockEntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/** Anade informacion al HUD sin sustituir los iconos ni modificar los efectos del jugador. */
public final class BeaconEffectHud {
	public static final Identifier BACKGROUND = Identifier.withDefaultNamespace("hud/effect_background_ambient");
	private static final Set<Holder<MobEffect>> EFFECTS = BeaconBlockEntity.BEACON_EFFECTS.stream()
			.flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet());
	private static final Set<BeaconBlockEntity> beacons = new HashSet<>();
	private static final Map<Holder<MobEffect>, EffectCountdown> timers = new HashMap<>();
	private static ClientLevel level;
	private static LocalPlayer player;
	private BeaconEffectHud() { }

	/** Limita esta feature a los tipos de efecto que vanilla ofrece en los beacons. */
	public static boolean supports(MobEffectInstance effect) { return EFFECTS.contains(effect.getEffect()); }

	/** Descubre beacons mediante sus ticks nativos, sin recorrer ni cargar chunks. */
	public static void track(BeaconBlockEntity beacon) {
		if (beacon.getLevel() == level) {
			if (beacon.getBeamSections().isEmpty()) { beacons.remove(beacon); }
			else { beacons.add(beacon); }
		}
	}

	/** Actualiza duraciones y cobertura una vez por tick y limpia cambios de jugador o dimension. */
	public static void tick(Minecraft minecraft) {
		if (level != minecraft.level || player != minecraft.player) {
			beacons.clear();
			timers.clear();
			level = minecraft.level;
			player = minecraft.player;
		}
		if (level == null || player == null || minecraft.isPaused()) { return; }
		beacons.removeIf(beacon -> beacon.isRemoved() || beacon.getLevel() != level);
		var active = player.getActiveEffectsMap();
		timers.keySet().removeIf(effect -> !active.containsKey(effect) || !active.get(effect).showIcon());
		for (MobEffectInstance effect : active.values()) {
			if (!supports(effect) || !effect.showIcon()) { continue; }
			boolean maintained = false;
			for (BeaconBlockEntity beacon : beacons) {
				var data = (BeaconBlockEntityAccessor) beacon;
				int levels = data.extendedbeaconrange$getLevels();
				if (beacon.getBeamSections().isEmpty() || !maintains(effect, levels,
						data.extendedbeaconrange$getPrimaryPower(), data.extendedbeaconrange$getSecondaryPower())) { continue; }
				boolean extended = ((BeaconRange.Source) beacon).extendedbeaconrange$usesExtendedRange();
				if (intersectsRange(beacon.getBlockPos(), BeaconRange.effectRadius(levels, extended),
						level.getHeight(), player.getBoundingBox())) { maintained = true; break; }
			}
			timers.computeIfAbsent(effect.getEffect(), key -> new EffectCountdown())
					.update(effect.getDuration(), effect.getAmplifier(), maintained, level.tickRateManager().tickrate());
		}
	}

	/** Reproduce el volumen vanilla sin crear cajas temporales por beacon y efecto. */
	static boolean intersectsRange(BlockPos pos, int radius, int worldHeight, AABB bounds) {
		return radius > 0 && bounds.intersects(pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
				pos.getX() + radius + 1.0, pos.getY() + radius + worldHeight + 1.0, pos.getZ() + radius + 1.0);
	}

	/** Comprueba el nivel activo y limita la cobertura al intervalo de renovacion vanilla. */
	static boolean maintains(MobEffectInstance effect, int levels,
			@Nullable Holder<MobEffect> primary, @Nullable Holder<MobEffect> secondary) {
		if (levels < 1 || levels > 4 || primary == null || !effect.isAmbient() || effect.isInfiniteDuration()) { return false; }
		int amplifier = -1;
		if (effect.getEffect().equals(primary)) {
			amplifier = levels == 4 && primary.equals(secondary) ? 1 : 0;
		} else if (levels == 4 && effect.getEffect().equals(secondary)) {
			amplifier = 0;
		}
		int refreshedDuration = (9 + levels * 2) * 20;
		// shortcut: vanilla no indica el origen; sin la renovacion esperada se muestra el tiempo real. Una fuente de nivel I puede mantener un efecto I mas largo.
		return effect.getAmplifier() == amplifier && effect.getDuration() <= (9 + 4 * 2) * 20
				&& effect.getDuration() >= refreshedDuration - 80 - 2;
	}

	/** Reserva espacio para el contador sin solaparlo con la fila de efectos negativos. */
	public static boolean needsTimerSpace() { return !timers.isEmpty() && BeaconClientConfig.get().showEffectTimers; }

	/** Oscurece el borde original en sentido horario desde el centro superior. */
	public static void extractBorder(GuiGraphicsExtractor graphics, MobEffectInstance effect, int x, int y, float partialTick) {
		EffectCountdown timer = timers.get(effect.getEffect());
		if (timer == null) { return; }
		int dark = (int) Math.ceil((1.0f - timer.remainingFraction(partialTick)) * 84);
		if (dark == 0) { return; }
		darken(graphics, x, y, 12, 0, Math.min(dark, 12), 3);
		darken(graphics, x, y, 21, 3, 3, Math.min(dark - 12, 21));
		int bottom = Math.clamp(dark - 33, 0, 21);
		darken(graphics, x, y, 21 - bottom, 21, bottom, 3);
		int left = Math.clamp(dark - 54, 0, 21);
		darken(graphics, x, y, 0, 21 - left, 3, left);
		darken(graphics, x, y, 3, 0, Math.clamp(dark - 75, 0, 9), 3);
	}

	/** Recorta el mismo sprite para conservar su forma y los resource packs del jugador. */
	private static void darken(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int width, int height) {
		if (width > 0 && height > 0) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, 24, 24, u, v, x + u, y + v, width, height, 0xff000000);
		}
	}

	/** Dibuja el nivel real y el contador opcional despues del icono vanilla. */
	public static void extractLabels(GuiGraphicsExtractor graphics, MobEffectInstance effect, int x, int y) {
		EffectCountdown timer = timers.get(effect.getEffect());
		if (timer == null) { return; }
		var levelText = timer.level();
		var font = Minecraft.getInstance().font;
		int width = font.width(levelText);
		graphics.pose().pushMatrix();
		graphics.pose().translate(x + 20, y + 15).scale(textScale(width + 2, 18));
		graphics.fill(-width - 1, -1, 1, 9, 0xb0000000);
		graphics.text(font, levelText, -width, 0, 0xffffffff);
		graphics.pose().popMatrix();
		if (BeaconClientConfig.get().showEffectTimers) {
			String time = timer.time();
			if (time.isEmpty()) { return; }
			int timeWidth = font.width(time);
			graphics.pose().pushMatrix();
			graphics.pose().translate(x + 12, y + 25).scale(textScale(timeWidth, 22));
			graphics.text(font, time, -timeWidth / 2, 0, 0xffffffff);
			graphics.pose().popMatrix();
		}
	}

	/** Mantiene el tamano habitual y ajusta solo textos que exceden su espacio en el HUD. */
	static float textScale(int textWidth, int availableWidth) {
		return Math.min(0.75f, availableWidth / (float) Math.max(1, textWidth));
	}
}
