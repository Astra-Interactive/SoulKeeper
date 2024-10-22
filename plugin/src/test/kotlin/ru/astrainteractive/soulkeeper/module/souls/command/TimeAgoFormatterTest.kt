package ru.astrainteractive.soulkeeper.module.souls.command

import org.junit.Test
import ru.astrainteractive.soulkeeper.command.TimeAgoFormatter
import java.time.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TimeAgoFormatterTest {
    @Test
    fun test() {
        assert(TimeAgoFormatter.format(Instant.now()) is TimeAgoFormatter.Format.SecondsAgo)
        assert(
            TimeAgoFormatter.format(
                Instant.now().minusSeconds(61.seconds.inWholeSeconds)
            ) is TimeAgoFormatter.Format.MinuteAgo
        )
        assert(
            TimeAgoFormatter.format(
                Instant.now().minusSeconds(61.minutes.inWholeSeconds)
            ) is TimeAgoFormatter.Format.HourAgo
        )
        assert(
            TimeAgoFormatter.format(
                Instant.now().minusSeconds(2.days.inWholeSeconds)
            ) is TimeAgoFormatter.Format.DayAgo
        )
        assert(
            TimeAgoFormatter.format(
                Instant.now().minusSeconds(64.days.inWholeSeconds)
            ) is TimeAgoFormatter.Format.MonthAgo
        )
    }
}
