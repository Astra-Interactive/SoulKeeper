package ru.astrainteractive.soulkeeper.module.souls.di

import ru.astrainteractive.astralibs.server.BukkitMinecraftNativeBridge
import ru.astrainteractive.astralibs.server.BukkitPlatformServer
import ru.astrainteractive.astralibs.server.MinecraftNativeBridge
import ru.astrainteractive.astralibs.server.PlatformServer
import ru.astrainteractive.soulkeeper.core.di.BukkitCoreModule
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.souls.domain.BukkitPickUpExpUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.BukkitPickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpExpUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.di.factory.ShowArmorStandUseCaseFactory
import ru.astrainteractive.soulkeeper.module.souls.server.BukkitEffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.server.BukkitEventProvider
import ru.astrainteractive.soulkeeper.module.souls.server.BukkitIsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.server.EffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.server.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.server.event.EventProvider

class BukkitPlatformServiceModule(
    coreModule: CoreModule,
    bukkitCoreModule: BukkitCoreModule,
    soulsDaoModule: SoulsDaoModule,
) : PlatformServiceModule {
    override val showArmorStandUseCase: ShowArmorStandUseCase = ShowArmorStandUseCaseFactory(coreModule).create()
    override val pickUpExpUseCase: PickUpExpUseCase = BukkitPickUpExpUseCase(
        collectXpSoundProvider = { coreModule.soulsConfigKrate.cachedValue.sounds.collectXp },
        soulsDao = soulsDaoModule.soulsDao
    )
    override val pickUpItemsUseCase: PickUpItemsUseCase = BukkitPickUpItemsUseCase(
        collectItemSoundProvider = { coreModule.soulsConfigKrate.cachedValue.sounds.collectItem },
        soulsDao = soulsDaoModule.soulsDao
    )
    override val isDeadPlayerProvider: IsDeadPlayerProvider = BukkitIsDeadPlayerProvider
    override val platformServer: PlatformServer = BukkitPlatformServer()
    override val effectEmitter: EffectEmitter = BukkitEffectEmitter
    override val minecraftNativeBridge: MinecraftNativeBridge = BukkitMinecraftNativeBridge()
    override val eventProvider: EventProvider = BukkitEventProvider(
        plugin = bukkitCoreModule.plugin
    )
}
