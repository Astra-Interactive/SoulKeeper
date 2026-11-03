@file:Suppress("FunctionNaming")

package ru.astrainteractive.soulkeeper.command.soul.util

import ru.astrainteractive.soulkeeper.command.soul.model.TimeAgo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class InstantExtTest {
    private val now = Instant.fromEpochSeconds(1_000_000_000L)

    private fun createdBefore(duration: Duration): Instant = now.minus(duration)

    @Test
    fun GIVEN_soul_created_seconds_ago_WHEN_time_ago_THEN_returns_seconds() {
        assertEquals(TimeAgo.SecondsAgo(30.seconds), createdBefore(30.seconds).timeAgo(now))
    }

    @Test
    fun GIVEN_soul_created_exactly_a_minute_ago_WHEN_time_ago_THEN_still_returns_seconds() {
        assertEquals(TimeAgo.SecondsAgo(60.seconds), createdBefore(1.minutes).timeAgo(now))
    }

    @Test
    fun GIVEN_soul_created_minutes_ago_WHEN_time_ago_THEN_returns_minutes() {
        assertEquals(TimeAgo.MinuteAgo(61.seconds), createdBefore(61.seconds).timeAgo(now))
    }

    @Test
    fun GIVEN_soul_created_hours_ago_WHEN_time_ago_THEN_returns_hours() {
        assertEquals(TimeAgo.HourAgo(61.minutes), createdBefore(61.minutes).timeAgo(now))
    }

    @Test
    fun GIVEN_soul_created_days_ago_WHEN_time_ago_THEN_returns_days() {
        assertEquals(TimeAgo.DayAgo(2.days), createdBefore(2.days).timeAgo(now))
    }

    @Test
    fun GIVEN_soul_created_months_ago_WHEN_time_ago_THEN_returns_months() {
        assertEquals(TimeAgo.MonthAgo(64.days), createdBefore(64.days).timeAgo(now))
    }

    @Test
    fun GIVEN_soul_created_a_day_and_hour_ago_WHEN_time_ago_THEN_keeps_whole_duration() {
        val duration = 1.days + 1.hours

        assertEquals(TimeAgo.DayAgo(duration), createdBefore(duration).timeAgo(now))
    }
}
