@file:Suppress("FunctionNaming")

package ru.astrainteractive.soulkeeper.module.souls.soul.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.klibs.kstorage.api.asCachedKrate
import ru.astrainteractive.klibs.kstorage.api.impl.DefaultMutableKrate
import ru.astrainteractive.soulkeeper.core.platform.api.Experienced
import ru.astrainteractive.soulkeeper.core.platform.fake.FakeEffectEmitter
import ru.astrainteractive.soulkeeper.core.platform.fake.FakeExperienced
import ru.astrainteractive.soulkeeper.core.plugin.model.SoulsConfig
import ru.astrainteractive.soulkeeper.data.dao.soul.fake.FakeSoulRepository
import ru.astrainteractive.soulkeeper.data.dao.soul.model.ItemDatabaseSoul
import ru.astrainteractive.soulkeeper.data.dao.soul.model.StringFormatObject
import ru.astrainteractive.soulkeeper.module.souls.soul.fake.FakeOnlineKPlayer
import ru.astrainteractive.soulkeeper.module.souls.soul.fake.FakePlayerInventory
import ru.astrainteractive.soulkeeper.module.souls.soul.model.PickUpSoulResult
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class PickUpSoulUseCaseTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val config = SoulsConfig()
    private val soulRepository = FakeSoulRepository()
    private val playerInventory = FakePlayerInventory()
    private val effectEmitter = FakeEffectEmitter()
    private val experienced = FakeExperienced()
    private val location = KLocation(x = 0.0, y = 64.0, z = 0.0, worldName = "world")
    private val player = FakeOnlineKPlayer(
        uuid = UUID.fromString("0f8fad5b-d9cb-469f-a165-70867728950e"),
        location = location
    )
    private val useCase = PickUpSoulUseCase(
        soulRepository = soulRepository,
        playerInventory = playerInventory,
        experiencedFactory = Experienced.Resolver<OnlineKPlayer> { experienced },
        effectEmitter = effectEmitter,
        dispatchers = TestKotlinDispatchers(dispatcher),
        soulsConfigKrate = DefaultMutableKrate(factory = { config }, loader = { null }).asCachedKrate()
    )

    private fun soul(exp: Int, items: List<StringFormatObject>): ItemDatabaseSoul = ItemDatabaseSoul(
        id = 7L,
        ownerUUID = player.uuid,
        ownerLastName = player.name,
        createdAt = Instant.fromEpochSeconds(0),
        isFree = false,
        location = location,
        hasItems = items.isNotEmpty(),
        exp = exp,
        items = items
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun GIVEN_soul_with_exp_only_WHEN_picked_up_THEN_player_gets_exp_and_soul_is_gone() = runTest(dispatcher) {
        val result = useCase(player, soul(exp = 12, items = emptyList()))

        assertEquals(PickUpSoulResult.AllPickedUp, result)
        assertEquals(listOf(12), experienced.givenExperience)
        assertEquals(listOf(7L), soulRepository.deletedSoulIds)
        assertEquals(
            listOf(config.sounds.collectXp, config.sounds.soulDisappear),
            effectEmitter.playedSounds
        )
        assertEquals(listOf(config.particles.soulGone), effectEmitter.spawnedParticles)
    }

    @Test
    fun GIVEN_soul_without_exp_WHEN_picked_up_THEN_no_exp_is_given() = runTest(dispatcher) {
        useCase(player, soul(exp = 0, items = emptyList()))

        assertTrue(experienced.givenExperience.isEmpty())
        assertTrue(config.sounds.collectXp !in effectEmitter.playedSounds)
    }

    @Test
    fun GIVEN_items_that_fit_WHEN_picked_up_THEN_items_are_given_and_soul_is_gone() = runTest(dispatcher) {
        val items = listOf(StringFormatObject("stone"), StringFormatObject("dirt"))

        val result = useCase(player, soul(exp = 0, items = items))

        assertEquals(PickUpSoulResult.AllPickedUp, result)
        assertEquals(items, playerInventory.receivedItems)
        assertEquals(listOf(7L), soulRepository.deletedSoulIds)
        assertTrue(config.sounds.collectItem in effectEmitter.playedSounds)
    }

    @Test
    fun GIVEN_full_inventory_for_some_items_WHEN_picked_up_THEN_leftovers_stay_in_soul() = runTest(dispatcher) {
        playerInventory.freeSlots = 1
        val items = listOf(StringFormatObject("stone"), StringFormatObject("dirt"))

        val result = useCase(player, soul(exp = 3, items = items))

        assertEquals(PickUpSoulResult.SomethingRest, result)
        assertEquals(listOf(StringFormatObject("dirt")), soulRepository.updatedSouls.last().items)
        assertEquals(0, soulRepository.updatedSouls.last().exp)
        assertTrue(soulRepository.deletedSoulIds.isEmpty())
        assertTrue(config.sounds.collectItem in effectEmitter.playedSounds)
        assertTrue(config.sounds.soulContentLeft in effectEmitter.playedSounds)
        assertEquals(listOf(config.particles.soulContentLeft), effectEmitter.spawnedParticles)
    }

    @Test
    fun GIVEN_full_inventory_WHEN_picked_up_THEN_no_collect_sound_and_soul_stays() = runTest(dispatcher) {
        playerInventory.freeSlots = 0
        val items = listOf(StringFormatObject("stone"))

        val result = useCase(player, soul(exp = 0, items = items))

        assertEquals(PickUpSoulResult.SomethingRest, result)
        assertTrue(config.sounds.collectItem !in effectEmitter.playedSounds)
        assertTrue(soulRepository.updatedSouls.isEmpty())
        assertTrue(soulRepository.deletedSoulIds.isEmpty())
    }

    @Test
    fun GIVEN_player_who_cannot_pick_up_WHEN_picked_up_THEN_items_stay_in_soul() = runTest(dispatcher) {
        playerInventory.canPickUp = false

        val result = useCase(player, soul(exp = 0, items = listOf(StringFormatObject("stone"))))

        assertEquals(PickUpSoulResult.SomethingRest, result)
        assertTrue(playerInventory.receivedItems.isEmpty())
        assertTrue(soulRepository.deletedSoulIds.isEmpty())
        assertEquals(listOf(config.sounds.soulContentLeft), effectEmitter.playedSounds)
    }
}
