package ru.astrainteractive.soulkeeper.module.souls.dao

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.between
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.astralibs.server.location.dist
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.DefaultSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemDatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.StringFormatObject
import ru.astrainteractive.soulkeeper.module.souls.database.table.SoulItemsTable
import ru.astrainteractive.soulkeeper.module.souls.database.table.SoulTable
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

@Suppress("TooManyFunctions")
internal class SoulsDaoImpl(
    private val databaseFlow: Flow<Database>,
    private val dispatchers: KotlinDispatchers
) : SoulsDao, Logger by JUtiltLogger("SoulKeeper-SoulsDaoImpl") {
    private val mutex = Mutex()

    private val soulsChangedSharedFlow = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private suspend fun <T> safeRun(
        tag: String,
        block: suspend () -> T
    ): Result<T> = runCatching {
        withContext(dispatchers.IO) {
            mutex.withLock {
                block.invoke()
            }
        }
    }.onFailure { throwable ->
        if (throwable is CancellationException) throw throwable
        error(throwable) { "#$tag error: ${throwable.message}" }
    }

    private fun findSoulIdsWithItems(soulIds: List<Long>): Set<Long> {
        if (soulIds.isEmpty()) return emptySet()
        return SoulItemsTable.select(SoulItemsTable.soulId)
            .where { SoulItemsTable.soulId inList soulIds }
            .withDistinct()
            .map { row -> row[SoulItemsTable.soulId].value }
            .toSet()
    }

    private fun findSoulItems(soulId: Long): List<StringFormatObject> {
        return SoulItemsTable.selectAll()
            .where { SoulItemsTable.soulId eq soulId }
            .map { row -> row[SoulItemsTable.itemStack] }
    }

    private fun toDatabaseSoul(row: ResultRow, hasItems: Boolean): DatabaseSoul {
        return DatabaseSoul(
            id = row[SoulTable.id].value,
            ownerUUID = UUID.fromString(row[SoulTable.ownerUUID]),
            ownerLastName = row[SoulTable.ownerLastName],
            createdAt = row[SoulTable.created_at],
            isFree = row[SoulTable.isFree],
            exp = row[SoulTable.exp],
            hasItems = hasItems,
            location = KLocation(
                x = row[SoulTable.locationX],
                y = row[SoulTable.locationY],
                z = row[SoulTable.locationZ],
                worldName = row[SoulTable.locationWorld]
            )
        )
    }

    private fun Query.toDatabaseSouls(): List<DatabaseSoul> {
        val rows = toList()
        val soulIdsWithItems = findSoulIdsWithItems(rows.map { row -> row[SoulTable.id].value })
        return rows.map { row ->
            toDatabaseSoul(
                row = row,
                hasItems = row[SoulTable.id].value in soulIdsWithItems
            )
        }
    }

    private fun findSoulUnsafe(id: Long): DatabaseSoul {
        return SoulTable.selectAll()
            .where { SoulTable.id eq id }
            .limit(1)
            .toDatabaseSouls()
            .firstOrNull()
            ?: throw SoulNotFoundException(id)
    }

    override fun getSoulsChangeFlow(): Flow<Unit> {
        return soulsChangedSharedFlow.asSharedFlow()
    }

    override suspend fun getSouls(): Result<List<DatabaseSoul>> = safeRun("getSouls") {
        transaction(databaseFlow.first()) {
            SoulTable.selectAll()
                .orderBy(SoulTable.created_at to SortOrder.DESC)
                .toDatabaseSouls()
        }
    }

    override suspend fun getSoul(id: Long): Result<DatabaseSoul> = safeRun("getSoul") {
        transaction(databaseFlow.first()) {
            findSoulUnsafe(id)
        }
    }

    override suspend fun getPlayerSouls(
        uuid: UUID
    ): Result<List<DatabaseSoul>> = safeRun("getPlayerSouls") {
        transaction(databaseFlow.first()) {
            SoulTable.selectAll()
                .where { SoulTable.ownerUUID.eq(uuid.toString()) }
                .toDatabaseSouls()
        }
    }

    override suspend fun insertSoul(
        soul: DefaultSoul,
    ): Result<DatabaseSoul> = safeRun("insertSoul") {
        transaction(databaseFlow.first()) {
            val soulId = SoulTable.insertAndGetId { statement ->
                statement[SoulTable.ownerUUID] = soul.ownerUUID.toString()
                statement[SoulTable.ownerLastName] = soul.ownerLastName
                statement[SoulTable.created_at] = soul.createdAt
                statement[SoulTable.isFree] = soul.isFree
                statement[SoulTable.locationWorld] = soul.location.worldName
                statement[SoulTable.exp] = soul.exp
                statement[SoulTable.locationX] = soul.location.x
                statement[SoulTable.locationY] = soul.location.y
                statement[SoulTable.locationZ] = soul.location.z
            }

            SoulItemsTable.batchInsert(soul.items) { item ->
                this[SoulItemsTable.soulId] = soulId
                this[SoulItemsTable.itemStack] = item
            }

            findSoulUnsafe(soulId.value)
        }
    }.onSuccess { soulsChangedSharedFlow.emit(Unit) }

    override suspend fun getSoulsNear(
        location: KLocation,
        radius: Int
    ): Result<List<DatabaseSoul>> = safeRun("getSoulsNear") {
        transaction(databaseFlow.first()) {
            SoulTable.selectAll()
                .where { SoulTable.locationWorld.eq(location.worldName) }
                .andWhere { SoulTable.locationX.between(location.x - radius, location.x + radius) }
                .andWhere { SoulTable.locationY.between(location.y - radius, location.y + radius) }
                .andWhere { SoulTable.locationZ.between(location.z - radius, location.z + radius) }
                .toDatabaseSouls()
                .filter { soul -> soul.location.dist(location) < radius }
        }
    }

    override suspend fun deleteSoul(id: Long): Result<Unit> = safeRun("deleteSoul") {
        transaction(databaseFlow.first()) {
            SoulItemsTable.deleteWhere { SoulItemsTable.soulId.eq(id) }
            SoulTable.deleteWhere { SoulTable.id.eq(id) }
        }
    }.onSuccess { soulsChangedSharedFlow.emit(Unit) }.map { }

    override suspend fun updateSoul(soul: DatabaseSoul): Result<Unit> = safeRun("updateSoul") {
        transaction(databaseFlow.first()) {
            SoulTable.update(
                where = { SoulTable.id.eq(soul.id) },
                body = { statement ->
                    statement[SoulTable.isFree] = soul.isFree
                    statement[SoulTable.exp] = soul.exp
                }
            )
        }
    }.onSuccess { soulsChangedSharedFlow.emit(Unit) }.map { }

    override suspend fun updateSoul(soul: ItemDatabaseSoul): Result<Unit> = safeRun("updateSoul") {
        transaction(databaseFlow.first()) {
            SoulTable.update(
                where = { SoulTable.id.eq(soul.id) },
                body = { statement ->
                    statement[SoulTable.isFree] = soul.isFree
                    statement[SoulTable.exp] = soul.exp
                }
            )
            SoulItemsTable.deleteWhere { SoulItemsTable.soulId.eq(soul.id) }
            SoulItemsTable.batchInsert(soul.items) { item ->
                this[SoulItemsTable.soulId] = soul.id
                this[SoulItemsTable.itemStack] = item
            }
        }
    }.onSuccess { soulsChangedSharedFlow.emit(Unit) }.map { }

    override suspend fun toItemDatabaseSoul(
        soul: DatabaseSoul
    ): Result<ItemDatabaseSoul> = safeRun("toItemDatabaseSoul") {
        transaction(databaseFlow.first()) {
            val items = findSoulItems(soul.id)
            ItemDatabaseSoul(
                id = soul.id,
                ownerUUID = soul.ownerUUID,
                ownerLastName = soul.ownerLastName,
                createdAt = soul.createdAt,
                isFree = soul.isFree,
                location = soul.location,
                hasItems = items.isNotEmpty(),
                exp = soul.exp,
                items = items
            )
        }
    }
}
