package ru.astrainteractive.soulkeeper.module.souls.di

import ru.astrainteractive.astralibs.server.MinecraftNativeBridge
import ru.astrainteractive.astralibs.server.PlatformServer
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.platform.Experienced
import ru.astrainteractive.soulkeeper.module.souls.platform.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.platform.event.EventProvider

interface PlatformServiceModule {

    val platformServer: PlatformServer
    val effectEmitter: EffectEmitter
    val minecraftNativeBridge: MinecraftNativeBridge
    val eventProvider: EventProvider
    val isDeadPlayerProvider: IsDeadPlayerProvider
    val onlineMinecraftPlayerExperiencedFactory: Experienced.Factory<OnlineMinecraftPlayer>

    val showArmorStandUseCase: ShowArmorStandUseCase
    val pickUpItemsUseCase: PickUpItemsUseCase
}
