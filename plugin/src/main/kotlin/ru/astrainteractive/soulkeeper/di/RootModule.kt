package ru.astrainteractive.soulkeeper.di

import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.command.SoulsCommandRegistry
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.event.SoulEvents
import ru.astrainteractive.soulkeeper.module.souls.database.di.SoulsDbModule
import ru.astrainteractive.soulkeeper.module.souls.domain.di.WorkerModule

interface RootModule {
    val lifecycle: Lifecycle

    class RootModuleImpl(plugin: JavaPlugin) : RootModule {
        private val coreModule: CoreModule = CoreModule.Default(plugin)

        private val soulsDbModule = SoulsDbModule.Default(
            dataFolder = coreModule.plugin.dataFolder,
            scope = coreModule.scope
        )

        private val event = SoulEvents(
            soulsDao = soulsDbModule.soulsDao,
            soulsConfigKrate = coreModule.soulsConfigKrate
        )

        private val soulsCommandRegistry = SoulsCommandRegistry(
            plugin = coreModule.plugin,
            scope = coreModule.scope,
            soulsDao = soulsDbModule.soulsDao,
            kyoriKrate = coreModule.kyoriComponentSerializer,
            translationKrate = coreModule.translation
        )

        private val workerModule = WorkerModule(
            coreModule = coreModule,
            soulsDbModule = soulsDbModule
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
