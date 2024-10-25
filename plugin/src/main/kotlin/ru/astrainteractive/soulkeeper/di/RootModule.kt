package ru.astrainteractive.soulkeeper.di

import org.bukkit.event.HandlerList
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.command.SoulsCommandRegistry
import ru.astrainteractive.soulkeeper.command.SoulsReloadCommandRegistry
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.core.plugin.LifecyclePlugin
import ru.astrainteractive.soulkeeper.event.SoulEvents
import ru.astrainteractive.soulkeeper.module.souls.database.di.SoulsDbModule
import ru.astrainteractive.soulkeeper.module.souls.domain.di.WorkerModule

interface RootModule {
    val lifecycle: Lifecycle

    class RootModuleImpl(plugin: LifecyclePlugin) : RootModule {
        private val coreModule: CoreModule = CoreModule.Default(plugin)

        private val soulsDbModule = SoulsDbModule.Default(
            dataFolder = coreModule.plugin.dataFolder,
            scope = coreModule.scope
        )

        private val workerModule = WorkerModule(
            coreModule = coreModule,
            soulsDbModule = soulsDbModule
        )

        private val event = SoulEvents(
            soulsDao = soulsDbModule.soulsDao,
            soulsConfigKrate = coreModule.soulsConfigKrate,
            soulCallRenderer = workerModule.soulCallRenderer
        )

        private val soulsCommandRegistry = SoulsCommandRegistry(
            plugin = coreModule.plugin,
            scope = coreModule.scope,
            soulsDao = soulsDbModule.soulsDao,
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
                soulsDbModule.lifecycle,
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
