package ru.astrainteractive.aspekt.module.souls.database.dao

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Bukkit
import org.bukkit.Location
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.astrainteractive.aspekt.module.souls.database.dao.editor.SoulFileEditor
import ru.astrainteractive.aspekt.module.souls.database.model.ItemStackSoul
import ru.astrainteractive.aspekt.module.souls.database.model.Soul
import ru.astrainteractive.aspekt.module.souls.database.table.SoulTable
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import java.util.UUID

internal class SoulsDaoImpl(
    private val databaseFlow: Flow<Database>,
    private val soulFileEditor: SoulFileEditor
) : SoulsDao, Logger by JUtiltLogger("AspeKt-SoulsDaoImpl") {
    private val mutex = Mutex()

    private fun toSoul(it: ResultRow): Soul {
        return Soul(
            ownerUUID = UUID.fromString(it[SoulTable.ownerUUID]),
            ownerName = it[SoulTable.ownerLastName],
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

    override suspend fun getSouls(): Result<List<Soul>> = runCatching {
        mutex.withLock {
            transaction(databaseFlow.first()) {
                SoulTable.selectAll().map(::toSoul)
            }
        }
    }.logFailure("getSouls")

    override suspend fun getPlayerSouls(uuid: UUID): Result<List<Soul>> = runCatching {
        mutex.withLock {
            transaction(databaseFlow.first()) {
                SoulTable.selectAll()
                    .where { SoulTable.ownerUUID.eq(uuid.toString()) }
                    .map(::toSoul)
            }
        }
    }.logFailure("getPlayerSouls")

    override suspend fun insertSoul(itemStackSoul: ItemStackSoul): Result<Unit> = runCatching {
        mutex.withLock {
            val soul = itemStackSoul.soul
            soulFileEditor.write(itemStackSoul)
            transaction(databaseFlow.first()) {
                SoulTable.insert {
                    it[SoulTable.ownerUUID] = soul.ownerUUID.toString()
                    it[SoulTable.ownerLastName] = soul.ownerName
                    it[SoulTable.created_at] = soul.createdAt
                    it[SoulTable.isFree] = soul.isFree
                    it[SoulTable.locationWorld] = soul.location.world.name
                    it[SoulTable.hasXp] = soul.hasXp
                    it[SoulTable.hasItems] = soul.hasItems
                    it[SoulTable.locationX] = soul.location.x
                    it[SoulTable.locationY] = soul.location.y
                    it[SoulTable.locationZ] = soul.location.z
                }
            }
        }
    }.logFailure("insertSoul").map { Unit }

    override suspend fun getSoulsNear(location: Location, radius: Int): Result<List<Soul>> = runCatching {
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
                    .map(::toSoul)
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
    }.logFailure("deleteSoul").map { Unit }

    override suspend fun updateSoul(itemStackSoul: ItemStackSoul): Result<Unit> = runCatching {
        mutex.withLock {
            soulFileEditor.write(itemStackSoul)
            transaction(databaseFlow.first()) {
                SoulTable.update(
                    where = {
                        SoulTable.created_at.eq(itemStackSoul.soul.createdAt)
                            .and(SoulTable.ownerUUID.eq(itemStackSoul.soul.ownerName))
                    },
                    body = {
                        it[SoulTable.isFree] = itemStackSoul.soul.isFree
                        it[SoulTable.hasXp] = itemStackSoul.soul.hasXp
                        it[SoulTable.hasItems] = itemStackSoul.soul.hasItems
                    }
                )
            }
        }
    }.logFailure("deleteSoul").map { Unit }

    override suspend fun toItemStackSoul(soul: Soul): Result<ItemStackSoul> {
        return soulFileEditor.read(soul)
    }
}
