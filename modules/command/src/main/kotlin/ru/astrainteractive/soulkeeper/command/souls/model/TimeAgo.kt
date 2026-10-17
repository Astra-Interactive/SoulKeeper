package ru.astrainteractive.soulkeeper.command.souls.model

import kotlin.time.Duration

internal sealed interface TimeAgo {
    val duration: Duration

    data class SecondsAgo(override val duration: Duration) : TimeAgo
    data class MinuteAgo(override val duration: Duration) : TimeAgo
    data class HourAgo(override val duration: Duration) : TimeAgo
    data class DayAgo(override val duration: Duration) : TimeAgo
    data class MonthAgo(override val duration: Duration) : TimeAgo
}
