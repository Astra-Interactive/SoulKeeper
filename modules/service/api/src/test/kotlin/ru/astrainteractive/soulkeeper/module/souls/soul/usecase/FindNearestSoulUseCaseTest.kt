@file:Suppress("FunctionNaming", "ExpressionBodyOnOneLine", "ArgumentPerLine", "ExplicitInvoke")

package ru.astrainteractive.soulkeeper.module.souls.soul.usecase

import kotlinx.coroutines.test.runTest
import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.soulkeeper.data.dao.soul.fake.FakeSoulRepository
import ru.astrainteractive.soulkeeper.data.dao.soul.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.data.dao.soul.model.SoulId
import ru.astrainteractive.soulkeeper.module.souls.soul.fake.FakeOnlineKPlayer
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class FindNearestSoulUseCaseTest {
    private val location = KLocation(x = 0.0, y = 64.0, z = 0.0, worldName = "world")
    private val player = FakeOnlineKPlayer(
        uuid = UUID.fromString("0f8fad5b-d9cb-469f-a165-70867728950e"),
        location = location
    )
    private val stranger = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7")
    private val soulRepository = FakeSoulRepository()
    private val useCase = FindNearestSoulUseCase(soulRepository)

    private fun soul(id: SoulId, owner: UUID, isFree: Boolean): DatabaseSoul = DatabaseSoul(
        id = id,
        ownerUUID = owner,
        ownerLastName = "Owner",
        createdAt = Instant.fromEpochSeconds(0),
        isFree = isFree,
        location = location,
        hasItems = true,
        exp = 0
    )

    @Test
    fun GIVEN_private_soul_of_stranger_and_own_soul_WHEN_looked_up_THEN_returns_own_soul() = runTest {
        val ownSoul = soul(id = SoulId(2L), owner = player.uuid, isFree = false)
        soulRepository.soulsNear = listOf(soul(id = SoulId(1L), owner = stranger, isFree = false), ownSoul)

        assertEquals(ownSoul, useCase(player))
    }

    @Test
    fun GIVEN_free_soul_of_stranger_WHEN_looked_up_THEN_returns_it() = runTest {
        val freeSoul = soul(id = SoulId(1L), owner = stranger, isFree = true)
        soulRepository.soulsNear = listOf(freeSoul)

        assertEquals(freeSoul, useCase(player))
    }

    @Test
    fun GIVEN_only_private_souls_of_strangers_WHEN_looked_up_THEN_returns_nothing() = runTest {
        soulRepository.soulsNear = listOf(soul(id = SoulId(1L), owner = stranger, isFree = false))

        assertNull(useCase(player))
    }

    @Test
    fun GIVEN_storage_failure_WHEN_looked_up_THEN_returns_nothing() = runTest {
        soulRepository.soulsNearFailure = IllegalStateException("Database is down")

        assertNull(useCase(player))
    }
}
