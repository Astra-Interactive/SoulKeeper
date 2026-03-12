package ru.astrainteractive.soulkeeper.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.cancel
import net.neoforged.fml.loading.FMLPaths
import ru.astrainteractive.astralibs.coroutines.NeoForgeMainDispatcher
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.soulkeeper.command.di.CommandModule
import ru.astrainteractive.soulkeeper.command.di.qualifier.PluginLifecycle
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.event.di.ForgeEventModule
import ru.astrainteractive.soulkeeper.module.souls.di.NeoForgePlatformServiceModule
import ru.astrainteractive.soulkeeper.module.souls.di.ServiceModule
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule
import java.io.File

object NeoForgeRootScope

@DependencyGraph(NeoForgeRootScope::class)
interface RootModule {

    val lifecycle: Lifecycle

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides @PluginLifecycle plugin: Lifecycle): RootModule
    }

    @SingleIn(NeoForgeRootScope::class)
    @Provides
    fun provideDataFolder(): File = FMLPaths.CONFIGDIR.get()
        .resolve("SoulKeeper")
        .toAbsolutePath()
        .toFile()
        .also(File::mkdirs)

    @SingleIn(NeoForgeRootScope::class)
    @Provides
    fun provideDispatchers(): KotlinDispatchers = object : KotlinDispatchers {
        override val Main: MainCoroutineDispatcher by lazy { NeoForgeMainDispatcher() }
        override val IO: CoroutineDispatcher = Dispatchers.IO
        override val Default: CoroutineDispatcher = Dispatchers.Default
        override val Unconfined: CoroutineDispatcher = Dispatchers.Unconfined
    }

    @SingleIn(NeoForgeRootScope::class)
    @Provides
    fun provideCoreModule(
        dispatchers: KotlinDispatchers,
        dataFolder: File
    ): CoreModule = createGraphFactory<CoreModule.Factory>().create(
        dispatchers = dispatchers,
        dataFolder = dataFolder
    )

    @SingleIn(NeoForgeRootScope::class)
    @Provides
    fun provideSoulsDaoModule(coreModule: CoreModule): SoulsDaoModule =
        createGraphFactory<SoulsDaoModule.Factory>().create(
            dataFolder = coreModule.dataFolder,
            ioScope = coreModule.ioScope,
            dispatchers = coreModule.dispatchers
        )

    @SingleIn(NeoForgeRootScope::class)
    @Provides
    fun provideNeoForgePlatformServiceModule(
        coreModule: CoreModule,
        soulsDaoModule: SoulsDaoModule
    ): NeoForgePlatformServiceModule =
        createGraphFactory<NeoForgePlatformServiceModule.Factory>().create(
            coreModule = coreModule,
            soulsDaoModule = soulsDaoModule
        )

    @SingleIn(NeoForgeRootScope::class)
    @Provides
    fun provideServiceModule(
        coreModule: CoreModule,
        soulsDaoModule: SoulsDaoModule,
        forgePlatformServiceModule: NeoForgePlatformServiceModule
    ): ServiceModule = createGraphFactory<ServiceModule.Factory>().create(
        coreModule = coreModule,
        soulsDaoModule = soulsDaoModule,
        platformServiceModule = forgePlatformServiceModule
    )

    @SingleIn(NeoForgeRootScope::class)
    @Provides
    fun provideForgeEventModule(
        coreModule: CoreModule,
        soulsDaoModule: SoulsDaoModule,
        forgePlatformServiceModule: NeoForgePlatformServiceModule
    ): ForgeEventModule = createGraphFactory<ForgeEventModule.Factory>().create(
        coreModule = coreModule,
        soulsDaoModule = soulsDaoModule,
        effectEmitter = forgePlatformServiceModule.effectEmitter
    )

    @SingleIn(NeoForgeRootScope::class)
    @Provides
    fun provideCommandModule(
        coreModule: CoreModule,
        soulsDaoModule: SoulsDaoModule,
        @PluginLifecycle plugin: Lifecycle
    ): CommandModule = createGraphFactory<CommandModule.Factory>().create(
        coreModule = coreModule,
        soulsDaoModule = soulsDaoModule,
        plugin = plugin
    )

    @SingleIn(NeoForgeRootScope::class)
    @Provides
    fun provideLifecycle(
        coreModule: CoreModule,
        soulsDaoModule: SoulsDaoModule,
        forgeEventModule: ForgeEventModule,
        serviceModule: ServiceModule,
        commandModule: CommandModule
    ): Lifecycle {
        val lifecycles = listOfNotNull(
            coreModule.lifecycle,
            soulsDaoModule.lifecycle,
            forgeEventModule.lifecycle,
            serviceModule.lifecycle,
            commandModule.lifecycle
        )
        return Lifecycle.Lambda(
            onEnable = {
                lifecycles.forEach(Lifecycle::onEnable)
            },
            onDisable = {
                lifecycles.forEach(Lifecycle::onDisable)
                coreModule.unconfinedScope.cancel()
            },
            onReload = {
                lifecycles.forEach(Lifecycle::onReload)
            }
        )
    }
}
