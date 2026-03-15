package ru.astrainteractive.soulkeeper.module.souls.di

import ru.astrainteractive.astralibs.server.bridge.PlatformServer
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.soulkeeper.module.souls.domain.AddSoulItemsIntoInventoryUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.platform.Experienced
import ru.astrainteractive.soulkeeper.module.souls.platform.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.event.EventProvider

interface PlatformServiceModule {

    val platformServer: PlatformServer
    val effectEmitter: EffectEmitter
    val eventProvider: EventProvider
    val isDeadPlayerProvider: IsDeadPlayerProvider
    val onlineMinecraftPlayerExperiencedFactory: Experienced.Factory<OnlineKPlayer>

    val showArmorStandUseCase: ShowArmorStandUseCase
    val pickUpItemsUseCase: PickUpItemsUseCase
    val addSoulItemsIntoInventoryUseCase: AddSoulItemsIntoInventoryUseCase
}
