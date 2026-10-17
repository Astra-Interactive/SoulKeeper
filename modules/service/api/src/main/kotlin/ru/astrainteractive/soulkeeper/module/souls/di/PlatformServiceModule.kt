package ru.astrainteractive.soulkeeper.module.souls.di

import ru.astrainteractive.astralibs.server.bridge.PlatformServer
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.soulkeeper.core.platform.api.Experienced
import ru.astrainteractive.soulkeeper.core.platform.api.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.armorstand.api.ArmorStandDisplay
import ru.astrainteractive.soulkeeper.module.souls.platform.api.EventProvider
import ru.astrainteractive.soulkeeper.module.souls.soul.api.PlayerInventory

interface PlatformServiceModule {

    val platformServer: PlatformServer
    val eventProvider: EventProvider
    val isDeadPlayerProvider: IsDeadPlayerProvider
    val onlineMinecraftPlayerExperiencedFactory: Experienced.Factory<OnlineKPlayer>

    val armorStandDisplay: ArmorStandDisplay
    val playerInventory: PlayerInventory
}
