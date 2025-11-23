package ru.astrainteractive.soulkeeper.di

import org.bukkit.event.HandlerList
import ru.astrainteractive.astralibs.async.DefaultBukkitDispatchers
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.lifecycle.LifecyclePlugin
import ru.astrainteractive.soulkeeper.command.di.CommandModule
import ru.astrainteractive.soulkeeper.core.di.BukkitCoreModule
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.event.di.BukkitEventModule
import ru.astrainteractive.soulkeeper.module.souls.di.BukkitPlatformServiceModule
import ru.astrainteractive.soulkeeper.module.souls.di.ServiceModule
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule

class RootModule(plugin: LifecyclePlugin) {
    private val coreModule: CoreModule = CoreModule(
        dispatchers = DefaultBukkitDispatchers(plugin),
        dataFolder = plugin.dataFolder
    )
    private val bukkitCoreModule = BukkitCoreModule(plugin)

    private val soulsDaoModule = SoulsDaoModule.Default(
        dataFolder = coreModule.dataFolder,
        scope = coreModule.ioScope
    )

    private val bukkitPlatformServiceModule = BukkitPlatformServiceModule(
        coreModule = coreModule,
        bukkitCoreModule = bukkitCoreModule,
        soulsDaoModule = soulsDaoModule
    )
    private val serviceModule = ServiceModule(
        coreModule = coreModule,
        soulsDaoModule = soulsDaoModule,
        platformServiceModule = bukkitPlatformServiceModule
    )

    private val bukkitEventModule = BukkitEventModule(
        coreModule = coreModule,
        bukkitCoreModule = bukkitCoreModule,
        soulsDaoModule = soulsDaoModule
    )

    private val commandModule = CommandModule(
        coreModule = coreModule,
        bukkitCoreModule = bukkitCoreModule,
        soulsDaoModule = soulsDaoModule
    )

    private val lifecycles: List<Lifecycle>
        get() = listOfNotNull(
            coreModule.lifecycle,
            bukkitCoreModule.lifecycle,
            soulsDaoModule.lifecycle,
            bukkitEventModule.lifecycle,
            serviceModule.lifecycle,
            commandModule.lifecycle
        )

    val lifecycle = Lifecycle.Lambda(
        onEnable = {
            lifecycles.forEach(Lifecycle::onEnable)
        },
        onDisable = {
            HandlerList.unregisterAll(bukkitCoreModule.plugin)
            lifecycles.forEach(Lifecycle::onDisable)
        },
        onReload = {
            lifecycles.forEach(Lifecycle::onReload)
        }
    )
}
