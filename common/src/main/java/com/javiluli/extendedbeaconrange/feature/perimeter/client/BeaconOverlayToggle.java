package com.javiluli.extendedbeaconrange.feature.perimeter.client;

import java.util.Arrays;
import com.javiluli.extendedbeaconrange.Constants;
import com.javiluli.extendedbeaconrange.mixin.perimeter.client.OptionsAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Procesa atajos y estado en ticks; el renderer solo consume datos preparados. */
public final class BeaconOverlayToggle {
	private static final KeyMapping TOGGLE_OVERLAY_KEY = new KeyMapping("key.extendedbeaconrange.toggle_overlay",
			InputConstants.Type.KEYBOARD, InputConstants.KEY_H,
			KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "controls")));
	private static final BeaconSelection SELECTION = new BeaconSelection();
	private static ClientLevel lastLevel;
	private static boolean keyMappingRegistered;
	private static boolean toggleShortcutWasDown;
	private BeaconOverlayToggle() { }

	/** Actualiza mundos, teclas y deteccion una vez por tick, no por frame. */
	public static void tick(Minecraft minecraft) {
		if (minecraft.level != lastLevel) {
			SELECTION.clear();
			lastLevel = minecraft.level;
			BeaconAreaCollector.reset(lastLevel);
			toggleShortcutWasDown = false;
		}
		if (lastLevel == null || minecraft.player == null) { return; }
		updateShortcut(minecraft);
		BeaconAreaCollector.update(minecraft, SELECTION);
	}

	/** Indica si deben descubrirse automaticamente nuevos beacons. */
	public static boolean isGlobal() { return SELECTION.global; }

	/** Registra el atajo antes de cargar options.txt y conserva reasignaciones vanilla. */
	public static void registerKeyMapping(Options options) {
		if (keyMappingRegistered || options == null) { return; }
		for (KeyMapping mapping : options.keyMappings) {
			if (mapping == TOGGLE_OVERLAY_KEY) { keyMappingRegistered = true; return; }
		}
		KeyMapping[] expanded = Arrays.copyOf(options.keyMappings, options.keyMappings.length + 1);
		expanded[expanded.length - 1] = TOGGLE_OVERLAY_KEY;
		((OptionsAccessor) options).extendedbeaconrange$setKeyMappings(expanded);
		KeyMapping.resetMapping();
		keyMappingRegistered = true;
	}

	/** Evita doble alternancia entre el keybind configurable y Ctrl+H. */
	private static void updateShortcut(Minecraft minecraft) {
		if (minecraft.gui.screen() != null || minecraft.getWindow() == null) {
			toggleShortcutWasDown = false;
			return;
		}
		boolean handled = false;
		while (TOGGLE_OVERLAY_KEY.consumeClick()) {
			toggle(minecraft);
			handled = true;
		}
		boolean control = InputConstants.isKeyDown(InputConstants.KEY_LCONTROL)
				|| InputConstants.isKeyDown(InputConstants.KEY_RCONTROL);
		boolean down = control && InputConstants.isKeyDown(InputConstants.KEY_H);
		if (down && !toggleShortcutWasDown && !handled) { toggle(minecraft); }
		toggleShortcutWasDown = down;
	}

	/** Alterna el beacon mirado o el modo global, incluso antes de que aparezca el primer beacon. */
	private static void toggle(Minecraft minecraft) {
		BlockPos target = targetedBeacon(minecraft);
		boolean all = target == null || SELECTION.global;
		boolean enabled = SELECTION.toggle(target);
		if (enabled && SELECTION.global) { BeaconAreaCollector.requestGlobalDiscovery(); }
		Component message = Component.literal(all ? "All beacon perimeters " : "Beacon perimeter ").withStyle(ChatFormatting.WHITE)
				.append(Component.literal(enabled ? "ON" : "OFF").withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
		minecraft.player.sendOverlayMessage(message);
	}

	/** Obtiene solo el beacon apuntado mediante el alcance normal de interaccion. */
	private static BlockPos targetedBeacon(Minecraft minecraft) {
		if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) { return null; }
		BlockPos pos = hit.getBlockPos();
		if (!minecraft.level.getBlockState(pos).is(Blocks.BEACON)) { return null; }
		BlockEntity entity = minecraft.level.getBlockEntity(pos);
		return entity instanceof BeaconBlockEntity beacon && BeaconAreaCollector.hasEffectiveRange(beacon) ? pos : null;
	}
}
