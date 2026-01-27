package ru.astrainteractive.soulkeeper.module.event.di

import org.bukkit.event.HandlerList
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.core.di.BukkitCoreModule
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.event.event.BukkitSoulEvents
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule

class BukkitEventModule(
    coreModule: CoreModule,
    bukkitCoreModule: BukkitCoreModule,
    soulsDaoModule: SoulsDaoModule
) {
    private val event = BukkitSoulEvents(
        soulsDao = soulsDaoModule.soulsDao,
        soulsConfigKrate = coreModule.soulsConfigKrate,
        ioScope = coreModule.ioScope,
        dataFolder = coreModule.dataFolder,
        stringFormat = coreModule.yamlFormat
    )
    val lifecycle = Lifecycle.Lambda(
        onEnable = {
            event.onEnable(bukkitCoreModule.plugin)
        },
        onDisable = {
            event.onDisable()
            HandlerList.unregisterAll(bukkitCoreModule.plugin)
        }
    )
}
