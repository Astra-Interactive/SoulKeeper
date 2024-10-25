package ru.astrainteractive.soulkeeper

import org.bukkit.plugin.java.JavaPlugin
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.soulkeeper.di.RootModule

class oulKeeper :
    JavaPlugin(),
    Logger by JUtiltLogger("SoulKeeper"),
    Lifecycle {
    private val rootModule by lazy { RootModule.RootModuleImpl(this) }

    override fun onEnable() {
        rootModule.lifecycle.onEnable()
    }

    override fun onDisable() {
        rootModule.lifecycle.onDisable()
    }

    override fun onReload() {
        rootModule.lifecycle.onReload()
    }
}
