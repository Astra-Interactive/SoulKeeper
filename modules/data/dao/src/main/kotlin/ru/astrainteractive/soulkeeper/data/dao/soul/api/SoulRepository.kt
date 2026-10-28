@file:Suppress("FunctionVerb")

package ru.astrainteractive.soulkeeper.data.dao.soul.api

import kotlinx.coroutines.flow.Flow
import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.soulkeeper.data.dao.soul.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.data.dao.soul.model.DefaultSoul
import ru.astrainteractive.soulkeeper.data.dao.soul.model.ItemDatabaseSoul
import java.util.UUID

interface SoulRepository {
    fun getSoulsChangeFlow(): Flow<Unit>

    suspend fun getSouls(): Result<List<DatabaseSoul>>
    suspend fun getSoul(id: Long): Result<DatabaseSoul>

    suspend fun getPlayerSouls(uuid: UUID): Result<List<DatabaseSoul>>

    suspend fun insertSoul(soul: DefaultSoul): Result<DatabaseSoul>

    suspend fun getSoulsNear(location: KLocation, radius: Int): Result<List<DatabaseSoul>>

    suspend fun deleteSoul(id: Long): Result<Unit>

    suspend fun updateSoul(soul: DatabaseSoul): Result<Unit>

    suspend fun updateSoul(soul: ItemDatabaseSoul): Result<Unit>

    suspend fun toItemDatabaseSoul(soul: DatabaseSoul): Result<ItemDatabaseSoul>
}
