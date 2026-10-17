package ru.astrainteractive.soulkeeper.module.souls.di

import ru.astrainteractive.astralibs.server.bridge.PlatformServer
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.soulkeeper.core.platform.Experienced
import ru.astrainteractive.soulkeeper.core.platform.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.platform.event.EventProvider
import ru.astrainteractive.soulkeeper.module.souls.soul.api.PlayerInventory

interface PlatformServiceModule {

    val platformServer: PlatformServer
    val eventProvider: EventProvider
    val isDeadPlayerProvider: IsDeadPlayerProvider
    val onlineMinecraftPlayerExperiencedFactory: Experienced.Factory<OnlineKPlayer>

    val showArmorStandUseCase: ShowArmorStandUseCase
    val playerInventory: PlayerInventory
}
