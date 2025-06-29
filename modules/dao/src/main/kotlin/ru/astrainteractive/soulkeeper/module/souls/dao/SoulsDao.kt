package ru.astrainteractive.soulkeeper.module.souls.dao

import kotlinx.coroutines.flow.Flow
import ru.astrainteractive.soulkeeper.module.souls.database.model.*
import java.util.*

interface SoulsDao {
    fun getSoulsChangeFlow(): Flow<Unit>

    suspend fun getSouls(): Result<List<DatabaseSoul>>

    suspend fun getPlayerSouls(uuid: UUID): Result<List<DatabaseSoul>>

    suspend fun insertSoul(soul: DefaultSoul): Result<DatabaseSoul>

    suspend fun getSoulsNear(location: Location, radius: Int): Result<List<DatabaseSoul>>

    suspend fun deleteSoul(id: Long): Result<Unit>

    suspend fun updateSoul(soul: DatabaseSoul): Result<Unit>

    suspend fun updateSoul(soul: ItemDatabaseSoul): Result<Unit>

    suspend fun toItemDatabaseSoul(soul: DatabaseSoul): Result<ItemDatabaseSoul>
}
