@file:Suppress("FunctionNaming", "TestOwnerBehaviour")

package ru.astrainteractive.soulkeeper.data.dao.soul.model

import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.soulkeeper.data.dao.soul.util.hasExp
import ru.astrainteractive.soulkeeper.data.dao.soul.util.withItems
import ru.astrainteractive.soulkeeper.data.dao.soul.util.withoutExp
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class ItemDatabaseSoulTest {
    private fun soul(exp: Int, items: List<StringFormatObject>): ItemDatabaseSoul {
        return ItemDatabaseSoul(
            id = 1L,
            ownerUUID = UUID.fromString("0f8fad5b-d9cb-469f-a165-70867728950e"),
            ownerLastName = "Owner",
            createdAt = Instant.fromEpochSeconds(0),
            isFree = false,
            location = KLocation(
                x = 0.0,
                y = 64.0,
                z = 0.0,
                worldName = "world"
            ),
            hasItems = items.isNotEmpty(),
            exp = exp,
            items = items
        )
    }

    @Test
    fun GIVEN_soul_with_exp_WHEN_checked_THEN_it_has_exp() {
        assertTrue(soul(exp = 5, items = emptyList()).hasExp())
    }

    @Test
    fun GIVEN_soul_without_exp_WHEN_checked_THEN_it_has_no_exp() {
        assertFalse(soul(exp = 0, items = emptyList()).hasExp())
    }

    @Test
    fun GIVEN_soul_with_exp_WHEN_exp_is_taken_THEN_only_exp_becomes_zero() {
        val items = listOf(StringFormatObject("stone"))
        val soul = soul(exp = 5, items = items)

        val soulWithoutExp = soul.withoutExp()

        assertEquals(0, soulWithoutExp.exp)
        assertEquals(items, soulWithoutExp.items)
    }

    @Test
    fun GIVEN_soul_with_items_WHEN_all_items_are_taken_THEN_it_has_no_items() {
        val soul = soul(exp = 0, items = listOf(StringFormatObject("stone")))

        val emptySoul = soul.withItems(emptyList())

        assertEquals(emptyList(), emptySoul.items)
        assertFalse(emptySoul.hasItems)
    }

    @Test
    fun GIVEN_soul_without_items_WHEN_items_are_put_back_THEN_it_has_items() {
        val leftovers = listOf(StringFormatObject("dirt"))

        val soul = soul(exp = 0, items = emptyList()).withItems(leftovers)

        assertEquals(leftovers, soul.items)
        assertTrue(soul.hasItems)
    }
}
