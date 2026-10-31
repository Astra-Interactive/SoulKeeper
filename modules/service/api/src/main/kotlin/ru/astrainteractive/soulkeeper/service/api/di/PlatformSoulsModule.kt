package ru.astrainteractive.soulkeeper.service.api.di

import ru.astrainteractive.astralibs.server.bridge.PlatformServer
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.soulkeeper.core.platform.api.Experienced
import ru.astrainteractive.soulkeeper.core.platform.api.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.service.api.armorstand.api.ArmorStandDisplay
import ru.astrainteractive.soulkeeper.service.api.platform.api.EventProvider
import ru.astrainteractive.soulkeeper.service.api.soul.api.PlayerInventory

interface PlatformSoulsModule {

    val platformServer: PlatformServer
    val eventProvider: EventProvider
    val isDeadPlayerProvider: IsDeadPlayerProvider
    val onlineMinecraftPlayerExperiencedFactory: Experienced.Resolver<OnlineKPlayer>

    val armorStandDisplay: ArmorStandDisplay
    val playerInventory: PlayerInventory
}
