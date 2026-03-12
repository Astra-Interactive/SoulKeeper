package ru.astrainteractive.soulkeeper.module.souls.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import ru.astrainteractive.astralibs.server.MinecraftNativeBridge
import ru.astrainteractive.astralibs.server.NeoForgeMinecraftNativeBridge
import ru.astrainteractive.astralibs.server.NeoForgePlatformServer
import ru.astrainteractive.astralibs.server.PlatformServer
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.domain.NeoForgePickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.StubShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.platform.Experienced
import ru.astrainteractive.soulkeeper.module.souls.platform.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.NeoForgeEffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.platform.NeoForgeEventProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.NeoForgeExperienced
import ru.astrainteractive.soulkeeper.module.souls.platform.NeoForgeIsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.event.EventProvider

object NeoForgePlatformServiceScope

@DependencyGraph(NeoForgePlatformServiceScope::class)
interface NeoForgePlatformServiceModule : PlatformServiceModule {

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Includes coreModule: CoreModule,
            @Includes soulsDaoModule: SoulsDaoModule
        ): NeoForgePlatformServiceModule
    }

    @SingleIn(NeoForgePlatformServiceScope::class)
    @Provides
    fun providePlatformServer(): PlatformServer = NeoForgePlatformServer

    @SingleIn(NeoForgePlatformServiceScope::class)
    @Provides
    fun provideEffectEmitter(): EffectEmitter = NeoForgeEffectEmitter()

    @SingleIn(NeoForgePlatformServiceScope::class)
    @Provides
    fun provideMinecraftNativeBridge(): MinecraftNativeBridge = NeoForgeMinecraftNativeBridge()

    @SingleIn(NeoForgePlatformServiceScope::class)
    @Provides
    fun provideEventProvider(): EventProvider = NeoForgeEventProvider

    @SingleIn(NeoForgePlatformServiceScope::class)
    @Provides
    fun provideIsDeadPlayerProvider(): IsDeadPlayerProvider = NeoForgeIsDeadPlayerProvider

    @SingleIn(NeoForgePlatformServiceScope::class)
    @Provides
    fun provideOnlineMinecraftPlayerExperiencedFactory(): Experienced.Factory<OnlineMinecraftPlayer> =
        NeoForgeExperienced.OnlineMinecraftPlayerFactory

    @SingleIn(NeoForgePlatformServiceScope::class)
    @Provides
    fun provideShowArmorStandUseCase(): ShowArmorStandUseCase = StubShowArmorStandUseCase

    @SingleIn(NeoForgePlatformServiceScope::class)
    @Provides
    fun providePickUpItemsUseCase(
        soulsConfigKrate: CachedKrate<SoulsConfig>,
        soulsDao: SoulsDao,
        effectEmitter: EffectEmitter,
        isDeadPlayerProvider: IsDeadPlayerProvider,
        dispatchers: KotlinDispatchers
    ): PickUpItemsUseCase = NeoForgePickUpItemsUseCase(
        collectItemSoundProvider = { soulsConfigKrate.cachedValue.sounds.collectItem },
        soulsDao = soulsDao,
        effectEmitter = effectEmitter,
        isDeadPlayerProvider = isDeadPlayerProvider,
        dispatchers = dispatchers
    )
}
