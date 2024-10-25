package ru.astrainteractive.soulkeeper.module.souls.domain.armorstand

import org.bukkit.entity.Player
import ru.astrainteractive.soulkeeper.module.souls.database.model.Soul

internal object ShowArmorStandStubUseCase : ShowArmorStandUseCase {
    override fun destroy(player: Player, ids: Collection<Int>) = Unit

    override fun show(id: Int, player: Player, soul: Soul) = Unit

    override fun generateEntityId(): Int = -1
}
