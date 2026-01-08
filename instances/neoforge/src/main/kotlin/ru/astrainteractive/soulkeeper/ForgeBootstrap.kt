package ru.astrainteractive.soulkeeper

import net.neoforged.fml.common.Mod

/**
 * Bootstrap class for NeoForge mod initialization.
 * 
 * This class is annotated with @Mod and serves as the entry point for NeoForge.
 * It immediately delegates to ForgeEntryPoint which handles the actual initialization,
 * including loading runtime dependencies.
 */
@Mod("soulkeeper")
class ForgeBootstrap {
    init {
        // Simply create the entry point - it will handle dependency loading internally
        ForgeEntryPoint()
    }
}