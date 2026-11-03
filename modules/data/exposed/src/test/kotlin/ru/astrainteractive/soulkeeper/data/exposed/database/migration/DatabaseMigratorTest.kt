@file:Suppress("FunctionNaming")

package ru.astrainteractive.soulkeeper.data.exposed.database.migration

import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DatabaseMigratorTest {
    private lateinit var directory: File
    private val databases = mutableListOf<Database>()
    private lateinit var database: Database

    private fun createDatabase(): Database {
        val file = directory.resolve("souls-${databases.size}.db")
        return Database.connect(
            url = "jdbc:sqlite:${file.absolutePath}",
            driver = "org.sqlite.JDBC"
        ).also(databases::add)
    }

    @BeforeTest
    fun setUp() {
        directory = createTempDirectory("soul-migration-test").toFile()
        database = createDatabase()
    }

    @AfterTest
    fun tearDown() {
        databases.forEach(TransactionManager::closeAndUnregister)
        directory.deleteRecursively()
    }

    private fun createMigrator(database: Database): DatabaseMigrator {
        return DatabaseMigrator(
            database = database,
            migrations = listOf(
                DropBrokenCreatedAtMigration(database),
                MakeCreatedAtNonNullMigration(database)
            ),
            latestDbVersion = 2
        )
    }

    private fun Database.prepareLegacySchema(version: Int?) {
        transaction(this) {
            exec(
                """
                CREATE TABLE SOUL (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    owner_uuid TEXT NOT NULL,
                    owner_last_name TEXT NOT NULL,
                    created_at BIGINT,
                    created_at_millis BIGINT,
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
                INSERT INTO SOUL VALUES
                    (1, 'owner-1', 'Steve', 123, NULL, 0, 10, 'world', 1, 64, 2),
                    (2, 'owner-2', 'Alex', 456, 789, 1, 20, 'nether', 3, 65, 4),
                    (3, 'owner-3', 'Sam', NULL, NULL, 0, 30, 'end', 5, 66, 6)
                """.trimIndent()
            )
            if (version != null) {
                exec("CREATE TABLE DB_VERSION (version INTEGER NOT NULL)")
                exec("INSERT INTO DB_VERSION VALUES ($version)")
            }
        }
    }

    private fun Database.readVersion(): Int? {
        return transaction(this) {
            var version: Int? = null
            exec("SELECT version FROM DB_VERSION") { rows ->
                if (rows.next()) version = rows.getInt("version")
            }
            version
        }
    }

    private fun Database.readTimestamps(): List<Long> {
        return transaction(this) {
            buildList {
                exec("SELECT created_at_millis FROM SOUL ORDER BY id") { rows ->
                    while (rows.next()) add(rows.getLong("created_at_millis"))
                }
            }
        }
    }

    private fun Database.readRequiredColumns(): Map<String, Boolean> {
        return transaction(this) {
            buildMap {
                exec("PRAGMA table_info(SOUL)") { rows ->
                    while (rows.next()) put(rows.getString("name"), rows.getInt("notnull") != 0)
                }
            }
        }
    }

    @Test
    fun GIVEN_fresh_database_WHEN_migrating_THEN_sets_latest_version_without_old_migrations(): TestResult {
        return runTest {
            createMigrator(database).migrate()

            assertEquals(2, database.readVersion())
            assertTrue(database.readRequiredColumns().isEmpty())
        }
    }

    @Test
    fun GIVEN_legacy_schema_WHEN_migrating_THEN_preserves_souls_and_fixes_timestamps(): TestResult {
        return runTest {
            database.prepareLegacySchema(version = 1)

            createMigrator(database).migrate()

            assertEquals(2, database.readVersion())
            assertFalse(database.readRequiredColumns().containsKey("created_at"))
            assertEquals(true, database.readRequiredColumns()["created_at_millis"])
            val timestamps = database.readTimestamps()
            assertEquals(listOf(123L, 789L), timestamps.take(2))
            assertEquals(3, timestamps.size)
            assertTrue(timestamps.last() > 0L)
            transaction(database) {
                exec("SELECT owner_last_name, exp FROM SOUL WHERE id = 2") { rows ->
                    assertTrue(rows.next())
                    assertEquals("Alex", rows.getString("owner_last_name"))
                    assertEquals(20, rows.getInt("exp"))
                }
            }
        }
    }

    @Test
    fun GIVEN_unversioned_legacy_schema_WHEN_migrating_THEN_runs_all_pending_versions(): TestResult {
        return runTest {
            database.prepareLegacySchema(version = null)

            createMigrator(database).migrate()

            assertEquals(2, database.readVersion())
            assertFalse(database.readRequiredColumns().containsKey("created_at"))
            assertEquals(listOf(123L, 789L), database.readTimestamps().take(2))
        }
    }

    @Test
    fun GIVEN_migrated_database_WHEN_running_again_THEN_preserves_data(): TestResult {
        return runTest {
            database.prepareLegacySchema(version = 1)
            val migrator = createMigrator(database)
            migrator.migrate()
            val timestamps = database.readTimestamps()

            migrator.migrate()

            assertEquals(2, database.readVersion())
            assertEquals(timestamps, database.readTimestamps())
        }
    }

    @Test
    fun GIVEN_updated_columns_WHEN_running_steps_again_THEN_skips_completed_changes(): TestResult {
        return runTest {
            database.prepareLegacySchema(version = 1)
            createMigrator(database).migrate()
            val timestamps = database.readTimestamps()

            DropBrokenCreatedAtMigration(database).migrate()
            MakeCreatedAtNonNullMigration(database).migrate()

            assertEquals(timestamps, database.readTimestamps())
            assertEquals(true, database.readRequiredColumns()["created_at_millis"])
        }
    }

    @Test
    fun GIVEN_two_connections_WHEN_migrating_first_THEN_second_database_is_untouched(): TestResult {
        return runTest {
            database.prepareLegacySchema(version = 1)
            val otherDatabase = createDatabase()
            otherDatabase.prepareLegacySchema(version = 1)

            createMigrator(database).migrate()

            assertEquals(2, database.readVersion())
            assertEquals(1, otherDatabase.readVersion())
            assertTrue(otherDatabase.readRequiredColumns().containsKey("created_at"))
            assertEquals(false, otherDatabase.readRequiredColumns()["created_at_millis"])
        }
    }
}
