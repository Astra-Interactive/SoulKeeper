package ru.astrainteractive.soulkeeper.data.dao.soul.api

import kotlinx.coroutines.flow.Flow
import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.soulkeeper.data.dao.soul.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.data.dao.soul.model.DefaultSoul
import ru.astrainteractive.soulkeeper.data.dao.soul.model.ItemDatabaseSoul
import ru.astrainteractive.soulkeeper.data.dao.soul.model.SoulId
import java.util.UUID

interface SoulRepository {
    val soulsChangeFlow: Flow<Unit>

    suspend fun loadSouls(): Result<List<DatabaseSoul>>
    suspend fun loadSoul(id: SoulId): Result<DatabaseSoul>

    suspend fun loadPlayerSouls(uuid: UUID): Result<List<DatabaseSoul>>

    suspend fun insertSoul(soul: DefaultSoul): Result<DatabaseSoul>

    suspend fun loadSoulsNear(location: KLocation, radius: Int): Result<List<DatabaseSoul>>

    suspend fun deleteSoul(id: SoulId): Result<Unit>

    suspend fun updateSoul(soul: DatabaseSoul): Result<Unit>

    suspend fun updateSoul(soul: ItemDatabaseSoul): Result<Unit>

    suspend fun toItemDatabaseSoul(soul: DatabaseSoul): Result<ItemDatabaseSoul>
}
