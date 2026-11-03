@file:Suppress("FunctionNaming")

package ru.astrainteractive.soulkeeper.data.dao.soul.model

import ru.astrainteractive.astralibs.server.location.KLocation
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class SoulTest {
    private val ownerUuid = UUID.fromString("0f8fad5b-d9cb-469f-a165-70867728950e")
    private val strangerUuid = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7")

    private fun soul(isFree: Boolean): DatabaseSoul {
        return DatabaseSoul(
            id = SoulId(1L),
            ownerUUID = ownerUuid,
            ownerLastName = "Owner",
            createdAt = Instant.fromEpochSeconds(0),
            isFree = isFree,
            location = KLocation(
                x = 0.0,
                y = 64.0,
                z = 0.0,
                worldName = "world"
            ),
            hasItems = false,
            exp = 0
        )
    }

    @Test
    fun GIVEN_private_soul_WHEN_owner_asks_THEN_soul_is_available() {
        assertTrue(soul(isFree = false).isAvailableTo(ownerUuid))
    }

    @Test
    fun GIVEN_private_soul_WHEN_stranger_asks_THEN_soul_is_not_available() {
        assertFalse(soul(isFree = false).isAvailableTo(strangerUuid))
    }

    @Test
    fun GIVEN_free_soul_WHEN_stranger_asks_THEN_soul_is_available() {
        assertTrue(soul(isFree = true).isAvailableTo(strangerUuid))
    }
}
