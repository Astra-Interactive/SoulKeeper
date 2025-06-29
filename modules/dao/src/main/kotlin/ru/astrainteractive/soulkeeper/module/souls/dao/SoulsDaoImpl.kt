package ru.astrainteractive.soulkeeper.module.souls.dao

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.astralibs.server.location.Location
import ru.astrainteractive.astralibs.server.location.dist
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.DefaultSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemDatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.database.table.SoulItemsTable
import ru.astrainteractive.soulkeeper.module.souls.database.table.SoulTable
import java.util.*

@Suppress("TooManyFunctions")
internal class SoulsDaoImpl(
    private val databaseFlow: Flow<Database>,
) : SoulsDao, Logger by JUtiltLogger("AspeKt-SoulsDaoImpl") {
    private val mutex = Mutex()
    private val soulsChangedSharedFlow = MutableSharedFlow<Unit>()

    override fun getSoulsChangeFlow(): Flow<Unit> {
        return soulsChangedSharedFlow.asSharedFlow()
    }

    private fun toDatabaseSoul(it: ResultRow): DatabaseSoul {
        return DatabaseSoul(
            id = it[SoulTable.id].value,
            ownerUUID = UUID.fromString(it[SoulTable.ownerUUID]),
            ownerLastName = it[SoulTable.ownerLastName],
            createdAt = it[SoulTable.created_at] ?: it[SoulTable.broken_created_at],
            isFree = it[SoulTable.isFree],
            exp = it[SoulTable.exp],
            hasItems = true, // todo
            location = Location(
                x = it[SoulTable.locationX],
                y = it[SoulTable.locationY],
                z = it[SoulTable.locationZ],
                worldName = it[SoulTable.locationWorld]
            )
        )
    }

    private fun <T> Result<T>.logFailure(key: String): Result<T> {
        onFailure { error { "#$key error: ${it.message}. ${it.cause}" } }
        return this
    }

    override suspend fun getSouls(): Result<List<DatabaseSoul>> = runCatching {
        mutex.withLock {
            transaction(databaseFlow.first()) {
                SoulTable.selectAll()
                    .orderBy(SoulTable.broken_created_at to SortOrder.DESC)
                    .orderBy(SoulTable.created_at to SortOrder.DESC)
                    .map(::toDatabaseSoul)
            }
        }
    }.logFailure("getSouls")

    override suspend fun getPlayerSouls(uuid: UUID): Result<List<DatabaseSoul>> = runCatching {
        mutex.withLock {
            transaction(databaseFlow.first()) {
                SoulTable.selectAll()
                    .where { SoulTable.ownerUUID.eq(uuid.toString()) }
                    .map(::toDatabaseSoul)
            }
        }
    }.logFailure("getPlayerSouls")

    override suspend fun insertSoul(
        soul: DefaultSoul,
    ): Result<DatabaseSoul> = runCatching {
        mutex.withLock {
            transaction(databaseFlow.first()) {
                val soulId = SoulTable.insertAndGetId {
                    it[SoulTable.ownerUUID] = soul.ownerUUID.toString()
                    it[SoulTable.ownerLastName] = soul.ownerLastName
                    it[SoulTable.broken_created_at] = soul.createdAt
                    it[SoulTable.created_at] = soul.createdAt
                    it[SoulTable.isFree] = soul.isFree
                    it[SoulTable.locationWorld] = soul.location.worldName
                    it[SoulTable.exp] = soul.exp
                    it[SoulTable.locationX] = soul.location.x
                    it[SoulTable.locationY] = soul.location.y
                    it[SoulTable.locationZ] = soul.location.z
                }

                SoulItemsTable.batchInsert(soul.items) { item ->
                    this[SoulItemsTable.soulId] = soulId
                    this[SoulItemsTable.itemStack] = item
                }

                SoulTable.selectAll()
                    .where { SoulTable.id eq soulId }
                    .limit(1)
                    .map(::toDatabaseSoul)
                    .first()
            }
        }
    }.logFailure("insertSoul").onSuccess { soulsChangedSharedFlow.emit(Unit) }

    override suspend fun getSoulsNear(location: Location, radius: Int): Result<List<DatabaseSoul>> = runCatching {
        mutex.withLock {
            transaction(databaseFlow.first()) {
                SoulTable.selectAll()
                    .where { SoulTable.locationWorld.eq(location.worldName) }
                    .andWhere { SoulTable.locationX.between(location.x - radius, location.x + radius) }
                    .andWhere { SoulTable.locationY.between(location.y - radius, location.y + radius) }
                    .andWhere { SoulTable.locationZ.between(location.z - radius, location.z + radius) }
                    .map(::toDatabaseSoul)
                    .filter { it.location.dist(location) < radius }
            }
        }
    }.logFailure("getSoulsNear")

    override suspend fun deleteSoul(id: Long): Result<Unit> = runCatching {
        mutex.withLock {
            transaction(databaseFlow.first()) {
                SoulTable.deleteWhere { SoulTable.id.eq(id) }
                SoulItemsTable.deleteWhere { SoulItemsTable.soulId.eq(id) }
            }
        }
        Unit
    }.logFailure("deleteSoul").onSuccess { soulsChangedSharedFlow.emit(Unit) }

    override suspend fun updateSoul(soul: DatabaseSoul): Result<Unit> = runCatching {
        mutex.withLock {
            transaction(databaseFlow.first()) {
                SoulTable.update(
                    where = { SoulTable.id.eq(soul.id) },
                    body = {
                        it[SoulTable.isFree] = soul.isFree
                        it[SoulTable.exp] = soul.exp
                    }
                )
            }
        }
        Unit
    }.logFailure("deleteSoul").onSuccess { soulsChangedSharedFlow.emit(Unit) }

    override suspend fun updateSoul(soul: ItemDatabaseSoul): Result<Unit> = runCatching {
        mutex.withLock {
            transaction(databaseFlow.first()) {
                SoulTable.update(
                    where = { SoulTable.id.eq(soul.id) },
                    body = {
                        it[SoulTable.isFree] = soul.isFree
                        it[SoulTable.exp] = soul.exp
                    }
                )
                SoulItemsTable.deleteWhere { SoulItemsTable.soulId.eq(soul.id) }
                SoulItemsTable.batchInsert(soul.items) { item ->
                    this[SoulItemsTable.soulId] = soul.id
                    this[SoulItemsTable.itemStack] = item
                }
            }
        }
        Unit
    }.logFailure("deleteSoul").onSuccess { soulsChangedSharedFlow.emit(Unit) }

    override suspend fun toItemDatabaseSoul(soul: DatabaseSoul): Result<ItemDatabaseSoul> = runCatching {
        mutex.withLock {
            transaction(databaseFlow.first()) {
                ItemDatabaseSoul(
                    id = soul.id,
                    ownerUUID = soul.ownerUUID,
                    ownerLastName = soul.ownerLastName,
                    createdAt = soul.createdAt,
                    isFree = soul.isFree,
                    location = soul.location,
                    hasItems = soul.hasItems,
                    exp = soul.exp,
                    items = SoulItemsTable.selectAll()
                        .where { SoulItemsTable.soulId eq soul.id }
                        .map { it[SoulItemsTable.itemStack] }
                )
            }
        }
    }.logFailure("deleteSoul").onSuccess { soulsChangedSharedFlow.emit(Unit) }
}
