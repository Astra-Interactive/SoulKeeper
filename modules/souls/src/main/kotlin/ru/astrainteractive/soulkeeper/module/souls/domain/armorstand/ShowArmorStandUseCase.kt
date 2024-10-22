package ru.astrainteractive.soulkeeper.module.souls.domain.armorstand

import org.bukkit.entity.Player
import ru.astrainteractive.soulkeeper.module.souls.database.model.Soul

internal interface ShowArmorStandUseCase {
    fun destroy(player: Player, ids: Collection<Int>)
    fun invoke(id: Int, player: Player, soul: Soul)
    fun generateEntityId(): Int
}
