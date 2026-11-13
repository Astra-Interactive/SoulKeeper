package ru.astrainteractive.soulkeeper.command.soul.util

import ru.astrainteractive.astralibs.localization.component.LocalizableComponent
import ru.astrainteractive.soulkeeper.command.soul.model.TimeAgo
import ru.astrainteractive.soulkeeper.core.plugin.model.PluginTranslation

internal fun formatTimeAgo(translation: PluginTranslation, elapsed: TimeAgo): LocalizableComponent {
    return when (elapsed) {
        is TimeAgo.DayAgo -> translation.timeAgo.days(elapsed.duration)
        is TimeAgo.HourAgo -> translation.timeAgo.hours(elapsed.duration)
        is TimeAgo.MinuteAgo -> translation.timeAgo.minutes(elapsed.duration)
        is TimeAgo.MonthAgo -> translation.timeAgo.months(elapsed.duration)
        is TimeAgo.SecondsAgo -> translation.timeAgo.seconds(elapsed.duration)
    }
}
