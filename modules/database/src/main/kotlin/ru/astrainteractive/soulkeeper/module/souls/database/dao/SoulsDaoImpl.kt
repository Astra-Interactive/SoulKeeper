package ru.astrainteractive.soulkeeper.module.souls.database.dao

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Bukkit
import org.bukkit.Location
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.between
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.soulkeeper.module.souls.database.dao.editor.BukkitSoulFile
import ru.astrainteractive.soulkeeper.module.souls.database.model.BukkitSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.Soul
import ru.astrainteractive.soulkeeper.module.souls.database.table.SoulTable
import java.util.UUID

internal class SoulsDaoImpl(
    private val databaseFlow: Flow<Database>,
    private val soulFileEditor: BukkitSoulFile
) : SoulsDao, Logger by JUtiltLogger("AspeKt-SoulsDaoImpl") {
    private val mutex = Mutex()

    private fun toDatabaseSoul(it: ResultRow): DatabaseSoul {
        return DatabaseSoul(
            id = it[SoulTable.id].value,
            ownerUUID = UUID.fromString(it[SoulTable.ownerUUID]),
            ownerLastName = it[SoulTable.ownerLastName],
            createdAt = it[SoulTable.created_at],
            isFree = it[SoulTable.isFree],
            hasXp = it[SoulTable.hasXp],
            hasItems = it[SoulTable.hasItems],
            location = Location(
                Bukkit.getServer().getWorld(it[SoulTable.locationWorld]),
                it[SoulTable.locationX],
                it[SoulTable.locationY],
                it[SoulTable.locationZ],
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

    override suspend fun insertSoul(soul: BukkitSoul): Result<DatabaseSoul> = runCatching {
        mutex.withLock {
            soulFileEditor.write(soul)
            transaction(databaseFlow.first()) {
                val id = SoulTable.insertAndGetId {
                    it[SoulTable.ownerUUID] = soul.ownerUUID.toString()
                    it[SoulTable.ownerLastName] = soul.ownerLastName
                    it[SoulTable.created_at] = soul.createdAt
                    it[SoulTable.isFree] = soul.isFree
                    it[SoulTable.locationWorld] = soul.location.world.name
                    it[SoulTable.hasXp] = soul.hasXp
                    it[SoulTable.hasItems] = soul.hasItems
                    it[SoulTable.locationX] = soul.location.x
                    it[SoulTable.locationY] = soul.location.y
                    it[SoulTable.locationZ] = soul.location.z
                }

                SoulTable.selectAll()
                    .where { SoulTable.id eq id }
                    .limit(1)
                    .map(::toDatabaseSoul)
                    .first()
            }
        }
    }.logFailure("insertSoul")

    override suspend fun getSoulsNear(location: Location, radius: Int): Result<List<DatabaseSoul>> = runCatching {
        mutex.withLock {
            transaction(databaseFlow.first()) {
                SoulTable.selectAll()
                    .where { SoulTable.locationWorld.eq(location.world.name) }
                    .andWhere { SoulTable.locationX.between(location.x - radius, location.x + radius) }
                    .andWhere { SoulTable.locationY.between(location.y - radius, location.y + radius) }
                    .andWhere { SoulTable.locationZ.between(location.z - radius, location.z + radius) }
                    // Doesn't work for some reason
//                    .andWhere {
//                        val x = PowerFunction(
//                            base = SoulTable.locationX.minus(location.x),
//                            exponent = doubleParam(2.0)
//                        )
//                        val y = PowerFunction(
//                            base = SoulTable.locationY.minus(location.y),
//                            exponent = doubleParam(2.0)
//                        )
//                        val z = PowerFunction(
//                            base = SoulTable.locationZ.minus(location.z),
//                            exponent = doubleParam(2.0)
//                        )
//                        val sqrt = SqrtFunction(expression = x.plus(y).plus(z))
//                        sqrt.less(radius.toBigDecimal())
//                    }
                    .map(::toDatabaseSoul)
                    .filter { it.location.distance(location) < radius }
            }
        }
    }.logFailure("getSoulsNear")

    override suspend fun deleteSoul(soul: Soul): Result<Unit> = runCatching {
        mutex.withLock {
            soulFileEditor.delete(soul)
            transaction(databaseFlow.first()) {
                SoulTable.deleteWhere {
                    SoulTable.created_at.eq(soul.createdAt)
                        .and(SoulTable.ownerUUID.eq(soul.ownerUUID.toString()))
                }
            }
        }
        Unit
    }.logFailure("deleteSoul")

    override suspend fun updateSoul(soul: Soul): Result<Unit> = runCatching {
        mutex.withLock {
            transaction(databaseFlow.first()) {
                SoulTable.update(
                    where = {
                        SoulTable.created_at.eq(soul.createdAt)
                            .and(SoulTable.ownerUUID.eq(soul.ownerUUID.toString()))
                    },
                    body = {
                        it[SoulTable.isFree] = soul.isFree
                        it[SoulTable.hasXp] = soul.hasXp
                        it[SoulTable.hasItems] = soul.hasItems
                    }
                )
            }
        }
        Unit
    }.logFailure("deleteSoul")

    override suspend fun updateSoul(soul: BukkitSoul): Result<Unit> = runCatching {
        mutex.withLock { soulFileEditor.write(soul).getOrThrow() }
        updateSoul(soul = soul as Soul).getOrThrow()
    }.logFailure("deleteSoul")

    override suspend fun toItemStackSoul(soul: Soul): Result<BukkitSoul> {
        return soulFileEditor.read(soul)
    }
}
