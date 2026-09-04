package ru.astrainteractive.soulkeeper.module.souls.dao

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.DefaultSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.StringFormatObject
import ru.astrainteractive.soulkeeper.module.souls.database.table.SoulItemsTable
import ru.astrainteractive.soulkeeper.module.souls.database.table.SoulTable
import java.io.File
import java.time.Instant
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.path.createTempFile
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SoulsDaoImplTest {
    private lateinit var databaseFile: File
    private lateinit var database: Database

    @BeforeTest
    fun setUp() {
        databaseFile = createTempFile(prefix = "souls_dao_test", suffix = ".db").toFile()
        database = Database.connect(
            url = "jdbc:sqlite:${databaseFile.absolutePath}",
            driver = "org.sqlite.JDBC"
        )
        transaction(database) {
            SchemaUtils.create(SoulTable, SoulItemsTable)
        }
    }

    @AfterTest
    fun tearDown() {
        TransactionManager.closeAndUnregister(database)
        databaseFile.delete()
    }

    private fun TestScope.createDao(): SoulsDao {
        return SoulsDaoImpl(
            databaseFlow = flowOf(database),
            dispatchers = TestKotlinDispatchers(StandardTestDispatcher(testScheduler))
        )
    }

    private fun createSoul(
        ownerUUID: UUID = UUID.randomUUID(),
        createdAt: Instant = Instant.parse("2026-01-01T00:00:00Z"),
        location: KLocation = KLocation(x = 0.0, y = 64.0, z = 0.0, worldName = "world"),
        items: List<StringFormatObject> = emptyList()
    ): DefaultSoul {
        return DefaultSoul(
            ownerUUID = ownerUUID,
            ownerLastName = "Steve",
            createdAt = createdAt,
            isFree = false,
            location = location,
            exp = 10,
            items = items
        )
    }

    private suspend fun SoulsDao.insertSoulOrFail(soul: DefaultSoul): DatabaseSoul {
        return insertSoul(soul).getOrThrow()
    }

    private fun TestScope.collectChangeEvents(dao: SoulsDao): List<Unit> {
        val events = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            dao.getSoulsChangeFlow().toList(events)
        }
        return events
    }

    private fun countStoredItems(): Long {
        return transaction(database) {
            SoulItemsTable.selectAll().count()
        }
    }

    @Test
    fun GIVEN_two_souls_WHEN_get_soul_by_id_THEN_returns_soul_with_requested_id() = runTest {
        val dao = createDao()
        val olderOwner = UUID.randomUUID()
        val newerOwner = UUID.randomUUID()
        val older = dao.insertSoulOrFail(
            createSoul(ownerUUID = olderOwner, createdAt = Instant.parse("2026-01-01T00:00:00Z"))
        )
        val newer = dao.insertSoulOrFail(
            createSoul(ownerUUID = newerOwner, createdAt = Instant.parse("2026-02-01T00:00:00Z"))
        )

        val olderResult = dao.getSoul(older.id).getOrThrow()
        val newerResult = dao.getSoul(newer.id).getOrThrow()

        assertEquals(older.id, olderResult.id)
        assertEquals(olderOwner, olderResult.ownerUUID)
        assertEquals(newer.id, newerResult.id)
        assertEquals(newerOwner, newerResult.ownerUUID)
    }

    @Test
    fun GIVEN_no_soul_with_id_WHEN_get_soul_THEN_returns_soul_not_found_failure() = runTest {
        val dao = createDao()
        dao.insertSoulOrFail(createSoul())

        val result = dao.getSoul(id = 9999L)

        assertIs<SoulNotFoundException>(result.exceptionOrNull())
    }

    @Test
    fun GIVEN_souls_with_and_without_items_WHEN_read_THEN_has_items_reflects_stored_items() = runTest {
        val dao = createDao()
        val withItems = dao.insertSoulOrFail(
            createSoul(items = listOf(StringFormatObject("sword"), StringFormatObject("shield")))
        )
        val withoutItems = dao.insertSoulOrFail(createSoul())

        assertTrue(withItems.hasItems)
        assertFalse(withoutItems.hasItems)

        val soulsById = dao.getSouls().getOrThrow().associateBy { soul -> soul.id }
        assertEquals(true, soulsById.getValue(withItems.id).hasItems)
        assertEquals(false, soulsById.getValue(withoutItems.id).hasItems)
        assertTrue(dao.getSoul(withItems.id).getOrThrow().hasItems)
        assertFalse(dao.getSoul(withoutItems.id).getOrThrow().hasItems)
    }

    @Test
    fun GIVEN_soul_with_items_WHEN_to_item_database_soul_THEN_returns_stored_items_in_order() = runTest {
        val dao = createDao()
        val items = listOf(StringFormatObject("sword"), StringFormatObject("shield"), StringFormatObject("apple"))
        val soul = dao.insertSoulOrFail(createSoul(items = items))

        val itemSoul = dao.toItemDatabaseSoul(soul).getOrThrow()

        assertEquals(soul.id, itemSoul.id)
        assertEquals(items, itemSoul.items)
        assertTrue(itemSoul.hasItems)
    }

    @Test
    fun GIVEN_change_flow_collector_WHEN_souls_are_read_THEN_no_change_event_is_emitted() = runTest {
        val dao = createDao()
        val soul = dao.insertSoulOrFail(createSoul(items = listOf(StringFormatObject("sword"))))
        val events = collectChangeEvents(dao)

        dao.getSouls().getOrThrow()
        dao.getSoul(soul.id).getOrThrow()
        dao.getPlayerSouls(soul.ownerUUID).getOrThrow()
        dao.getSoulsNear(soul.location, radius = 5).getOrThrow()
        dao.toItemDatabaseSoul(soul).getOrThrow()
        advanceUntilIdle()

        assertEquals(0, events.size)
    }

    @Test
    fun GIVEN_change_flow_collector_WHEN_soul_is_written_THEN_change_event_is_emitted_per_write() = runTest {
        val dao = createDao()
        val events = collectChangeEvents(dao)

        val soul = dao.insertSoulOrFail(createSoul())
        advanceUntilIdle()
        assertEquals(1, events.size)

        dao.updateSoul(soul.copy(isFree = true)).getOrThrow()
        advanceUntilIdle()
        assertEquals(2, events.size)

        val itemSoul = dao.toItemDatabaseSoul(soul).getOrThrow()
        dao.updateSoul(itemSoul.copy(items = listOf(StringFormatObject("sword")))).getOrThrow()
        advanceUntilIdle()
        assertEquals(3, events.size)

        dao.deleteSoul(soul.id).getOrThrow()
        advanceUntilIdle()
        assertEquals(4, events.size)
    }

    @Test
    fun GIVEN_souls_in_several_worlds_WHEN_get_souls_near_THEN_returns_same_world_souls_within_radius() =
        runTest {
            val dao = createDao()
            val center = KLocation(x = 0.0, y = 64.0, z = 0.0, worldName = "world")
            val atCenter = dao.insertSoulOrFail(createSoul(location = center))
            val nearby = dao.insertSoulOrFail(createSoul(location = center.copy(x = 3.0)))
            dao.insertSoulOrFail(createSoul(location = center.copy(x = 10.0)))
            dao.insertSoulOrFail(createSoul(location = center.copy(x = 4.0, z = 4.0)))
            dao.insertSoulOrFail(createSoul(location = center.copy(worldName = "world_nether")))

            val nearSouls = dao.getSoulsNear(center, radius = 5).getOrThrow()

            assertEquals(setOf(atCenter.id, nearby.id), nearSouls.map { soul -> soul.id }.toSet())
        }

    @Test
    fun GIVEN_souls_with_different_created_at_WHEN_get_souls_THEN_newest_comes_first() = runTest {
        val dao = createDao()
        val oldest = dao.insertSoulOrFail(createSoul(createdAt = Instant.parse("2026-01-01T00:00:00Z")))
        val newest = dao.insertSoulOrFail(createSoul(createdAt = Instant.parse("2026-03-01T00:00:00Z")))
        val middle = dao.insertSoulOrFail(createSoul(createdAt = Instant.parse("2026-02-01T00:00:00Z")))

        val souls = dao.getSouls().getOrThrow()

        assertEquals(listOf(newest.id, middle.id, oldest.id), souls.map { soul -> soul.id })
    }

    @Test
    fun GIVEN_souls_of_two_players_WHEN_get_player_souls_THEN_returns_only_requested_player_souls() = runTest {
        val dao = createDao()
        val steve = UUID.randomUUID()
        val alex = UUID.randomUUID()
        val steveFirst = dao.insertSoulOrFail(createSoul(ownerUUID = steve))
        val steveSecond = dao.insertSoulOrFail(createSoul(ownerUUID = steve))
        dao.insertSoulOrFail(createSoul(ownerUUID = alex))

        val steveSouls = dao.getPlayerSouls(steve).getOrThrow()

        assertEquals(setOf(steveFirst.id, steveSecond.id), steveSouls.map { soul -> soul.id }.toSet())
        assertTrue(steveSouls.all { soul -> soul.ownerUUID == steve })
    }

    @Test
    fun GIVEN_soul_WHEN_free_flag_and_exp_updated_THEN_get_soul_returns_updated_values() = runTest {
        val dao = createDao()
        val soul = dao.insertSoulOrFail(createSoul())

        dao.updateSoul(soul.copy(isFree = true, exp = 0)).getOrThrow()

        val updated = dao.getSoul(soul.id).getOrThrow()
        assertTrue(updated.isFree)
        assertEquals(0, updated.exp)
    }

    @Test
    fun GIVEN_soul_with_items_WHEN_items_updated_THEN_items_are_replaced_and_has_items_follows() = runTest {
        val dao = createDao()
        val soul = dao.insertSoulOrFail(
            createSoul(items = listOf(StringFormatObject("sword"), StringFormatObject("shield")))
        )
        val itemSoul = dao.toItemDatabaseSoul(soul).getOrThrow()

        dao.updateSoul(itemSoul.copy(items = listOf(StringFormatObject("apple")))).getOrThrow()
        assertEquals(listOf(StringFormatObject("apple")), dao.toItemDatabaseSoul(soul).getOrThrow().items)
        assertTrue(dao.getSoul(soul.id).getOrThrow().hasItems)

        dao.updateSoul(itemSoul.copy(items = emptyList())).getOrThrow()
        assertEquals(emptyList(), dao.toItemDatabaseSoul(soul).getOrThrow().items)
        assertFalse(dao.getSoul(soul.id).getOrThrow().hasItems)
    }

    @Test
    fun GIVEN_two_souls_with_items_WHEN_one_deleted_THEN_only_its_items_and_row_are_removed() = runTest {
        val dao = createDao()
        val deleted = dao.insertSoulOrFail(createSoul(items = listOf(StringFormatObject("sword"))))
        val kept = dao.insertSoulOrFail(
            createSoul(items = listOf(StringFormatObject("shield"), StringFormatObject("apple")))
        )

        dao.deleteSoul(deleted.id).getOrThrow()

        assertEquals(listOf(kept.id), dao.getSouls().getOrThrow().map { soul -> soul.id })
        assertIs<SoulNotFoundException>(dao.getSoul(deleted.id).exceptionOrNull())
        assertEquals(2L, countStoredItems())
        assertEquals(2, dao.toItemDatabaseSoul(kept).getOrThrow().items.size)
    }

    @Test
    fun GIVEN_cancelled_caller_WHEN_get_souls_THEN_cancellation_is_propagated_instead_of_failure_result() = runTest {
        val dao = createDao()
        var propagated: Throwable? = null

        val job = launch {
            cancelSelf()
            try {
                dao.getSouls()
            } catch (cancellation: CancellationException) {
                propagated = cancellation
            }
        }
        job.join()

        assertIs<CancellationException>(propagated)
    }

    private fun CoroutineScope.cancelSelf() {
        cancel()
    }
}
