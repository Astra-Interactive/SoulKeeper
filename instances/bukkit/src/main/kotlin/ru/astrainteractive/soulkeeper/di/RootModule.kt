package ru.astrainteractive.soulkeeper.di

import org.bukkit.event.HandlerList
import ru.astrainteractive.astralibs.async.DefaultBukkitDispatchers
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.lifecycle.LifecyclePlugin
import ru.astrainteractive.soulkeeper.command.SoulsCommandRegistry
import ru.astrainteractive.soulkeeper.command.SoulsReloadCommandRegistry
import ru.astrainteractive.soulkeeper.core.di.CoreBukkitModule
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.event.di.BukkitEventModule
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule
import ru.astrainteractive.soulkeeper.module.souls.domain.di.WorkerModule

interface RootModule {
    val lifecycle: Lifecycle

    class RootModuleImpl(plugin: LifecyclePlugin) : RootModule {
        private val coreModule: CoreModule = CoreModule(
            dispatchers = DefaultBukkitDispatchers(plugin),
            dataFolder = plugin.dataFolder
        )
        private val coreBukkitModule = CoreBukkitModule(plugin)

        private val soulsDaoModule = SoulsDaoModule.Default(
            dataFolder = coreModule.dataFolder,
            scope = coreModule.scope
        )

        private val workerModule = WorkerModule(
            coreModule = coreModule,
            soulsDaoModule = soulsDaoModule,
            coreBukkitModule = coreBukkitModule
        )

        private val bukkitEventModule = BukkitEventModule(
            coreModule = coreModule,
            bukkitCoreModule = coreBukkitModule,
            soulsDaoModule = soulsDaoModule
        )

        private val soulsCommandRegistry = SoulsCommandRegistry(
            plugin = coreBukkitModule.plugin,
            scope = coreModule.scope,
            soulsDao = soulsDaoModule.soulsDao,
            kyoriKrate = coreModule.kyoriComponentSerializer,
            translationKrate = coreModule.translation,
        )

        private val soulsReloadCommandRegistry = SoulsReloadCommandRegistry(
            plugin = coreBukkitModule.plugin,
            kyoriKrate = coreModule.kyoriComponentSerializer,
            translationKrate = coreModule.translation
        )

        private val lifecycles: List<Lifecycle>
            get() = listOfNotNull(
                coreModule.lifecycle,
                coreBukkitModule.lifecycle,
                soulsDaoModule.lifecycle,
                bukkitEventModule.lifecycle,
                workerModule.lifecycle,
            )

        override val lifecycle = Lifecycle.Lambda(
            onEnable = {
                soulsCommandRegistry.register()
                soulsReloadCommandRegistry.register()
                lifecycles.forEach(Lifecycle::onEnable)
            },
            onDisable = {
                HandlerList.unregisterAll(coreBukkitModule.plugin)
                lifecycles.forEach(Lifecycle::onDisable)
            },
            onReload = {
                lifecycles.forEach(Lifecycle::onReload)
            }
        )
    }
}
