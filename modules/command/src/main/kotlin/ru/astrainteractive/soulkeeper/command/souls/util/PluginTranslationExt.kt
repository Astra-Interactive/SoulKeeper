package ru.astrainteractive.soulkeeper.command.souls.util

import ru.astrainteractive.astralibs.localization.component.LocalizableComponent
import ru.astrainteractive.soulkeeper.command.souls.model.TimeAgo
import ru.astrainteractive.soulkeeper.core.plugin.model.PluginTranslation

internal fun PluginTranslation.format(elapsed: TimeAgo): LocalizableComponent {
    return when (elapsed) {
        is TimeAgo.DayAgo -> timeAgo.days(elapsed.duration)
        is TimeAgo.HourAgo -> timeAgo.hours(elapsed.duration)
        is TimeAgo.MinuteAgo -> timeAgo.minutes(elapsed.duration)
        is TimeAgo.MonthAgo -> timeAgo.months(elapsed.duration)
        is TimeAgo.SecondsAgo -> timeAgo.seconds(elapsed.duration)
    }
}
