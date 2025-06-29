package ru.astrainteractive.soulkeeper.module.souls.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import ru.astrainteractive.astralibs.exposed.model.DatabaseConfiguration
import ru.astrainteractive.astralibs.exposed.model.connect
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDaoImpl
import ru.astrainteractive.soulkeeper.module.souls.database.table.SoulItemsTable
import ru.astrainteractive.soulkeeper.module.souls.database.table.SoulTable
import java.io.File

interface SoulsDaoModule {
    val lifecycle: Lifecycle

    val databaseFlow: Flow<Database>

    val soulsDao: SoulsDao

    class Default(
        dataFolder: File,
        scope: CoroutineScope
    ) : SoulsDaoModule {
        override val databaseFlow: Flow<Database> = flow {
            if (!dataFolder.exists()) dataFolder.mkdirs()
            val database = DatabaseConfiguration.H2("souls").connect(dataFolder)
            TransactionManager.manager.defaultIsolationLevel = java.sql.Connection.TRANSACTION_SERIALIZABLE
            transaction(database) {
                addLogger(Slf4jSqlDebugLogger)
                SchemaUtils.create(SoulTable)
                SchemaUtils.createMissingTablesAndColumns(SoulTable)
                SchemaUtils.createMissingTablesAndColumns(SoulItemsTable)
            }
            emit(database)
        }.shareIn(scope, SharingStarted.Eagerly, 1)

        override val soulsDao: SoulsDao = SoulsDaoImpl(
            databaseFlow = databaseFlow,
        )

        override val lifecycle: Lifecycle = Lifecycle.Lambda(
            onDisable = {
                GlobalScope.launch {
                    TransactionManager.closeAndUnregister(databaseFlow.first())
                }
            }
        )
    }
}
