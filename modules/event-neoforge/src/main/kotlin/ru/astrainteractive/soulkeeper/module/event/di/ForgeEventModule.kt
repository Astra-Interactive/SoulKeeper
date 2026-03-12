package ru.astrainteractive.soulkeeper.module.event.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.StringFormat
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.core.di.qualifier.DataFolder
import ru.astrainteractive.soulkeeper.core.di.qualifier.IoScope
import ru.astrainteractive.soulkeeper.core.di.qualifier.MainScope
import ru.astrainteractive.soulkeeper.core.di.qualifier.YamlFormat
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.module.event.di.qualifier.ForgeEventLifecycle
import ru.astrainteractive.soulkeeper.module.event.event.ForgeSoulEvents
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter
import java.io.File

object ForgeEventScope

@DependencyGraph(ForgeEventScope::class)
interface ForgeEventModule {

    @get:ForgeEventLifecycle
    val lifecycle: Lifecycle

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Includes coreModule: CoreModule,
            @Includes soulsDaoModule: SoulsDaoModule,
            @Provides effectEmitter: EffectEmitter
        ): ForgeEventModule
    }

    @SingleIn(ForgeEventScope::class)
    @ForgeEventLifecycle
    @Provides
    @Suppress("UnusedPrivateProperty")
    fun provideLifecycle(
        soulsDao: SoulsDao,
        soulsConfigKrate: CachedKrate<SoulsConfig>,
        effectEmitter: EffectEmitter,
        @MainScope mainScope: CoroutineScope,
        dispatchers: KotlinDispatchers,
        @DataFolder dataFolder: File,
        @YamlFormat stringFormat: StringFormat,
        @IoScope ioScope: CoroutineScope
    ): Lifecycle {
        @Suppress("UnusedPrivateProperty")
        val event = ForgeSoulEvents(
            soulsDao = soulsDao,
            soulsConfigKrate = soulsConfigKrate,
            effectEmitter = effectEmitter,
            mainScope = mainScope,
            dispatchers = dispatchers,
            dataFolder = dataFolder,
            stringFormat = stringFormat,
            ioScope = ioScope
        )

        return Lifecycle.Lambda(
            onEnable = {
            },
            onDisable = {
            }
        )
    }
}
