package ru.astrainteractive.soulkeeper.command.soul.util

import ru.astrainteractive.soulkeeper.command.soul.model.TimeAgo
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

internal fun Instant.timeAgo(now: Instant): TimeAgo {
    val passedTime = now.minus(this@timeAgo)
    return when {
        passedTime > 30.days -> TimeAgo.MonthAgo(passedTime)
        passedTime > 1.days -> TimeAgo.DayAgo(passedTime)
        passedTime > 1.hours -> TimeAgo.HourAgo(passedTime)
        passedTime > 1.minutes -> TimeAgo.MinuteAgo(passedTime)
        else -> TimeAgo.SecondsAgo(passedTime)
    }
}
