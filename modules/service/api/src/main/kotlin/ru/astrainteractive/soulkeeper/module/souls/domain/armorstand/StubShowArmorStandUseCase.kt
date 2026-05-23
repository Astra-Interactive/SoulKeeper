package ru.astrainteractive.soulkeeper.module.souls.domain.armorstand

import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.soulkeeper.module.souls.database.model.Soul

object StubShowArmorStandUseCase : ShowArmorStandUseCase {
    override fun destroy(player: OnlineKPlayer, ids: Collection<Int>) = Unit

    override fun show(id: Int, player: OnlineKPlayer, soul: Soul) = Unit

    override fun generateEntityId(): Int = -1
}
