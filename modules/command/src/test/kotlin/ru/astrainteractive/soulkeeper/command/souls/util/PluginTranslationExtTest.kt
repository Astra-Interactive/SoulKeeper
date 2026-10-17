@file:Suppress("FunctionNaming")

package ru.astrainteractive.soulkeeper.command.souls.util

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import ru.astrainteractive.soulkeeper.command.souls.model.TimeAgo
import ru.astrainteractive.soulkeeper.core.plugin.model.PluginTranslation
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class PluginTranslationExtTest {
    private val translation = PluginTranslation()

    private fun plain(elapsed: TimeAgo): String {
        val component = translation.format(elapsed).toComponent(Locale.US)
        return PlainTextComponentSerializer.plainText().serialize(component)
    }

    @Test
    fun GIVEN_seconds_ago_WHEN_formatted_THEN_shows_seconds() {
        assertEquals("15 seconds ago", plain(TimeAgo.SecondsAgo(15.seconds)))
    }

    @Test
    fun GIVEN_minutes_ago_WHEN_formatted_THEN_shows_minutes() {
        assertEquals("3 minutes ago", plain(TimeAgo.MinuteAgo(3.minutes)))
    }

    @Test
    fun GIVEN_hours_ago_WHEN_formatted_THEN_shows_hours() {
        assertEquals("5 hours ago", plain(TimeAgo.HourAgo(5.hours)))
    }

    @Test
    fun GIVEN_days_ago_WHEN_formatted_THEN_shows_days() {
        assertEquals("2 days ago", plain(TimeAgo.DayAgo(2.days)))
    }

    @Test
    fun GIVEN_months_ago_WHEN_formatted_THEN_shows_whole_months() {
        assertEquals("2 months ago", plain(TimeAgo.MonthAgo(64.days)))
    }
}
