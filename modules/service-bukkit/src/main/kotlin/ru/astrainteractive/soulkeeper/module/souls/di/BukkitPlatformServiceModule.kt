package ru.astrainteractive.soulkeeper.module.souls.di

import ru.astrainteractive.astralibs.server.BukkitMinecraftNativeBridge
import ru.astrainteractive.astralibs.server.BukkitPlatformServer
import ru.astrainteractive.astralibs.server.MinecraftNativeBridge
import ru.astrainteractive.astralibs.server.PlatformServer
import ru.astrainteractive.soulkeeper.core.di.BukkitCoreModule
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.souls.domain.BukkitPickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.di.factory.ShowArmorStandUseCaseFactory
import ru.astrainteractive.soulkeeper.module.souls.platform.BukkitEffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.platform.BukkitEventProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.BukkitExperienced
import ru.astrainteractive.soulkeeper.module.souls.platform.BukkitIsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.platform.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.event.EventProvider

class BukkitPlatformServiceModule(
    coreModule: CoreModule,
    bukkitCoreModule: BukkitCoreModule,
    soulsDaoModule: SoulsDaoModule,
) : PlatformServiceModule {
    override val platformServer: PlatformServer = BukkitPlatformServer()
    override val effectEmitter: EffectEmitter = BukkitEffectEmitter
    override val minecraftNativeBridge: MinecraftNativeBridge = BukkitMinecraftNativeBridge()
    override val eventProvider: EventProvider = BukkitEventProvider(
        plugin = bukkitCoreModule.plugin
    )
    override val isDeadPlayerProvider: IsDeadPlayerProvider = BukkitIsDeadPlayerProvider
    override val onlineMinecraftPlayerExperiencedFactory = BukkitExperienced.OnlineMinecraftPlayerFactory

    override val showArmorStandUseCase: ShowArmorStandUseCase = ShowArmorStandUseCaseFactory(coreModule).create()
    override val pickUpItemsUseCase: PickUpItemsUseCase = BukkitPickUpItemsUseCase(
        collectItemSoundProvider = { coreModule.soulsConfigKrate.cachedValue.sounds.collectItem },
        soulsDao = soulsDaoModule.soulsDao,
        dispatchers = coreModule.dispatchers
    )
}
