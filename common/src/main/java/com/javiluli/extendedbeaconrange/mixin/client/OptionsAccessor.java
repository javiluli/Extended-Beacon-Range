package com.javiluli.extendedbeaconrange.mixin.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Permite registrar keybinds client-side desde codigo comun sin depender de eventos especificos de Fabric, Forge o NeoForge.
 *
 * <p>
 * Minecraft guarda los keybinds en un array final dentro de {@link Options}. Para que el toggle del perimetro aparezca en la pantalla de
 * Controles en los tres loaders, ampliamos ese array mediante accessor solo en cliente.
 * </p>
 */
@Mixin(Options.class)
public interface OptionsAccessor {
	/**
	 * Sustituye el array interno de keybinds por una copia ampliada.
	 *
	 * @param keyMappings array completo de keybinds que Minecraft mostrara en Controles.
	 */
	@Mutable
	@Accessor("keyMappings")
	void extendedbeaconrange$setKeyMappings(KeyMapping[] keyMappings);
}
