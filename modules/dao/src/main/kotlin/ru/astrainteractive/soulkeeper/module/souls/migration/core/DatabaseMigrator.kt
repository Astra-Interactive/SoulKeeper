package ru.astrainteractive.soulkeeper.module.souls.migration.core

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger

class DatabaseMigrator(
    private val migrations: List<DatabaseMigration>,
    private val latestDbVersion: Int
) : Logger by JUtiltLogger("SoulKeeper-DatabaseMigrator") {

    suspend fun migrate(database: Database) {
        val currentDbVersion = getCurrentVersion(database)
        if (latestDbVersion == currentDbVersion) {
            info { "#migrate database is up to date" }
            return
        }

        info { "#migrate current version=$currentDbVersion, latest=$latestDbVersion, running pending migrations..." }
        migrations
            .filter { migration -> migration.requiredDbVersion == currentDbVersion }
            .forEach { migration ->
                info { "#migrate running migration to version $currentDbVersion..." }
                migration.migrate(database)
            }

        val newVersion = currentDbVersion + 1
        info { "#migrate migrated to version $newVersion" }
        setVersion(database, newVersion)

        migrate(database)
    }

    private suspend fun isTableExists(
        database: Database,
        tableName: String
    ): Boolean {
        return transaction(database) {
            var exists = false
            exec("SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'") { rs ->
                exists = rs.next()
            }
            exists
        }
    }

    private suspend fun hasAnyUserTables(database: Database): Boolean {
        return transaction(database) {
            var count = 0
            exec("SELECT COUNT(*) AS cnt FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'") { rs ->
                if (rs.next()) count = rs.getInt("cnt")
            }
            count > 0
        }
    }

    @Suppress("MaxLineLength", "MaximumLineLength")
    private suspend fun getCurrentVersion(database: Database): Int {
        val versionTableExists = isTableExists(database, "DB_VERSION")
        if (!versionTableExists) {
            val isFreshDatabase = !hasAnyUserTables(database)
            val initialVersion = if (isFreshDatabase) latestDbVersion else DEFAULT_VERSION
            info { "#getCurrentVersion DB_VERSION table not found, freshDb=$isFreshDatabase, initialVersion=$initialVersion" }
            setVersion(database, initialVersion)
            return initialVersion
        }
        return transaction(database) {
            var version: Int? = null
            exec("SELECT version FROM DB_VERSION LIMIT 1") { rs ->
                if (rs.next()) version = rs.getInt("version")
            }
            version ?: DEFAULT_VERSION
        }
    }

    private suspend fun setVersion(database: Database, version: Int) {
        transaction(database) {
            exec("CREATE TABLE IF NOT EXISTS DB_VERSION (version INTEGER NOT NULL)")
            var hasRow = false
            exec("SELECT COUNT(*) AS cnt FROM DB_VERSION") { rs ->
                if (rs.next()) hasRow = rs.getInt("cnt") > 0
            }
            if (hasRow) {
                exec("UPDATE DB_VERSION SET version = $version")
            } else {
                exec("INSERT INTO DB_VERSION (version) VALUES ($version)")
            }
        }
    }

    companion object {
        private const val DEFAULT_VERSION = 0
    }
}
