package ru.astrainteractive.soulkeeper.service.api.platform.model

import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer

data class SharedPlayerMoveEvent(
    val player: OnlineKPlayer,
    val to: KLocation,
)
