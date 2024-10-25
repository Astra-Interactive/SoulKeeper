package ru.astrainteractive.soulkeeper.module.souls.database.dao

import org.bukkit.Location
import ru.astrainteractive.soulkeeper.module.souls.database.model.BukkitSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.Soul
import java.util.UUID

interface SoulsDao {
    suspend fun getSouls(): Result<List<DatabaseSoul>>

    suspend fun getPlayerSouls(uuid: UUID): Result<List<DatabaseSoul>>

    suspend fun insertSoul(soul: BukkitSoul): Result<DatabaseSoul>

    suspend fun getSoulsNear(location: Location, radius: Int): Result<List<DatabaseSoul>>

    suspend fun deleteSoul(soul: Soul): Result<Unit>

    suspend fun updateSoul(soul: Soul): Result<Unit>

    suspend fun updateSoul(soul: BukkitSoul): Result<Unit>

    suspend fun toItemStackSoul(soul: Soul): Result<BukkitSoul>
}
