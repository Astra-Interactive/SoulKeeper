package ru.astrainteractive.soulkeeper.module.souls.migration.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.module.souls.migration.core.DatabaseMigration
import java.time.Instant

class MakeCreatedAtNonNullMigration :
    DatabaseMigration,
    Logger by JUtiltLogger("SoulKeeper-MakeCreatedAtNonNullMigration") {

    override val requiredDbVersion: Int = 1

    @Suppress("LoopWithTooManyJumpStatements")
    override suspend fun migrate(database: Database) {
        val isNullable = transaction(database) {
            var nullable = false
            exec("PRAGMA table_info(SOUL)") { rs ->
                while (rs.next()) {
                    if (rs.getString("name") != "created_at_millis") continue
                    nullable = rs.getInt("notnull") == 0
                    if (nullable) break
                }
            }
            nullable
        }
        if (!isNullable) {
            info { "#migrate 'created_at_millis' is already NOT NULL, skipping migration" }
            return
        }
        val nowMillis = Instant.now().toEpochMilli()
        info { "#migrate recreating SOUL table with 'created_at_millis' as NOT NULL (default=$nowMillis)..." }
        transaction(database) {
            exec(
                """
                CREATE TABLE SOUL_TEMP (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    owner_uuid TEXT NOT NULL,
                    owner_last_name TEXT NOT NULL,
                    created_at_millis BIGINT NOT NULL,
                    is_free INTEGER NOT NULL,
                    exp INTEGER NOT NULL,
                    location_world TEXT NOT NULL,
                    location_x REAL NOT NULL,
                    location_y REAL NOT NULL,
                    location_z REAL NOT NULL
                )
                """.trimIndent()
            )
            exec(
                """
                INSERT INTO SOUL_TEMP (id, owner_uuid, owner_last_name, created_at_millis, is_free, exp, location_world, location_x, location_y, location_z)
                SELECT id, owner_uuid, owner_last_name, COALESCE(created_at_millis, $nowMillis), is_free, exp, location_world, location_x, location_y, location_z
                FROM SOUL
                """.trimIndent()
            )
            exec("DROP TABLE SOUL")
            exec("ALTER TABLE SOUL_TEMP RENAME TO SOUL")
        }
        info { "#migrate SOUL table recreated with 'created_at_millis' as NOT NULL" }
    }
}
