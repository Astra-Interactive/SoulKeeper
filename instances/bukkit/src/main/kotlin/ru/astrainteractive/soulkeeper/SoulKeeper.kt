package ru.astrainteractive.soulkeeper

import ru.astrainteractive.astralibs.lifecycle.LifecyclePlugin
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.di.RootModule

class SoulKeeper :
    LifecyclePlugin(),
    Logger by JUtiltLogger("SoulKeeper") {
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
