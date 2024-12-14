package ru.astrainteractive.soulkeeper.module.souls.domain.armorstand

import org.bukkit.entity.Player
import ru.astrainteractive.soulkeeper.module.souls.io.model.Soul

internal interface ShowArmorStandUseCase {
    fun destroy(player: Player, ids: Collection<Int>)
    fun generateEntityId(): Int
    fun show(id: Int, player: Player, soul: Soul)
}
