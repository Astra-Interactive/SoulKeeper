package ru.astrainteractive.soulkeeper.module.souls.worker.call

import org.bukkit.entity.Player
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul

interface SoulCallRenderer {
    suspend fun rememberSoul(soul: DatabaseSoul)
    suspend fun removeSoul(soul: DatabaseSoul)
    suspend fun updateSoulsForPlayer(player: Player)
    suspend fun displayArmorStands(player: Player)
    suspend fun playSounds(player: Player)
    suspend fun displayParticlesContinuously(player: Player)
    fun clear()
}
