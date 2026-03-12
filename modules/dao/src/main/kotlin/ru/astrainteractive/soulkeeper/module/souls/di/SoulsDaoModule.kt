package ru.astrainteractive.soulkeeper.module.souls.di

import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.shareIn
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.exposed.model.DatabaseConfiguration
import ru.astrainteractive.klibs.mikro.exposed.util.connectAsFlow
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDaoImpl
import ru.astrainteractive.soulkeeper.module.souls.database.table.SoulItemsTable
import ru.astrainteractive.soulkeeper.module.souls.database.table.SoulTable
import ru.astrainteractive.soulkeeper.module.souls.di.qualifier.DaoLifecycle
import ru.astrainteractive.soulkeeper.module.souls.migration.core.DatabaseMigrator
import ru.astrainteractive.soulkeeper.module.souls.migration.core.FileMigration
import ru.astrainteractive.soulkeeper.module.souls.migration.database.DropBrokenCreatedAtMigration
import ru.astrainteractive.soulkeeper.module.souls.migration.database.MakeCreatedAtNonNullMigration
import ru.astrainteractive.soulkeeper.module.souls.migration.file.H2ToSqliteMigration
import ru.astrainteractive.soulkeeper.module.souls.migration.file.KrateFolderMigration
import java.io.File

object SoulsDaoScope

@DependencyGraph(SoulsDaoScope::class)
interface SoulsDaoModule {

    val databaseFlow: Flow<Database>

    val soulsDao: SoulsDao

    @get:DaoLifecycle
    val lifecycle: Lifecycle

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides dataFolder: File,
            @Provides ioScope: CoroutineScope,
            @Provides dispatchers: KotlinDispatchers
        ): SoulsDaoModule
    }

    @SingleIn(SoulsDaoScope::class)
    @Provides
    fun provideDatabaseFlow(
        dataFolder: File,
        ioScope: CoroutineScope,
        dispatchers: KotlinDispatchers
    ): Flow<Database> {
        val logger = JUtiltLogger("SoulsDaoModule")
        val fileMigrations: List<FileMigration> = listOf(
            H2ToSqliteMigration(dataFolder, dispatchers),
            KrateFolderMigration(
                dataFolder = dataFolder,
                kratesFolder = dataFolder.resolve(".deaths")
            )
        )
        val databaseMigrator = DatabaseMigrator(
            migrations = listOf(
                DropBrokenCreatedAtMigration(),
                MakeCreatedAtNonNullMigration(),
            ),
            latestDbVersion = 2
        )
        val dbConfigurationFlow = flow {
            if (!dataFolder.exists()) dataFolder.mkdirs()
            val configuration = dataFolder.resolve("souls_v3")
                .absolutePath
                .let(DatabaseConfiguration::SQLite)
            emit(configuration)
        }
        return dbConfigurationFlow
            .onStart { fileMigrations.forEach { migration -> migration.migrate() } }
            .flatMapLatest { databaseConfiguration -> databaseConfiguration.connectAsFlow() }
            .onEach { database -> databaseMigrator.migrate(database) }
            .onEach { database ->
                TransactionManager.manager.defaultIsolationLevel =
                    java.sql.Connection.TRANSACTION_SERIALIZABLE
                transaction(database) {
                    SchemaUtils.create(SoulTable)
                    SchemaUtils.create(SoulItemsTable)
                }
            }
            .retry { throwable ->
                logger.error(throwable) { "Could not connect to database" }
                delay(5000L)
                true
            }
            .shareIn(ioScope, SharingStarted.Eagerly, 1)
    }

    @Binds
    val SoulsDaoImpl.bind: SoulsDao

    @SingleIn(SoulsDaoScope::class)
    @DaoLifecycle
    @Provides
    fun provideLifecycle(): Lifecycle = Lifecycle.Lambda(
        onDisable = {}
    )
}
