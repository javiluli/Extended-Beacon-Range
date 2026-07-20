package com.javiluli.extendedbeaconrange.mixin.client;

import java.util.Map;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Expone el orden interno de categorias de controles para registrar la categoria del mod.
 *
 * <p>
 * Vanilla ordena los keybinds con un mapa privado. Si una categoria nueva no esta registrada ahi, algunas pantallas de Controles pueden
 * fallar al ordenar. Este accessor permite anadir la categoria del perimetro sin depender de APIs externas.
 * </p>
 */
@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {
	/**
	 * Devuelve el mapa mutable que Minecraft usa para ordenar categorias de keybinds.
	 *
	 * @return mapa categoria -> prioridad de orden.
	 */
	@Accessor("CATEGORY_SORT_ORDER")
	static Map<String, Integer> extendedbeaconrange$getCategorySortOrder() {
		throw new AssertionError();
	}
}
