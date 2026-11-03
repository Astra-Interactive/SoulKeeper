package ru.astrainteractive.soulkeeper.service.api.armorstand.stub

import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.soulkeeper.data.dao.soul.model.Soul
import ru.astrainteractive.soulkeeper.service.api.armorstand.api.ArmorStandDisplay

object StubArmorStandDisplay : ArmorStandDisplay {
    override fun destroy(player: OnlineKPlayer, ids: Collection<Int>) = Unit

    override fun show(id: Int, player: OnlineKPlayer, soul: Soul) = Unit

    override fun generateEntityId(): Int = -1
}
