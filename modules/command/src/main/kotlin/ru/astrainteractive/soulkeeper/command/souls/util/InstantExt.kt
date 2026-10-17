package ru.astrainteractive.soulkeeper.command.souls.util

import ru.astrainteractive.soulkeeper.command.souls.model.TimeAgo
import java.time.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal fun Instant.timeAgo(now: Instant): TimeAgo {
    val passedTime = now.minusSeconds(epochSecond).epochSecond.seconds
    return when {
        passedTime > 30.days -> TimeAgo.MonthAgo(passedTime)
        passedTime > 1.days -> TimeAgo.DayAgo(passedTime)
        passedTime > 1.hours -> TimeAgo.HourAgo(passedTime)
        passedTime > 1.minutes -> TimeAgo.MinuteAgo(passedTime)
        else -> TimeAgo.SecondsAgo(passedTime)
    }
}
