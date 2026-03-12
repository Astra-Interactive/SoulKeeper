package ru.astrainteractive.soulkeeper.module.event.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.StringFormat
import org.bukkit.event.HandlerList
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.lifecycle.LifecyclePlugin
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.soulkeeper.core.di.BukkitCoreModule
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.core.di.qualifier.DataFolder
import ru.astrainteractive.soulkeeper.core.di.qualifier.IoScope
import ru.astrainteractive.soulkeeper.core.di.qualifier.YamlFormat
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.module.event.di.qualifier.BukkitEventLifecycle
import ru.astrainteractive.soulkeeper.module.event.event.BukkitSoulEvents
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule
import java.io.File

object BukkitEventScope

@DependencyGraph(BukkitEventScope::class)
interface BukkitEventModule {

    @get:BukkitEventLifecycle
    val lifecycle: Lifecycle

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Includes coreModule: CoreModule,
            @Includes bukkitCoreModule: BukkitCoreModule,
            @Includes soulsDaoModule: SoulsDaoModule
        ): BukkitEventModule
    }

    @SingleIn(BukkitEventScope::class)
    @BukkitEventLifecycle
    @Provides
    fun provideLifecycle(
        soulsDao: SoulsDao,
        soulsConfigKrate: CachedKrate<SoulsConfig>,
        @IoScope ioScope: CoroutineScope,
        @DataFolder dataFolder: File,
        @YamlFormat stringFormat: StringFormat,
        plugin: LifecyclePlugin
    ): Lifecycle {
        val event = BukkitSoulEvents(
            soulsDao = soulsDao,
            soulsConfigKrate = soulsConfigKrate,
            ioScope = ioScope,
            dataFolder = dataFolder,
            stringFormat = stringFormat
        )
        return Lifecycle.Lambda(
            onEnable = {
                event.onEnable(plugin)
            },
            onDisable = {
                event.onDisable()
                HandlerList.unregisterAll(plugin)
            }
        )
    }
}
