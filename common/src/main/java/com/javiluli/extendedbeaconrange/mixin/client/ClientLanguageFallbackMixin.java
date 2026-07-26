package com.javiluli.extendedbeaconrange.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.resources.language.ClientLanguage;

/**
 * Traducciones de seguridad para entornos Fabric que no cargan los assets del mod dentro del resource manager.
 *
 * <p>
 * No sustituye los JSON de idioma: solo rellena las claves si Minecraft no las ha encontrado previamente.
 * </p>
 */
@Mixin(ClientLanguage.class)
public class ClientLanguageFallbackMixin {
	private static final String KEY_CATEGORY = "key.categories.extendedbeaconrange";
	private static final String KEY_TOGGLE_OVERLAY = "key.extendedbeaconrange.toggle_overlay";

	/**
	 * Agrega textos minimos para la pantalla de controles.
	 *
	 * <p>
	 * Al usar {@code putIfAbsent} se respetan traducciones de resource packs, archivos lang del mod u otros mods.
	 * </p>
	 *
	 * @param translations mapa mutable de traducciones cargadas por Minecraft.
	 * @return el mismo mapa, con fallbacks minimos si faltaban.
	 */
	@ModifyArg(method = "loadFrom", at = @At(value = "INVOKE", target = "Ljava/util/Map;copyOf(Ljava/util/Map;)Ljava/util/Map;", remap = false))
	private static Map<String, String> extendedbeaconrange$addFallbackTranslations(Map<String, String> translations) {
		translations.putIfAbsent(KEY_CATEGORY, "Extended Beacon Range");
		translations.putIfAbsent(KEY_TOGGLE_OVERLAY, "Toggle targeted beacon perimeter");
		return translations;
	}
}
