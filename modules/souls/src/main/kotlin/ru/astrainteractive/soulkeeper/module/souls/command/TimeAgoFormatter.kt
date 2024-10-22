package ru.astrainteractive.soulkeeper.module.souls.command

import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal object TimeAgoFormatter {
    sealed interface Format {
        val duration: Duration

        data class SecondsAgo(override val duration: Duration) : Format
        data class MinuteAgo(override val duration: Duration) : Format
        data class HourAgo(override val duration: Duration) : Format
        data class DayAgo(override val duration: Duration) : Format
        data class MonthAgo(override val duration: Duration) : Format
    }

    fun format(instant: Instant): Format {
        val now = Instant.now()
        val passedTime = now.minusSeconds(instant.epochSecond)
        return when {
            passedTime.epochSecond > 30.days.inWholeSeconds -> {
                Format.MonthAgo(passedTime.epochSecond.seconds)
            }

            passedTime.epochSecond > 1.days.inWholeSeconds -> {
                Format.DayAgo(passedTime.epochSecond.seconds)
            }

            passedTime.epochSecond > 1.hours.inWholeSeconds -> {
                Format.HourAgo(passedTime.epochSecond.seconds)
            }

            passedTime.epochSecond > 1.minutes.inWholeSeconds -> {
                Format.MinuteAgo(passedTime.epochSecond.seconds)
            }

            else -> {
                Format.SecondsAgo(passedTime.epochSecond.seconds)
            }
        }
    }
}
