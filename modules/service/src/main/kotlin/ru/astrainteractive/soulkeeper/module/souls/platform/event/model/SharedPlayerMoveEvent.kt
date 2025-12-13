package ru.astrainteractive.soulkeeper.module.souls.platform.event.model

import ru.astrainteractive.astralibs.server.location.Location
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer

data class SharedPlayerMoveEvent(
    val player: OnlineMinecraftPlayer,
    val to: Location,
)
