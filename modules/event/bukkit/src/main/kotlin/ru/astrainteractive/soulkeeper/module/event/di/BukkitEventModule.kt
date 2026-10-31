package ru.astrainteractive.soulkeeper.module.event.di

import org.bukkit.event.HandlerList
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.core.di.BukkitCoreModule
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.data.dao.soul.api.SoulRepository
import ru.astrainteractive.soulkeeper.module.event.event.BukkitSoulEvents

class BukkitEventModule(
    coreModule: CoreModule,
    bukkitCoreModule: BukkitCoreModule,
    soulRepository: SoulRepository
) {
    private val event = BukkitSoulEvents(
        soulRepository = soulRepository,
        clock = coreModule.clock,
        soulsConfigKrate = coreModule.soulsConfigKrate,
        ioScope = coreModule.ioScope,
        dataFolder = coreModule.dataFolder,
        stringFormat = coreModule.yamlFormat,
        effectEmitter = coreModule.effectEmitter
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
