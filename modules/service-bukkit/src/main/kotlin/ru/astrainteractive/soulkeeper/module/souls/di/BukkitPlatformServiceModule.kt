package ru.astrainteractive.soulkeeper.module.souls.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import org.bukkit.Bukkit
import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.astralibs.lifecycle.LifecyclePlugin
import ru.astrainteractive.astralibs.server.BukkitMinecraftNativeBridge
import ru.astrainteractive.astralibs.server.BukkitPlatformServer
import ru.astrainteractive.astralibs.server.MinecraftNativeBridge
import ru.astrainteractive.astralibs.server.PlatformServer
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.soulkeeper.core.di.BukkitCoreModule
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.domain.BukkitPickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.PacketEventsShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.StubShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.platform.BukkitEffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.platform.BukkitEventProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.BukkitExperienced
import ru.astrainteractive.soulkeeper.module.souls.platform.BukkitIsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.platform.Experienced
import ru.astrainteractive.soulkeeper.module.souls.platform.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.event.EventProvider

object BukkitPlatformServiceScope

@DependencyGraph(BukkitPlatformServiceScope::class)
interface BukkitPlatformServiceModule : PlatformServiceModule {

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Includes coreModule: CoreModule,
            @Includes bukkitCoreModule: BukkitCoreModule,
            @Includes soulsDaoModule: SoulsDaoModule
        ): BukkitPlatformServiceModule
    }

    @SingleIn(BukkitPlatformServiceScope::class)
    @Provides
    fun providePlatformServer(): PlatformServer = BukkitPlatformServer()

    @SingleIn(BukkitPlatformServiceScope::class)
    @Provides
    fun provideEffectEmitter(): EffectEmitter = BukkitEffectEmitter

    @SingleIn(BukkitPlatformServiceScope::class)
    @Provides
    fun provideMinecraftNativeBridge(): MinecraftNativeBridge = BukkitMinecraftNativeBridge()

    @SingleIn(BukkitPlatformServiceScope::class)
    @Provides
    fun provideEventProvider(plugin: LifecyclePlugin): EventProvider = BukkitEventProvider(
        plugin = plugin
    )

    @SingleIn(BukkitPlatformServiceScope::class)
    @Provides
    fun provideIsDeadPlayerProvider(): IsDeadPlayerProvider = BukkitIsDeadPlayerProvider

    @SingleIn(BukkitPlatformServiceScope::class)
    @Provides
    fun provideOnlineMinecraftPlayerExperiencedFactory(): Experienced.Factory<OnlineMinecraftPlayer> =
        BukkitExperienced.OnlineMinecraftPlayerFactory

    @SingleIn(BukkitPlatformServiceScope::class)
    @Provides
    fun provideShowArmorStandUseCase(
        kyoriKrate: CachedKrate<KyoriComponentSerializer>,
        translationKrate: CachedKrate<PluginTranslation>
    ): ShowArmorStandUseCase {
        if (!Bukkit.getPluginManager().isPluginEnabled("packetevents")) {
            return StubShowArmorStandUseCase
        }
        return PacketEventsShowArmorStandUseCase(
            kyoriKrate = kyoriKrate,
            translationKrate = translationKrate
        )
    }
}
