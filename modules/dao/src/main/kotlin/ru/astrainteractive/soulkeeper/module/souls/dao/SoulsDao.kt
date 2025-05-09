package ru.astrainteractive.soulkeeper.module.souls.dao

import kotlinx.coroutines.flow.Flow
import org.bukkit.Location
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.io.model.BukkitSoul
import ru.astrainteractive.soulkeeper.module.souls.io.model.Soul
import java.util.UUID

interface SoulsDao {
    fun getSoulsChangeFlow(): Flow<Unit>

    suspend fun getSouls(): Result<List<DatabaseSoul>>

    suspend fun getPlayerSouls(uuid: UUID): Result<List<DatabaseSoul>>

    suspend fun insertSoul(soul: BukkitSoul): Result<DatabaseSoul>

    suspend fun getSoulsNear(location: Location, radius: Int): Result<List<DatabaseSoul>>

    suspend fun deleteSoul(soul: Soul): Result<Unit>

    suspend fun updateSoul(soul: Soul): Result<Unit>

    suspend fun updateSoul(soul: BukkitSoul): Result<Unit>

    suspend fun toItemStackSoul(soul: Soul): Result<BukkitSoul>
}
