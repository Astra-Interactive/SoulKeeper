package ru.astrainteractive.soulkeeper.core.plugin

import org.bukkit.plugin.java.JavaPlugin
import ru.astrainteractive.astralibs.lifecycle.Lifecycle

abstract class LifecyclePlugin : Lifecycle, JavaPlugin() {
    override fun onEnable() {
        super<JavaPlugin>.onEnable()
        super<Lifecycle>.onEnable()
    }

    override fun onDisable() {
        super<JavaPlugin>.onDisable()
        super<Lifecycle>.onDisable()
    }
}
