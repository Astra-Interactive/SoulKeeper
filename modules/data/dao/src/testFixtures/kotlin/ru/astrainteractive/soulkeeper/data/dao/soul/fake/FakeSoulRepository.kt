package ru.astrainteractive.soulkeeper.data.dao.soul.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.soulkeeper.data.dao.soul.api.SoulRepository
import ru.astrainteractive.soulkeeper.data.dao.soul.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.data.dao.soul.model.DefaultSoul
import ru.astrainteractive.soulkeeper.data.dao.soul.model.ItemDatabaseSoul
import java.util.UUID

class FakeSoulRepository : SoulRepository {
    var soulsNear: List<DatabaseSoul> = emptyList()
    var soulsNearFailure: Throwable? = null

    val updatedSouls = mutableListOf<ItemDatabaseSoul>()
    val deletedSoulIds = mutableListOf<Long>()

    private fun notUsed(): Nothing = error("The tests do not call this")

    override fun getSoulsChangeFlow(): Flow<Unit> = emptyFlow()

    override suspend fun getSouls(): Result<List<DatabaseSoul>> = notUsed()

    override suspend fun getSoul(id: Long): Result<DatabaseSoul> = notUsed()

    override suspend fun getPlayerSouls(uuid: UUID): Result<List<DatabaseSoul>> = notUsed()

    override suspend fun insertSoul(soul: DefaultSoul): Result<DatabaseSoul> = notUsed()

    override suspend fun getSoulsNear(location: KLocation, radius: Int): Result<List<DatabaseSoul>> {
        val failure = soulsNearFailure ?: return Result.success(soulsNear)
        return Result.failure(failure)
    }

    override suspend fun deleteSoul(id: Long): Result<Unit> {
        deletedSoulIds += id
        return Result.success(Unit)
    }

    override suspend fun updateSoul(soul: DatabaseSoul): Result<Unit> = notUsed()

    override suspend fun updateSoul(soul: ItemDatabaseSoul): Result<Unit> {
        updatedSouls += soul
        return Result.success(Unit)
    }

    override suspend fun toItemDatabaseSoul(soul: DatabaseSoul): Result<ItemDatabaseSoul> = notUsed()
}
