package ru.astrainteractive.soulkeeper.data.exposed.database.migration

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.klibs.mikro.exposed.model.DatabaseConfiguration
import ru.astrainteractive.klibs.mikro.exposed.util.connect
import ru.astrainteractive.soulkeeper.data.dao.soul.api.SoulRepository
import ru.astrainteractive.soulkeeper.data.dao.soul.model.DefaultSoul
import ru.astrainteractive.soulkeeper.data.exposed.database.api.FileMigration
import java.io.File

internal class H2ToSqliteMigration(
    private val dataFolder: File,
    private val createSchema: (Database) -> Unit,
    private val createRepository: (Database) -> SoulRepository
) : FileMigration, Logger by JUtiltLogger("SoulKeeper-H2ToSqliteMigration") {

    override suspend fun migrate() = coroutineScope {
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
        createSchema(sqliteDatabase)

        val sqliteSoulRepository = createRepository(sqliteDatabase)
        val h2SoulRepository = createRepository(h2Database)
        h2SoulRepository.getSouls()
            .onFailure { throwable ->
                error(throwable) { "Could not convert souls from H2 to SQLite" }
                return@coroutineScope
            }
            .getOrNull()
            .orEmpty()
            .map { dbSoul -> async { h2SoulRepository.toItemDatabaseSoul(dbSoul) } }
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
            .forEach { soul -> sqliteSoulRepository.insertSoul(soul) }
        info { "#migrate Migration has been completed. Closing databases..." }
        TransactionManager.closeAndUnregister(h2Database)
        TransactionManager.closeAndUnregister(sqliteDatabase)
        dataFolder.resolve("souls_v2.mv.db").delete()
    }
}
