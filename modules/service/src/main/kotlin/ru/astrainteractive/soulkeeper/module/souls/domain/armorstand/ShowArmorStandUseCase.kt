package ru.astrainteractive.soulkeeper.module.souls.domain.armorstand

import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.soulkeeper.module.souls.database.model.Soul

interface ShowArmorStandUseCase {
    fun destroy(
        player: OnlineKPlayer,
        ids: Collection<Int>
    )

    fun generateEntityId(): Int
    fun show(
        id: Int,
        player: OnlineKPlayer,
        soul: Soul
    )
}
