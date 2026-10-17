package ru.astrainteractive.soulkeeper.di

import org.bukkit.event.HandlerList
import ru.astrainteractive.astralibs.command.api.brigadier.command.MultiplatformCommand
import ru.astrainteractive.astralibs.command.api.brigadier.command.PaperMultiplatformCommands
import ru.astrainteractive.astralibs.command.api.registrar.PaperCommandRegistrarContext
import ru.astrainteractive.astralibs.coroutines.DefaultBukkitDispatchers
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.lifecycle.LifecyclePlugin
import ru.astrainteractive.soulkeeper.command.di.CommandModule
import ru.astrainteractive.soulkeeper.core.di.BukkitCoreModule
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.core.platform.BukkitEffectEmitter
import ru.astrainteractive.soulkeeper.data.exposed.di.SoulsDaoModule
import ru.astrainteractive.soulkeeper.module.event.di.BukkitEventModule
import ru.astrainteractive.soulkeeper.module.souls.di.BukkitPlatformSoulsModule
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsModule

class RootModule(plugin: LifecyclePlugin) {
    private val coreModule: CoreModule = CoreModule(
        dispatchers = DefaultBukkitDispatchers(plugin),
        dataFolder = plugin.dataFolder,
        effectEmitter = BukkitEffectEmitter,
        multiplatformCommand = MultiplatformCommand(PaperMultiplatformCommands())
    )
    private val bukkitCoreModule = BukkitCoreModule(plugin)

    private val soulsDaoModule = SoulsDaoModule.Default(
        dataFolder = coreModule.dataFolder,
        ioScope = coreModule.ioScope,
        dispatchers = coreModule.dispatchers
    )

    private val bukkitPlatformSoulsModule = BukkitPlatformSoulsModule(
        coreModule = coreModule,
        bukkitCoreModule = bukkitCoreModule
    )
    private val soulsModule = SoulsModule(
        coreModule = coreModule,
        soulRepository = soulsDaoModule.soulRepository,
        platformSoulsModule = bukkitPlatformSoulsModule
    )

    private val bukkitEventModule = BukkitEventModule(
        coreModule = coreModule,
        bukkitCoreModule = bukkitCoreModule,
        soulRepository = soulsDaoModule.soulRepository
    )

    private val commandModule = CommandModule(
        coreModule = coreModule,
        commandRegistrarContext = PaperCommandRegistrarContext(
            mainScope = coreModule.unconfinedScope,
            plugin = plugin
        ),
        soulRepository = soulsDaoModule.soulRepository,
        soulsModule = soulsModule,
        lifecyclePlugin = plugin,
    )

    private val lifecycles: List<Lifecycle>
        get() = listOfNotNull(
            coreModule.lifecycle,
            bukkitCoreModule.lifecycle,
            soulsDaoModule.lifecycle,
            bukkitEventModule.lifecycle,
            soulsModule.lifecycle,
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
