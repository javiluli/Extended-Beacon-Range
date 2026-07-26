package com.javiluli.extendedbeaconrange.client.overlay;

import com.javiluli.extendedbeaconrange.Constants;
import com.javiluli.extendedbeaconrange.mixin.client.OptionsAccessor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import org.lwjgl.glfw.GLFW;

/**
 * Gestiona el estado del overlay y los atajos para mostrar u ocultar perimetros concretos.
 */
public final class BeaconOverlayToggle {
	/** Nombre de traduccion del keybind configurable que aparece en Controles. */
	private static final String TOGGLE_KEY_TRANSLATION = "key.extendedbeaconrange.toggle_overlay";
	/** Categoria vanilla registrada para agrupar el keybind del mod dentro de Controles. */
	private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category
			.register(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "controls"));
	/**
	 * Keybind configurable desde los ajustes vanilla.
	 *
	 * <p>
	 * Minecraft vanilla gestiona teclas simples en la pantalla de Controles. Por eso mantenemos {@code Ctrl + H} como atajo adicional,
	 * pero exponemos {@code H} como tecla principal configurable para funcionar igual en Fabric, Forge y NeoForge sin dependencias extra.
	 * </p>
	 */
	private static final KeyMapping TOGGLE_OVERLAY_KEY = new KeyMapping(TOGGLE_KEY_TRANSLATION, InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_H, KEY_CATEGORY);
	/**
	 * Beacons seleccionados por el jugador para renderizar su perimetro.
	 *
	 * <p>
	 * Guardamos posiciones, no entidades de bloque, para evitar retener referencias de mundos descargados. Si el chunk esta cargado y el
	 * bloque deja de ser un beacon, la posicion se limpia automaticamente.
	 * </p>
	 */
	private static final Set<BlockPos> VISIBLE_BEACONS = new HashSet<>();
	/** Indica que la seleccion actual viene del modo "mostrar todos" y no de toggles individuales. */
	private static boolean allLoadedBeaconsVisible;
	/** Mundo cliente asociado al conjunto actual de beacons seleccionados. */
	private static ClientLevel lastLevel;
	/** Evita registrar el keybind mas de una vez en la instancia de opciones del cliente. */
	private static boolean keyMappingRegistered;
	/** Estado anterior del atajo para alternar solo una vez por pulsacion. */
	private static boolean toggleShortcutWasDown;

	private BeaconOverlayToggle() {
	}

	/**
	 * Procesa el atajo global y devuelve si el overlay debe renderizarse.
	 *
	 * @param minecraft instancia de cliente actual.
	 * @return {@code true} si hay al menos un beacon seleccionado.
	 */
	public static boolean updateAndHasVisibleBeacons(Minecraft minecraft) {
		if (minecraft != null) {
			registerKeyMapping(minecraft.options);
		}
		clearSelectionWhenLevelChanges(minecraft);
		updateOverlayToggleShortcut(minecraft);
		cleanupInvalidVisibleBeacons(minecraft);
		return !VISIBLE_BEACONS.isEmpty();
	}

	/**
	 * Comprueba si una posicion de beacon debe renderizarse en este frame.
	 *
	 * @param pos posicion absoluta del beacon.
	 * @return {@code true} si el jugador activo ese beacon previamente.
	 */
	public static boolean isBeaconVisible(BlockPos pos) {
		return VISIBLE_BEACONS.contains(pos);
	}

	/**
	 * Registra el keybind en las opciones del cliente para que aparezca en la pantalla de Controles.
	 *
	 * <p>
	 * Se llama desde el mixin de {@link Options#load(boolean)} para que el keybind exista antes de procesar {@code options.txt}. Asi aparece
	 * en la pantalla de Controles desde el menu principal y conserva la tecla personalizada entre reinicios.
	 * </p>
	 *
	 * @param options opciones del cliente donde Minecraft guarda los keybinds.
	 */
	public static void registerKeyMapping(Options options) {
		if (keyMappingRegistered || options == null) {
			return;
		}

		KeyMapping[] keyMappings = options.keyMappings;
		for (KeyMapping keyMapping : keyMappings) {
			if (keyMapping == TOGGLE_OVERLAY_KEY) {
				keyMappingRegistered = true;
				return;
			}
		}

		KeyMapping[] expandedKeyMappings = Arrays.copyOf(keyMappings, keyMappings.length + 1);
		expandedKeyMappings[expandedKeyMappings.length - 1] = TOGGLE_OVERLAY_KEY;
		((OptionsAccessor) options).extendedbeaconrange$setKeyMappings(expandedKeyMappings);
		KeyMapping.resetMapping();
		keyMappingRegistered = true;
	}

	/**
	 * Limpia la seleccion al cambiar de mundo o servidor para evitar perimetros heredados entre partidas.
	 *
	 * @param minecraft instancia de cliente actual.
	 */
	private static void clearSelectionWhenLevelChanges(Minecraft minecraft) {
		ClientLevel currentLevel = minecraft == null ? null : minecraft.level;
		if (currentLevel != lastLevel) {
			clearVisibleBeacons();
			lastLevel = currentLevel;
		}
	}

	/**
	 * Alterna beacons concretos con el keybind configurable o con el atajo historico {@code Ctrl + H}.
	 *
	 * @param minecraft instancia de cliente actual.
	 */
	private static void updateOverlayToggleShortcut(Minecraft minecraft) {
		if (minecraft.screen != null || minecraft.getWindow() == null) {
			toggleShortcutWasDown = false;
			return;
		}

		boolean toggledByConfigurableKey = false;
		while (TOGGLE_OVERLAY_KEY.consumeClick()) {
			toggleTargetedBeaconOrClearAll(minecraft);
			toggledByConfigurableKey = true;
		}

		Window window = minecraft.getWindow();
		boolean controlDown = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL)
				|| InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
		boolean shortcutDown = controlDown && InputConstants.isKeyDown(window, GLFW.GLFW_KEY_H);
		if (shortcutDown && !toggleShortcutWasDown && !toggledByConfigurableKey) {
			toggleTargetedBeaconOrClearAll(minecraft);
		}

		toggleShortcutWasDown = shortcutDown;
	}

	/**
	 * Alterna solo el beacon apuntado dentro del alcance vanilla. Si no hay beacon apuntado, activa todos los beacons cargados cuando no hay
	 * ninguno seleccionado, o limpia toda la seleccion cuando ya hay algun perimetro visible.
	 *
	 * @param minecraft instancia de cliente actual.
	 */
	private static void toggleTargetedBeaconOrClearAll(Minecraft minecraft) {
		BlockPos targetedBeacon = getTargetedBeaconPos(minecraft);
		if (targetedBeacon == null) {
			if (VISIBLE_BEACONS.isEmpty()) {
				int activatedBeacons = activateAllLoadedBeacons(minecraft);
				allLoadedBeaconsVisible = activatedBeacons > 0;
				showOverlayToggleMessage(minecraft, activatedBeacons > 0 ? "All beacon perimeters" : "No beacon perimeters found",
						activatedBeacons > 0);
			} else {
				clearVisibleBeacons();
				showOverlayToggleMessage(minecraft, "All beacon perimeters", false);
			}
			return;
		}

		if (allLoadedBeaconsVisible) {
			clearVisibleBeacons();
			showOverlayToggleMessage(minecraft, "All beacon perimeters", false);
			return;
		}

		boolean nowVisible;
		BlockPos immutableTarget = targetedBeacon.immutable();
		if (VISIBLE_BEACONS.contains(immutableTarget)) {
			VISIBLE_BEACONS.remove(immutableTarget);
			nowVisible = false;
		} else {
			VISIBLE_BEACONS.add(immutableTarget);
			nowVisible = true;
		}

		allLoadedBeaconsVisible = false;
		showOverlayToggleMessage(minecraft, "Beacon perimeter", nowVisible);
	}

	/**
	 * Borra toda seleccion activa y sale del modo "mostrar todos".
	 */
	private static void clearVisibleBeacons() {
		VISIBLE_BEACONS.clear();
		allLoadedBeaconsVisible = false;
	}

	/**
	 * Busca el beacon que el jugador esta mirando con el alcance normal de picar/interactuar.
	 *
	 * @param minecraft instancia de cliente actual.
	 * @return posicion del beacon apuntado, o {@code null} si no hay ninguno.
	 */
	private static BlockPos getTargetedBeaconPos(Minecraft minecraft) {
		if (minecraft == null || minecraft.level == null || !(minecraft.hitResult instanceof BlockHitResult blockHit)
				|| blockHit.getType() != HitResult.Type.BLOCK) {
			return null;
		}

		BlockPos pos = blockHit.getBlockPos();
		if (!minecraft.level.getBlockState(pos).is(Blocks.BEACON)) {
			return null;
		}

		BlockEntity blockEntity = minecraft.level.getBlockEntity(pos);
		return blockEntity instanceof BeaconBlockEntity beacon && BeaconAreaCollector.hasEffectiveRange(beacon) ? pos : null;
	}

	/**
	 * Selecciona todos los beacons cargados dentro de la distancia de render del cliente.
	 *
	 * @param minecraft instancia de cliente actual.
	 * @return cantidad de beacons anadidos a la seleccion.
	 */
	private static int activateAllLoadedBeacons(Minecraft minecraft) {
		if (minecraft == null || minecraft.level == null || minecraft.player == null) {
			return 0;
		}

		return BeaconAreaCollector.addLoadedBeaconPositions(minecraft, VISIBLE_BEACONS);
	}

	/**
	 * Elimina posiciones seleccionadas que ya no son beacons en chunks cargados.
	 *
	 * @param minecraft instancia de cliente actual.
	 */
	private static void cleanupInvalidVisibleBeacons(Minecraft minecraft) {
		if (minecraft == null || minecraft.level == null || VISIBLE_BEACONS.isEmpty()) {
			return;
		}

		for (Iterator<BlockPos> iterator = VISIBLE_BEACONS.iterator(); iterator.hasNext();) {
			BlockPos pos = iterator.next();
			boolean isLoaded = minecraft.level.getChunkSource().getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false) != null;
			if (isLoaded) {
				BlockEntity blockEntity = minecraft.level.getBlockEntity(pos);
				if (!(blockEntity instanceof BeaconBlockEntity beacon) || !BeaconAreaCollector.hasEffectiveRange(beacon)) {
					iterator.remove();
				}
			}
		}

		if (VISIBLE_BEACONS.isEmpty()) {
			allLoadedBeaconsVisible = false;
		}
	}

	/**
	 * Muestra un mensaje corto en pantalla al cambiar el estado de un perimetro.
	 *
	 * @param minecraft instancia de cliente actual.
	 * @param label texto blanco que describe que se ha cambiado.
	 * @param enabled {@code true} para ON en verde, {@code false} para OFF en rojo.
	 */
	private static void showOverlayToggleMessage(Minecraft minecraft, String label, boolean enabled) {
		if (minecraft.player != null) {
			Component message = Component.literal(label + " ").withStyle(ChatFormatting.WHITE)
					.append(Component.literal(enabled ? "ON" : "OFF").withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
			minecraft.player.sendOverlayMessage(message);
		}
	}
}
