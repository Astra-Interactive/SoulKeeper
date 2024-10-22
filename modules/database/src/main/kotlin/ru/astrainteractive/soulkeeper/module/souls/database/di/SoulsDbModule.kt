package ru.astrainteractive.soulkeeper.module.souls.database.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import ru.astrainteractive.astralibs.exposed.factory.DatabaseFactory
import ru.astrainteractive.astralibs.exposed.model.DatabaseConfiguration
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.module.souls.database.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.dao.SoulsDaoImpl
import ru.astrainteractive.soulkeeper.module.souls.database.dao.editor.SoulFileEditor
import ru.astrainteractive.soulkeeper.module.souls.database.table.SoulTable
import java.io.File

interface SoulsDbModule {
    val lifecycle: Lifecycle

    val databaseFlow: Flow<Database>

    val soulsDao: SoulsDao

    class Default(
        dataFolder: File,
        scope: CoroutineScope
    ) : SoulsDbModule {
        override val databaseFlow: Flow<Database> = flow {
            if (!dataFolder.exists()) dataFolder.mkdirs()
            val database = DatabaseFactory(dataFolder).create(DatabaseConfiguration.H2("souls"))
            TransactionManager.manager.defaultIsolationLevel = java.sql.Connection.TRANSACTION_SERIALIZABLE
            transaction(database) {
                addLogger(Slf4jSqlDebugLogger)
                SchemaUtils.create(
                    SoulTable
                )
            }
            emit(database)
        }.shareIn(scope, SharingStarted.Eagerly, 1)

        override val soulsDao: SoulsDao = SoulsDaoImpl(
            databaseFlow = databaseFlow,
            soulFileEditor = SoulFileEditor(
                folder = dataFolder.resolve("storage")
            )
        )

        override val lifecycle: Lifecycle = Lifecycle.Lambda(
            onDisable = {
                runBlocking {
                    TransactionManager.closeAndUnregister(databaseFlow.first())
                }
            }
        )
    }
}
