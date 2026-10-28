package ru.astrainteractive.soulkeeper.data.exposed.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.shareIn
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.klibs.mikro.exposed.model.DatabaseConfiguration
import ru.astrainteractive.klibs.mikro.exposed.util.connectAsFlow
import ru.astrainteractive.soulkeeper.data.dao.soul.api.SoulRepository
import ru.astrainteractive.soulkeeper.data.exposed.database.api.FileMigration
import ru.astrainteractive.soulkeeper.data.exposed.database.migration.DatabaseMigrator
import ru.astrainteractive.soulkeeper.data.exposed.database.migration.DropBrokenCreatedAtMigration
import ru.astrainteractive.soulkeeper.data.exposed.database.migration.H2ToSqliteMigration
import ru.astrainteractive.soulkeeper.data.exposed.database.migration.KrateFolderMigration
import ru.astrainteractive.soulkeeper.data.exposed.database.migration.MakeCreatedAtNonNullMigration
import ru.astrainteractive.soulkeeper.data.exposed.soul.ExposedSoulRepository
import ru.astrainteractive.soulkeeper.data.exposed.soul.table.SoulItemsTable
import ru.astrainteractive.soulkeeper.data.exposed.soul.table.SoulTable
import java.io.File
import kotlin.time.Duration.Companion.seconds

interface SoulsDaoModule {
    val lifecycle: Lifecycle

    val databaseFlow: Flow<Database>

    val soulRepository: SoulRepository

    class Default(
        private val dataFolder: File,
        private val ioScope: CoroutineScope,
        private val dispatchers: KotlinDispatchers
    ) : SoulsDaoModule, Logger by JUtiltLogger("SoulsDaoModule") {
        private val fileMigrations: List<FileMigration>
            get() = listOf(
                H2ToSqliteMigration(
                    dataFolder = dataFolder,
                    createSchema = ::createSoulTables,
                    createRepository = { database ->
                        ExposedSoulRepository(
                            databaseFlow = flowOf(database),
                            dispatchers = dispatchers
                        )
                    }
                ),
                KrateFolderMigration(
                    dataFolder = dataFolder,
                    kratesFolder = dataFolder.resolve(".deaths")
                )
            )
        private val databaseMigrator: DatabaseMigrator
            get() = DatabaseMigrator(
                migrations = listOf(
                    DropBrokenCreatedAtMigration(),
                    MakeCreatedAtNonNullMigration(),
                ),
                latestDbVersion = 2
            )

        private val dbConfigurationFlow = flow {
            if (!dataFolder.exists()) dataFolder.mkdirs()
            val configuration = dataFolder.resolve("souls_v3")
                .absolutePath
                .let(DatabaseConfiguration::SQLite)
            emit(configuration)
        }
        override val databaseFlow: Flow<Database> = dbConfigurationFlow
            .onStart { runFileMigrations() }
            .flatMapLatest(::connectDatabase)
            .onEach(databaseMigrator::migrate)
            .onEach { database ->
                TransactionManager.manager.defaultIsolationLevel = java.sql.Connection.TRANSACTION_SERIALIZABLE
                createSoulTables(database)
            }
            .retry { throwable ->
                error(throwable) { "Could not connect to database" }
                delay(5.seconds)
                true
            }
            .shareIn(
                scope = ioScope,
                started = SharingStarted.Eagerly,
                replay = 1
            )

        override val soulRepository: SoulRepository = ExposedSoulRepository(
            databaseFlow = databaseFlow,
            dispatchers = dispatchers
        )

        override val lifecycle: Lifecycle = Lifecycle.Lambda(
            onDisable = {}
        )

        private fun createSoulTables(database: Database) {
            transaction(database) {
                SchemaUtils.create(SoulTable)
                SchemaUtils.create(SoulItemsTable)
            }
        }

        private fun connectDatabase(configuration: DatabaseConfiguration): Flow<Database> {
            return configuration.connectAsFlow()
        }

        private suspend fun runFileMigrations() {
            for (migration in fileMigrations) {
                migration.migrate()
            }
        }
    }
}
