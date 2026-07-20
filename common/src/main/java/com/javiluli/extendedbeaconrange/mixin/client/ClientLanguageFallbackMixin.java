package com.javiluli.extendedbeaconrange.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.resources.language.ClientLanguage;

/**
 * Traducciones de seguridad para entornos Fabric que no cargan los assets del mod
 * dentro del resource manager. No sustituye los JSON de idioma: solo rellena las
 * claves si Minecraft no las ha encontrado previamente.
 */
@Mixin(ClientLanguage.class)
public class ClientLanguageFallbackMixin {
	private static final String KEY_CATEGORY = "key.categories.extendedbeaconrange";
	private static final String KEY_TOGGLE_OVERLAY = "key.extendedbeaconrange.toggle_overlay";

	/**
	 * Agrega textos minimos para la pantalla de controles. Al usar putIfAbsent se
	 * respetan traducciones de resource packs, archivos lang del mod u otros mods.
	 */
	@ModifyArg(method = "loadFrom", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;copyOf(Ljava/util/Map;)Lcom/google/common/collect/ImmutableMap;", remap = false))
	private static Map<String, String> extendedbeaconrange$addFallbackTranslations(Map<String, String> translations) {
		translations.putIfAbsent(KEY_CATEGORY, "Extended Beacon Range");
		translations.putIfAbsent(KEY_TOGGLE_OVERLAY, "Toggle targeted beacon perimeter");
		return translations;
	}
}
