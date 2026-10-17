package ru.astrainteractive.soulkeeper.module.souls.armorstand.api

import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.soulkeeper.data.dao.soul.model.Soul

interface ArmorStandDisplay {
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
