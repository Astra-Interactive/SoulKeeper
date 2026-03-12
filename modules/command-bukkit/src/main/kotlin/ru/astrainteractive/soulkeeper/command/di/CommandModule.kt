package ru.astrainteractive.soulkeeper.command.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.StringFormat
import ru.astrainteractive.astralibs.command.api.registrar.PaperCommandRegistrarContext
import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.lifecycle.LifecyclePlugin
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.soulkeeper.command.di.qualifier.BukkitCommandLifecycle
import ru.astrainteractive.soulkeeper.command.reload.SoulsReloadCommandRegistrar
import ru.astrainteractive.soulkeeper.command.soulkrate.SoulKrateCommandRegistrar
import ru.astrainteractive.soulkeeper.command.souls.SoulsCommandExecutor
import ru.astrainteractive.soulkeeper.command.souls.SoulsListCommandRegistrar
import ru.astrainteractive.soulkeeper.core.di.BukkitCoreModule
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.core.di.qualifier.DataFolder
import ru.astrainteractive.soulkeeper.core.di.qualifier.IoScope
import ru.astrainteractive.soulkeeper.core.di.qualifier.MainScope
import ru.astrainteractive.soulkeeper.core.di.qualifier.YamlFormat
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule
import java.io.File

object BukkitCommandScope

@DependencyGraph(BukkitCommandScope::class)
interface CommandModule {

    @get:BukkitCommandLifecycle
    val lifecycle: Lifecycle

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Includes coreModule: CoreModule,
            @Includes bukkitCoreModule: BukkitCoreModule,
            @Includes soulsDaoModule: SoulsDaoModule
        ): CommandModule
    }

    @SingleIn(BukkitCommandScope::class)
    @BukkitCommandLifecycle
    @Provides
    fun provideLifecycle(
        @MainScope mainScope: CoroutineScope,
        plugin: LifecyclePlugin,
        kyoriKrate: CachedKrate<KyoriComponentSerializer>,
        @IoScope ioScope: CoroutineScope,
        soulsDao: SoulsDao,
        translationKrate: CachedKrate<PluginTranslation>,
        @YamlFormat yamlFormat: StringFormat,
        @DataFolder dataFolder: File
    ): Lifecycle {
        val paperCommandRegistrar = PaperCommandRegistrarContext(
            mainScope = mainScope,
            plugin = plugin
        )
        return Lifecycle.Lambda(
            onEnable = {
                SoulsListCommandRegistrar(
                    kyoriKrate = kyoriKrate,
                    registrarContext = paperCommandRegistrar,
                    soulsCommandExecutor = SoulsCommandExecutor(
                        ioScope = ioScope,
                        soulsDao = soulsDao,
                        translationKrate = translationKrate,
                        kyoriKrate = kyoriKrate
                    )
                ).register()
                SoulKrateCommandRegistrar(
                    registrarContext = paperCommandRegistrar,
                    stringFormat = yamlFormat,
                    dataFolder = dataFolder,
                    ioScope = ioScope
                ).register()
                SoulsReloadCommandRegistrar(
                    plugin = plugin,
                    translationKrate = translationKrate,
                    kyoriKrate = kyoriKrate,
                    registrarContext = paperCommandRegistrar
                ).register()
            },
            onDisable = {
            }
        )
    }
}
