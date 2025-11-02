package ru.astrainteractive.soulkeeper.module.souls.renderer.api

import org.bukkit.entity.Player
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul

interface SoulEffectRenderer {
    suspend fun renderOnce(player: Player, souls: List<DatabaseSoul>)
}
