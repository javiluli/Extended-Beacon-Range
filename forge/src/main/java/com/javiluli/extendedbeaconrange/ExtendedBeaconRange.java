package com.javiluli.extendedbeaconrange;

import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class ExtendedBeaconRange {
    public ExtendedBeaconRange() {
        /**
         * +-------+
         * |English|
         * +-------+
         * 
         * ARCHITECTURAL OVERVIEW - EXTENDED BEACON RANGE
         * 
         * This mod uses a MultiLoader structure. To facilitate
         * development, the execution flow is explained here:
         * 
         * 1. CORE (Common Module):
         * All logic resides in the 'common' module. Being a 100% Mixin-based
         * mod, we do not use manual registries or platform events.
         * 
         * 2. MIXIN (The Brain):
         * The 'BeaconRangeMixin' file intercepts the Vanilla 'BeaconBlockEntity' class.
         * - Injection Point: Injected into the 'applyEffects' method.
         * - Logic: Captures the local variable that defines the radius (distance) and
         * multiplies it before Minecraft uses it to search for entities.
         * 
         * 3. COMPATIBILITY (Java 21):
         * Following Minecraft 1.21.1+ standards, the project is forced to JAVA_21 in
         * both the build system and the Mixin compatibility level.
         *
         * 4. LOADERS (Fabric / NeoForge):
         * These modules act merely as "containers". They do not contain
         * business logic.
         * Their only function is:
         * - Declare the 'extendedbeaconrange.mixins.json' file in their respective
         * configuration files (neoforge.mods.toml / fabric.mod.json).
         */

        /**
         * +-------+
         * |Español|
         * +-------+
         * 
         * ARCHITECTURAL OVERVIEW - EXTENDED BEACON RANGE
         * 
         * Este mod utiliza una estructura MultiLoader. Para facilitar el
         * desarrollo, aqui se explica el flujo de ejecucion:
         * 
         * 1. NUCLEO (Common Module):
         * Toda la logica reside en el modulo 'common'. Al ser un mod basado 100% en
         * Mixins, no utilizamos registros manuales ni eventos de plataforma.
         * 
         * 2. MIXIN (The Brain):
         * El archivo 'BeaconRangeMixin' intercepta la clase 'BeaconBlockEntity' de
         * Vanilla.
         * - Punto de Inyeccion: Se inyecta en el metodo 'applyEffects'.
         * - Logica: Captura la variable local que define el radio (distancia) y la
         * multiplica antes de que Minecraft la use para buscar entidades.
         * 
         * 3. COMPATIBILIDAD (Java 21):
         * Siguiendo los estandares de Minecraft 1.21.1+, el proyecto esta forzado a
         * JAVA_21 tanto en el sistema de compilacion como en el nivel de compatibilidad
         * del Mixin.
         *
         * 4. LOADERS (Fabric / NeoForge):
         * Estos modulos actuan meramente como "contenedores". No contienen logica de
         * negocio.
         * Su unica funcion es:
         * - Declarar el archivo 'extendedbeaconrange.mixins.json' en sus respectivos
         * archivos de configuracion (neoforge.mods.toml / fabric.mod.json).
         */
    }
}