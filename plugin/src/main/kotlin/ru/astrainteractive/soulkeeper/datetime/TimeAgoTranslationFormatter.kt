package ru.astrainteractive.soulkeeper.datetime

import ru.astrainteractive.astralibs.string.StringDesc
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation

internal class TimeAgoTranslationFormatter(private val translation: PluginTranslation) {
    fun format(timeAgo: TimeAgoFormatter.Format): StringDesc {
        return when (timeAgo) {
            is TimeAgoFormatter.Format.DayAgo -> translation.souls.daysAgoFormat(timeAgo.duration)
            is TimeAgoFormatter.Format.HourAgo -> translation.souls.hoursAgoFormat(timeAgo.duration)
            is TimeAgoFormatter.Format.MinuteAgo -> translation.souls.minutesAgoFormat(
                timeAgo.duration
            )

            is TimeAgoFormatter.Format.MonthAgo -> translation.souls.monthsAgoFormat(
                timeAgo.duration
            )

            is TimeAgoFormatter.Format.SecondsAgo -> translation.souls.secondsAgoFormat(
                timeAgo.duration
            )
        }
    }
}
