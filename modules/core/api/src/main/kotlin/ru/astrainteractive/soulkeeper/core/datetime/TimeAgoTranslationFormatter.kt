package ru.astrainteractive.soulkeeper.core.datetime

import ru.astrainteractive.astralibs.localization.component.LocalizableComponent
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation

class TimeAgoTranslationFormatter(private val translation: PluginTranslation) {
    fun format(timeAgo: TimeAgoFormatter.Format): LocalizableComponent {
        return when (timeAgo) {
            is TimeAgoFormatter.Format.DayAgo -> translation.timeAgo.days(timeAgo.duration)
            is TimeAgoFormatter.Format.HourAgo -> translation.timeAgo.hours(timeAgo.duration)
            is TimeAgoFormatter.Format.MinuteAgo -> translation.timeAgo.minutes(
                timeAgo.duration
            )

            is TimeAgoFormatter.Format.MonthAgo -> translation.timeAgo.months(
                timeAgo.duration
            )

            is TimeAgoFormatter.Format.SecondsAgo -> translation.timeAgo.seconds(
                timeAgo.duration
            )
        }
    }
}
