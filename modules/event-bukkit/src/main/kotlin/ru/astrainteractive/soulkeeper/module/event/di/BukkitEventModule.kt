package ru.astrainteractive.soulkeeper.module.event.di

import org.bukkit.event.HandlerList
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.core.di.CoreBukkitModule
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.event.event.SoulEvents
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule

class BukkitEventModule(
    coreModule: CoreModule,
    bukkitCoreModule: CoreBukkitModule,
    soulsDaoModule: SoulsDaoModule
) {
    private val event = SoulEvents(
        soulsDao = soulsDaoModule.soulsDao,
        soulsConfigKrate = coreModule.soulsConfigKrate,
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
