package ru.astrainteractive.soulkeeper.module.souls.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.klibs.mikro.exposed.model.DatabaseConfiguration
import ru.astrainteractive.klibs.mikro.exposed.util.connect
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDaoImpl
import ru.astrainteractive.soulkeeper.module.souls.database.table.SoulItemsTable
import ru.astrainteractive.soulkeeper.module.souls.database.table.SoulTable
import ru.astrainteractive.soulkeeper.module.souls.migration.DropBrokenCreatedAtMigration
import ru.astrainteractive.soulkeeper.module.souls.migration.H2ToSqliteMigration
import ru.astrainteractive.soulkeeper.module.souls.migration.KrateFolderMigration
import ru.astrainteractive.soulkeeper.module.souls.migration.MakeCreatedAtNonNullMigration
import java.io.File

interface SoulsDaoModule {
    val lifecycle: Lifecycle

    val databaseFlow: Flow<Database>

    val soulsDao: SoulsDao

    class Default(
        dataFolder: File,
        ioScope: CoroutineScope,
        dispatchers: KotlinDispatchers
    ) : SoulsDaoModule, Logger by JUtiltLogger("SoulsDaoModule") {
        override val databaseFlow: Flow<Database> = flow {
            H2ToSqliteMigration(dataFolder, dispatchers)
                .migrate()
            KrateFolderMigration(
                dataFolder = dataFolder,
                kratesFolder = dataFolder.resolve(".deaths")
            ).migrate()
            if (!dataFolder.exists()) dataFolder.mkdirs()
            val database = dataFolder.resolve("souls_v3")
                .absolutePath
                .let(DatabaseConfiguration::SQLite)
                .connect()
            TransactionManager.manager.defaultIsolationLevel = java.sql.Connection.TRANSACTION_SERIALIZABLE
            DropBrokenCreatedAtMigration(database).migrate()
            MakeCreatedAtNonNullMigration(database).migrate()
            transaction(database) {
                SchemaUtils.create(SoulTable)
                SchemaUtils.create(SoulItemsTable)
            }
            emit(database)
        }
            .retry { throwable ->
                error(throwable) { "Could not connect to database" }
                delay(5000L)
                true
            }
            .shareIn(ioScope, SharingStarted.Eagerly, 1)

        override val soulsDao: SoulsDao = SoulsDaoImpl(
            databaseFlow = databaseFlow,
            dispatchers = dispatchers
        )

        override val lifecycle: Lifecycle = Lifecycle.Lambda(
            onDisable = {
                GlobalScope.launch(dispatchers.IO) {
                    databaseFlow.firstOrNull()?.let(TransactionManager::closeAndUnregister)
                }
            }
        )
    }
}
