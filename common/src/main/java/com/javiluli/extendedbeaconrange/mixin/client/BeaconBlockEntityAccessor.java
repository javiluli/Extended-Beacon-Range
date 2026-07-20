package com.javiluli.extendedbeaconrange.mixin.client;

import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Expone datos internos del {@link BeaconBlockEntity} que vanilla no publica mediante getter.
 *
 * <p>
 * El overlay necesita leer {@code levels} en cliente para calcular el radio efectivo sin duplicar la logica de activacion del beacon.
 * </p>
 */
@Mixin(BeaconBlockEntity.class)
public interface BeaconBlockEntityAccessor {
	/**
	 * Devuelve los niveles de piramide detectados por el beacon.
	 *
	 * @return niveles vanilla del beacon, de 0 a 4.
	 */
	@Accessor("levels")
	int extendedbeaconrange$getLevels();
}
