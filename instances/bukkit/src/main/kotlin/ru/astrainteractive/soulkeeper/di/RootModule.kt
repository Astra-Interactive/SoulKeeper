package ru.astrainteractive.soulkeeper.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraphFactory
import org.bukkit.event.HandlerList
import ru.astrainteractive.astralibs.coroutines.DefaultBukkitDispatchers
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.lifecycle.LifecyclePlugin
import ru.astrainteractive.soulkeeper.command.di.CommandModule
import ru.astrainteractive.soulkeeper.core.di.BukkitCoreModule
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.event.di.BukkitEventModule
import ru.astrainteractive.soulkeeper.module.souls.di.BukkitPlatformServiceModule
import ru.astrainteractive.soulkeeper.module.souls.di.ServiceModule
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule

object BukkitRootScope

@DependencyGraph(BukkitRootScope::class)
interface RootModule {

    val lifecycle: Lifecycle

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides plugin: LifecyclePlugin): RootModule
    }

    @SingleIn(BukkitRootScope::class)
    @Provides
    fun provideCoreModule(plugin: LifecyclePlugin): CoreModule =
        createGraphFactory<CoreModule.Factory>().create(
            dispatchers = DefaultBukkitDispatchers(plugin),
            dataFolder = plugin.dataFolder
        )

    @SingleIn(BukkitRootScope::class)
    @Provides
    fun provideBukkitCoreModule(plugin: LifecyclePlugin): BukkitCoreModule =
        createGraphFactory<BukkitCoreModule.Factory>().create(plugin = plugin)

    @SingleIn(BukkitRootScope::class)
    @Provides
    fun provideSoulsDaoModule(coreModule: CoreModule): SoulsDaoModule =
        createGraphFactory<SoulsDaoModule.Factory>().create(
            dataFolder = coreModule.dataFolder,
            ioScope = coreModule.ioScope,
            dispatchers = coreModule.dispatchers
        )

    @SingleIn(BukkitRootScope::class)
    @Provides
    fun provideBukkitPlatformServiceModule(
        coreModule: CoreModule,
        bukkitCoreModule: BukkitCoreModule,
        soulsDaoModule: SoulsDaoModule
    ): BukkitPlatformServiceModule =
        createGraphFactory<BukkitPlatformServiceModule.Factory>().create(
            coreModule = coreModule,
            bukkitCoreModule = bukkitCoreModule,
            soulsDaoModule = soulsDaoModule
        )

    @SingleIn(BukkitRootScope::class)
    @Provides
    fun provideServiceModule(
        coreModule: CoreModule,
        soulsDaoModule: SoulsDaoModule,
        bukkitPlatformServiceModule: BukkitPlatformServiceModule
    ): ServiceModule =
        createGraphFactory<ServiceModule.Factory>().create(
            coreModule = coreModule,
            soulsDaoModule = soulsDaoModule,
            platformServiceModule = bukkitPlatformServiceModule
        )

    @SingleIn(BukkitRootScope::class)
    @Provides
    fun provideBukkitEventModule(
        coreModule: CoreModule,
        bukkitCoreModule: BukkitCoreModule,
        soulsDaoModule: SoulsDaoModule
    ): BukkitEventModule =
        createGraphFactory<BukkitEventModule.Factory>().create(
            coreModule = coreModule,
            bukkitCoreModule = bukkitCoreModule,
            soulsDaoModule = soulsDaoModule
        )

    @SingleIn(BukkitRootScope::class)
    @Provides
    fun provideCommandModule(
        coreModule: CoreModule,
        bukkitCoreModule: BukkitCoreModule,
        soulsDaoModule: SoulsDaoModule
    ): CommandModule =
        createGraphFactory<CommandModule.Factory>().create(
            coreModule = coreModule,
            bukkitCoreModule = bukkitCoreModule,
            soulsDaoModule = soulsDaoModule
        )

    @SingleIn(BukkitRootScope::class)
    @Provides
    fun provideLifecycle(
        coreModule: CoreModule,
        bukkitCoreModule: BukkitCoreModule,
        soulsDaoModule: SoulsDaoModule,
        bukkitEventModule: BukkitEventModule,
        serviceModule: ServiceModule,
        commandModule: CommandModule,
        plugin: LifecyclePlugin
    ): Lifecycle {
        val lifecycles = listOfNotNull(
            coreModule.lifecycle,
            bukkitCoreModule.lifecycle,
            soulsDaoModule.lifecycle,
            bukkitEventModule.lifecycle,
            serviceModule.lifecycle,
            commandModule.lifecycle
        )
        return Lifecycle.Lambda(
            onEnable = {
                lifecycles.forEach(Lifecycle::onEnable)
            },
            onDisable = {
                HandlerList.unregisterAll(plugin)
                lifecycles.forEach(Lifecycle::onDisable)
            },
            onReload = {
                lifecycles.forEach(Lifecycle::onReload)
            }
        )
    }
}
