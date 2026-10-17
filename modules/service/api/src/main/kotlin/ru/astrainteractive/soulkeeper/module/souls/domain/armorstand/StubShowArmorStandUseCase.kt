package ru.astrainteractive.soulkeeper.module.souls.domain.armorstand

import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.soulkeeper.data.dao.soul.model.Soul

object StubShowArmorStandUseCase : ShowArmorStandUseCase {
    override fun destroy(player: OnlineKPlayer, ids: Collection<Int>) = Unit

    override fun show(id: Int, player: OnlineKPlayer, soul: Soul) = Unit

    override fun generateEntityId(): Int = -1
}
