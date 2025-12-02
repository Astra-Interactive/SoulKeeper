package ru.astrainteractive.soulkeeper.module.souls.di

import ru.astrainteractive.astralibs.server.MinecraftNativeBridge
import ru.astrainteractive.astralibs.server.NeoForgeMinecraftNativeBridge
import ru.astrainteractive.astralibs.server.NeoForgePlatformServer
import ru.astrainteractive.astralibs.server.PlatformServer
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.souls.domain.NeoForgePickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.StubShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.platform.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.NeoForgeEffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.platform.NeoForgeEventProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.NeoForgeExperienced
import ru.astrainteractive.soulkeeper.module.souls.platform.NeoForgeIsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.event.EventProvider

class NeoForgePlatformServiceModule(
    coreModule: CoreModule,
    soulsDaoModule: SoulsDaoModule,
) : PlatformServiceModule {
    override val platformServer: PlatformServer = NeoForgePlatformServer
    override val effectEmitter: EffectEmitter = NeoForgeEffectEmitter()
    override val minecraftNativeBridge: MinecraftNativeBridge = NeoForgeMinecraftNativeBridge()
    override val eventProvider: EventProvider = NeoForgeEventProvider
    override val isDeadPlayerProvider: IsDeadPlayerProvider = NeoForgeIsDeadPlayerProvider

    override val showArmorStandUseCase: ShowArmorStandUseCase = StubShowArmorStandUseCase
    override val onlineMinecraftPlayerExperiencedFactory = NeoForgeExperienced.OnlineMinecraftPlayerFactory
    override val pickUpItemsUseCase: PickUpItemsUseCase = NeoForgePickUpItemsUseCase(
        collectItemSoundProvider = { coreModule.soulsConfigKrate.cachedValue.sounds.collectItem },
        soulsDao = soulsDaoModule.soulsDao,
        effectEmitter = effectEmitter,
        isDeadPlayerProvider = isDeadPlayerProvider
    )
}
