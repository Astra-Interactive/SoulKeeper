package ru.astrainteractive.soulkeeper.di

import org.bukkit.event.HandlerList
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.lifecycle.LifecyclePlugin
import ru.astrainteractive.soulkeeper.command.SoulsCommandRegistry
import ru.astrainteractive.soulkeeper.command.SoulsReloadCommandRegistry
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.event.SoulEvents
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule
import ru.astrainteractive.soulkeeper.module.souls.domain.di.WorkerModule

interface RootModule {
    val lifecycle: Lifecycle

    class RootModuleImpl(plugin: LifecyclePlugin) : RootModule {
        private val coreModule: CoreModule = CoreModule.Default(plugin)

        private val soulsDaoModule = SoulsDaoModule.Default(
            dataFolder = coreModule.plugin.dataFolder,
            scope = coreModule.scope
        )

        private val workerModule = WorkerModule(
            coreModule = coreModule,
            soulsDaoModule = soulsDaoModule
        )

        private val event = SoulEvents(
            soulsDao = soulsDaoModule.soulsDao,
            soulsConfigKrate = coreModule.soulsConfigKrate,
            soulCallRenderer = workerModule.soulCallRenderer
        )

        private val soulsCommandRegistry = SoulsCommandRegistry(
            plugin = coreModule.plugin,
            scope = coreModule.scope,
            soulsDao = soulsDaoModule.soulsDao,
            kyoriKrate = coreModule.kyoriComponentSerializer,
            translationKrate = coreModule.translation,
            soulCallRenderer = workerModule.soulCallRenderer
        )

        private val soulsReloadCommandRegistry = SoulsReloadCommandRegistry(
            plugin = coreModule.plugin,
            kyoriKrate = coreModule.kyoriComponentSerializer,
            translationKrate = coreModule.translation
        )

        private val lifecycles: List<Lifecycle>
            get() = listOfNotNull(
                coreModule.lifecycle,
                soulsDaoModule.lifecycle,
                workerModule.lifecycle,
            )

        override val lifecycle = Lifecycle.Lambda(
            onEnable = {
                event.onEnable(coreModule.plugin)
                soulsCommandRegistry.register()
                soulsReloadCommandRegistry.register()
                lifecycles.forEach(Lifecycle::onEnable)
            },
            onDisable = {
                event.onDisable()
                HandlerList.unregisterAll(coreModule.plugin)
                lifecycles.forEach(Lifecycle::onDisable)
            },
            onReload = {
                lifecycles.forEach(Lifecycle::onReload)
            }
        )
    }
}
