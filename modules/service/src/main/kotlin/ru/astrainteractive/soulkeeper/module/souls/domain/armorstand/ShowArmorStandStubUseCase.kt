package ru.astrainteractive.soulkeeper.module.souls.domain.armorstand

import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.soulkeeper.module.souls.database.model.Soul

object ShowArmorStandStubUseCase : ShowArmorStandUseCase {
    override fun destroy(player: OnlineMinecraftPlayer, ids: Collection<Int>) = Unit

    override fun show(id: Int, player: OnlineMinecraftPlayer, soul: Soul) = Unit

    override fun generateEntityId(): Int = -1
}
