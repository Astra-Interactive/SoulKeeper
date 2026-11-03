package ru.astrainteractive.soulkeeper.data.dao.soul.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.soulkeeper.data.dao.soul.api.SoulRepository
import ru.astrainteractive.soulkeeper.data.dao.soul.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.data.dao.soul.model.DefaultSoul
import ru.astrainteractive.soulkeeper.data.dao.soul.model.ItemDatabaseSoul
import ru.astrainteractive.soulkeeper.data.dao.soul.model.SoulId
import java.util.UUID

class FakeSoulRepository : SoulRepository {
    override val soulsChangeFlow: Flow<Unit> = emptyFlow()

    var soulsNear: List<DatabaseSoul> = emptyList()
    var soulsNearFailure: Throwable? = null

    val updatedSouls = mutableListOf<ItemDatabaseSoul>()
    val deletedSoulIds = mutableListOf<SoulId>()

    private fun notUsed(): Nothing = error("The tests do not call this")

    override suspend fun loadSouls(): Result<List<DatabaseSoul>> = notUsed()

    override suspend fun loadSoul(id: SoulId): Result<DatabaseSoul> = notUsed()

    override suspend fun loadPlayerSouls(uuid: UUID): Result<List<DatabaseSoul>> = notUsed()

    override suspend fun insertSoul(soul: DefaultSoul): Result<DatabaseSoul> = notUsed()

    override suspend fun loadSoulsNear(location: KLocation, radius: Int): Result<List<DatabaseSoul>> {
        val failure = soulsNearFailure ?: return Result.success(soulsNear)
        return Result.failure(failure)
    }

    override suspend fun deleteSoul(id: SoulId): Result<Unit> {
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
