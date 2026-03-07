package ru.astrainteractive.soulkeeper.module.souls.di

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import ru.astrainteractive.klibs.mikro.exposed.model.DatabaseConfiguration
import ru.astrainteractive.klibs.mikro.exposed.model.DatabaseConfiguration.SQLite
import ru.astrainteractive.klibs.mikro.exposed.util.connect
import ru.astrainteractive.soulkeeper.module.souls.database.table.SoulItemsTable
import ru.astrainteractive.soulkeeper.module.souls.database.table.SoulTable
import java.nio.file.Files
import kotlin.test.Test

class SoulsDaoModuleTest {
    fun getTempDirectory() = Files.createTempDirectory("tmp").toFile()

    fun configurationFlow(configuration: DatabaseConfiguration) = callbackFlow {
        val dataFolder = getTempDirectory()
        if (!dataFolder.exists()) dataFolder.mkdirs()
        val database = configuration.connect()
        TransactionManager.manager.defaultIsolationLevel = java.sql.Connection.TRANSACTION_SERIALIZABLE

        transaction(database) {
            SchemaUtils.create(SoulTable)
            SchemaUtils.create(SoulItemsTable)
        }
        send(database)
        awaitClose {
            database.connector.invoke().close()
            TransactionManager.closeAndUnregister(database)
        }
    }

    @Test
    fun test() = runTest {
        withContext(Dispatchers.Default) {
            val dataFolder = getTempDirectory()
            val configuration = MutableStateFlow(
                dataFolder.resolve("db1")
                    .absolutePath
                    .let(DatabaseConfiguration::SQLite)
            )
            val count = MutableStateFlow(0)
            configuration
                .flatMapLatest { configuration -> configurationFlow(configuration) }
                .onEach { println("Created database!") }
                .onEach { count.update { currentCount -> currentCount + 1 } }
                .launchIn(backgroundScope)
            delay(1000L)
            configuration.update {
                dataFolder.resolve("db2")
                    .absolutePath
                    .let(DatabaseConfiguration::SQLite)
            }
            delay(1000L)
            configuration.emit(
                dataFolder.resolve("db3")
                    .absolutePath
                    .let(DatabaseConfiguration::SQLite)
            )
        }
    }
}
