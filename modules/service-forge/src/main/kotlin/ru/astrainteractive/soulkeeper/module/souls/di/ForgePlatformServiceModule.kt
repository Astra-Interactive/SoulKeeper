package ru.astrainteractive.soulkeeper.module.souls.di

import ru.astrainteractive.astralibs.server.ForgeMinecraftNativeBridge
import ru.astrainteractive.astralibs.server.ForgePlatformServer
import ru.astrainteractive.astralibs.server.MinecraftNativeBridge
import ru.astrainteractive.astralibs.server.PlatformServer
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.souls.domain.ForgePickUpExpUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.ForgePickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpExpUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.StubShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.platform.ForgeEffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.platform.ForgeEventProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.ForgeIsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.event.EventProvider

class ForgePlatformServiceModule(
    coreModule: CoreModule,
    soulsDaoModule: SoulsDaoModule,
) : PlatformServiceModule {
    override val platformServer: PlatformServer = ForgePlatformServer
    override val effectEmitter: EffectEmitter = ForgeEffectEmitter()
    override val minecraftNativeBridge: MinecraftNativeBridge = ForgeMinecraftNativeBridge()
    override val eventProvider: EventProvider = ForgeEventProvider
    override val isDeadPlayerProvider: IsDeadPlayerProvider = ForgeIsDeadPlayerProvider

    override val showArmorStandUseCase: ShowArmorStandUseCase = StubShowArmorStandUseCase
    override val pickUpExpUseCase: PickUpExpUseCase = ForgePickUpExpUseCase(
        collectXpSoundProvider = { coreModule.soulsConfigKrate.cachedValue.sounds.collectXp },
        soulsDao = soulsDaoModule.soulsDao,
        effectEmitter = effectEmitter
    )
    override val pickUpItemsUseCase: PickUpItemsUseCase = ForgePickUpItemsUseCase(
        collectItemSoundProvider = { coreModule.soulsConfigKrate.cachedValue.sounds.collectItem },
        soulsDao = soulsDaoModule.soulsDao,
        effectEmitter = effectEmitter
    )
}
