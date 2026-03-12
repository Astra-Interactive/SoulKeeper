package ru.astrainteractive.soulkeeper.command.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import ru.astrainteractive.astralibs.command.registrar.NeoForgeCommandRegistrarContext
import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.soulkeeper.command.di.qualifier.NeoForgeCommandLifecycle
import ru.astrainteractive.soulkeeper.command.di.qualifier.PluginLifecycle
import ru.astrainteractive.soulkeeper.command.reload.SoulsReloadCommandRegistrar
import ru.astrainteractive.soulkeeper.command.souls.SoulsCommandExecutor
import ru.astrainteractive.soulkeeper.command.souls.SoulsListCommandRegistrar
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.core.di.qualifier.IoScope
import ru.astrainteractive.soulkeeper.core.di.qualifier.UnconfinedScope
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule

object NeoForgeCommandScope

@DependencyGraph(NeoForgeCommandScope::class)
interface CommandModule {

    @get:NeoForgeCommandLifecycle
    val lifecycle: Lifecycle

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Includes coreModule: CoreModule,
            @Includes soulsDaoModule: SoulsDaoModule,
            @Provides @PluginLifecycle plugin: Lifecycle
        ): CommandModule
    }

    @SingleIn(NeoForgeCommandScope::class)
    @NeoForgeCommandLifecycle
    @Provides
    fun provideLifecycle(
        @UnconfinedScope unconfinedScope: CoroutineScope,
        kyoriKrate: CachedKrate<KyoriComponentSerializer>,
        @IoScope ioScope: CoroutineScope,
        soulsDao: SoulsDao,
        translationKrate: CachedKrate<PluginTranslation>,
        @PluginLifecycle plugin: Lifecycle
    ): Lifecycle {
        val commandRegistrar = NeoForgeCommandRegistrarContext(unconfinedScope)
        return Lifecycle.Lambda(
            onEnable = {
                SoulsListCommandRegistrar(
                    kyoriKrate = kyoriKrate,
                    registrarContext = commandRegistrar,
                    soulsCommandExecutor = SoulsCommandExecutor(
                        ioScope = ioScope,
                        soulsDao = soulsDao,
                        translationKrate = translationKrate,
                        kyoriKrate = kyoriKrate
                    )
                ).register()
                SoulsReloadCommandRegistrar(
                    plugin = plugin,
                    translationKrate = translationKrate,
                    kyoriKrate = kyoriKrate,
                    registrarContext = commandRegistrar
                ).register()
            },
            onDisable = {}
        )
    }
}
