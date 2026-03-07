package ru.astrainteractive.soulkeeper.module.souls.migration.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.module.souls.migration.core.DatabaseMigration

class DropBrokenCreatedAtMigration :
    DatabaseMigration,
    Logger by JUtiltLogger("DropBrokenCreatedAtMigration") {

    override val requiredDbVersion: Int = 1
    override suspend fun migrate(database: Database) {
        val columnExists = transaction(database) {
            buildList {
                exec("PRAGMA table_info(SOUL)") { rs ->
                    while (rs.next()) {
                        add(rs.getString("name"))
                    }
                }
            }.contains("created_at")
        }
        if (!columnExists) {
            info { "#migrate column 'created_at' does not exist, skipping migration" }
            return
        }
        info { "#migrate copying 'created_at' into 'created_at_millis' where missing..." }
        transaction(database) {
            exec(
                stmt = """
                    UPDATE SOUL
                    SET created_at_millis = created_at
                    WHERE created_at_millis IS NULL AND created_at
                """.trimIndent()
            )
        }
        info { "#migrate dropping column 'created_at' from SOUL table..." }
        transaction(database) {
            exec("ALTER TABLE SOUL DROP COLUMN created_at")
        }
        info { "#migrate column 'created_at' has been dropped" }
    }
}
