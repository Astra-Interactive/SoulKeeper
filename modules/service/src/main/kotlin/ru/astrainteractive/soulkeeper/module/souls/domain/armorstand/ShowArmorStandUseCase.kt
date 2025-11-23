package ru.astrainteractive.soulkeeper.module.souls.domain.armorstand

import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.soulkeeper.module.souls.database.model.Soul

interface ShowArmorStandUseCase {
    fun destroy(
        player: OnlineMinecraftPlayer,
        ids: Collection<Int>
    )

    fun generateEntityId(): Int
    fun show(
        id: Int,
        player: OnlineMinecraftPlayer,
        soul: Soul
    )
}
