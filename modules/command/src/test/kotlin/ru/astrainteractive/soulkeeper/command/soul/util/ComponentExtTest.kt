@file:Suppress("FunctionNaming")

package ru.astrainteractive.soulkeeper.command.soul.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import kotlin.test.Test
import kotlin.test.assertEquals

class ComponentExtTest {
    private fun Component.plain(): String = PlainTextComponentSerializer.plainText().serialize(this)

    @Test
    fun GIVEN_missing_button_WHEN_appended_THEN_entry_is_unchanged() {
        assertEquals("entry", Component.text("entry").append(other = null, addSpace = true).plain())
    }

    @Test
    fun GIVEN_empty_button_WHEN_appended_THEN_entry_is_unchanged() {
        assertEquals("entry", Component.text("entry").append(other = Component.empty(), addSpace = true).plain())
    }

    @Test
    fun GIVEN_button_WHEN_appended_with_space_THEN_space_separates_them() {
        val entry = Component.text("entry").append(other = Component.text("[TP]"), addSpace = true)

        assertEquals("entry [TP]", entry.plain())
    }

    @Test
    fun GIVEN_button_WHEN_appended_without_space_THEN_they_are_joined() {
        val entry = Component.text("entry").append(other = Component.text("[TP]"), addSpace = false)

        assertEquals("entry[TP]", entry.plain())
    }
}
