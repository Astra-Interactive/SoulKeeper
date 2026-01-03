package ru.astrainteractive.soulkeeper.module.souls.migration

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.klibs.mikro.exposed.model.DatabaseConfiguration
import ru.astrainteractive.klibs.mikro.exposed.util.connect
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDaoImpl
import ru.astrainteractive.soulkeeper.module.souls.database.model.DefaultSoul
import ru.astrainteractive.soulkeeper.module.souls.database.table.SoulItemsTable
import ru.astrainteractive.soulkeeper.module.souls.database.table.SoulTable
import java.io.File

class H2ToSqliteMigration(
    val dataFolder: File
) : Logger by JUtiltLogger("H2ToSqliteMigration") {

    suspend fun migrate() = coroutineScope {
        if (!dataFolder.resolve("souls_v2.mv.db").exists()) return@coroutineScope
        info { "#migrate started migration of database..." }
        val h2Database = dataFolder.resolve("souls_v2")
            .absolutePath
            .let(DatabaseConfiguration::H2)
            .connect()
        val sqliteDatabase = dataFolder.resolve("souls_v3")
            .absolutePath
            .let(DatabaseConfiguration::SQLite)
            .connect()
        transaction(sqliteDatabase) {
            SchemaUtils.create(SoulTable)
            SchemaUtils.create(SoulItemsTable)
        }

        val sqliteSoulsDao = SoulsDaoImpl(flowOf(sqliteDatabase))
        val hwSoulsDao = SoulsDaoImpl(flowOf(h2Database))
        hwSoulsDao.getSouls()
            .onFailure {
                error(it) { "Could not convert souls from H2 to SQLite" }
                return@coroutineScope
            }
            .getOrNull()
            .orEmpty()
            .map { dbSoul -> async { hwSoulsDao.toItemDatabaseSoul(dbSoul) } }
            .awaitAll()
            .mapNotNull { soul -> soul.getOrNull() }
            .map { soul ->
                DefaultSoul(
                    ownerUUID = soul.ownerUUID,
                    ownerLastName = soul.ownerLastName,
                    createdAt = soul.createdAt,
                    isFree = soul.isFree,
                    location = soul.location,
                    exp = soul.exp,
                    items = soul.items,
                )
            }
            .forEach { soul -> sqliteSoulsDao.insertSoul(soul) }
        info { "#migrate Migration has been completed. Closing databases..." }
        TransactionManager.closeAndUnregister(h2Database)
        TransactionManager.closeAndUnregister(sqliteDatabase)
        dataFolder.resolve("souls_v2.mv.db").delete()
    }
}
