@file:Suppress("LongParameterList")

package ru.astrainteractive.soulkeeper.core.plugin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.astrainteractive.astralibs.localization.component.LocalizableComponent
import ru.astrainteractive.astralibs.localization.component.PlaceholderReplacement
import ru.astrainteractive.astralibs.localization.component.replace
import ru.astrainteractive.astralibs.localization.component.replaceAll
import ru.astrainteractive.astralibs.localization.locale.MinecraftLocales
import ru.astrainteractive.astralibs.localization.text.LocalizedText
import kotlin.time.Duration

/**
 * Texts of the plugin, grouped by the feature that sends them. Every text has a default, so the plugin works
 * without `translations.yml` and a missing key keeps its default.
 */
@Serializable
data class PluginTranslation(
    @SerialName("command_error")
    val commandError: CommandError = CommandError(),
    @SerialName("reload")
    val reload: Reload = Reload(),
    @SerialName("soul_list")
    val soulList: SoulList = SoulList(),
    @SerialName("time_ago")
    val timeAgo: TimeAgo = TimeAgo(),
    @SerialName("soul")
    val soul: Soul = Soul()
) {
    /** Failures any command can report. */
    @Serializable
    data class CommandError(
        @SerialName("no_permission")
        val noPermission: LocalizedText = PREFIX.concat(
            LocalizedText.build {
                translation(MinecraftLocales.EN_US, "&#db2c18You don't have permission!")
                translation(MinecraftLocales.RU_RU, "&#db2c18У вас нет прав!")
            }
        ),
        @SerialName("wrong_usage")
        val wrongUsage: LocalizedText = PREFIX.concat(
            LocalizedText.build {
                translation(MinecraftLocales.EN_US, "&#db2c18Wrong usage!")
                translation(MinecraftLocales.RU_RU, "&#db2c18Неверное использование!")
            }
        ),
        @SerialName("players_only")
        val playersOnly: LocalizedText = PREFIX.concat(
            LocalizedText.build {
                translation(MinecraftLocales.EN_US, "&#db2c18This command is for players only!")
                translation(MinecraftLocales.RU_RU, "&#db2c18Эта команда только для игроков!")
            }
        ),
        @SerialName("unknown_error")
        val unknownError: LocalizedText = PREFIX.concat(
            LocalizedText.build {
                translation(MinecraftLocales.EN_US, "&#db2c18The command failed with an unknown error")
                translation(MinecraftLocales.RU_RU, "&#db2c18Команда завершилась с неизвестной ошибкой")
            }
        )
    )

    @Serializable
    data class Reload(
        @SerialName("started")
        val started: LocalizedText = PREFIX.concat(
            LocalizedText.build {
                translation(MinecraftLocales.EN_US, "&#dbbb18Reloading the plugin")
                translation(MinecraftLocales.RU_RU, "&#dbbb18Перезагрузка плагина")
            }
        ),
        @SerialName("completed")
        val completed: LocalizedText = PREFIX.concat(
            LocalizedText.build {
                translation(MinecraftLocales.EN_US, "&#42f596Reload complete")
                translation(MinecraftLocales.RU_RU, "&#42f596Перезагрузка успешно завершена")
            }
        )
    )

    /** Sent by `/souls`: one page of the souls a sender can see, with buttons next to each soul. */
    @Serializable
    data class SoulList(
        @SerialName("title")
        val title: LocalizedText = PREFIX.concat(
            LocalizedText.build {
                translation(MinecraftLocales.EN_US, "&#42f596Souls you can see:")
                translation(MinecraftLocales.RU_RU, "&#42f596Список видимых вам душ:")
            }
        ),
        @SerialName("empty_page")
        private val emptyPage: LocalizedText = PREFIX.concat(
            LocalizedText.build {
                translation(MinecraftLocales.EN_US, "&#db2c18No souls on page %page%")
                translation(MinecraftLocales.RU_RU, "&#db2c18Нет душ на странице %page%")
            }
        ),
        @SerialName("entry")
        private val entry: LocalizedText = LocalizedText.shared(
            "&#b8b8b8%index%. &#d1a71d%owner% &#b8b8b8(%time_ago%) &#b8b8b8(%x%; %y%; %z%) %dist%m"
        ),
        @SerialName("free_button")
        val freeButton: LocalizedText = LocalizedText.shared("&#b50b05[FREE]"),
        @SerialName("teleport_button")
        val teleportButton: LocalizedText = LocalizedText.shared("&#1db2b8[TP]"),
        @SerialName("next_page")
        val nextPage: LocalizedText = LocalizedText.build {
            translation(MinecraftLocales.EN_US, "&#42f596[>>NEXT>>]")
            translation(MinecraftLocales.RU_RU, "&#42f596[>>ДАЛЬШЕ>>]")
        },
        @SerialName("previous_page")
        val previousPage: LocalizedText = LocalizedText.build {
            translation(MinecraftLocales.EN_US, "&#42f596[<<BACK<<]")
            translation(MinecraftLocales.RU_RU, "&#42f596[<<РАНЬШЕ<<]")
        }
    ) {
        fun emptyPage(page: Int): LocalizableComponent = emptyPage.replace("%page%", page.toString())

        fun entry(
            index: Int,
            owner: String,
            timeAgo: LocalizableComponent,
            distance: Int,
            x: Int,
            y: Int,
            z: Int
        ): LocalizableComponent = entry.replaceAll(
            PlaceholderReplacement.plain("%index%", "$index"),
            PlaceholderReplacement.plain("%owner%", owner),
            PlaceholderReplacement.plain("%dist%", "$distance"),
            PlaceholderReplacement(placeholder = "%time_ago%", value = timeAgo),
            PlaceholderReplacement.plain("%x%", "$x"),
            PlaceholderReplacement.plain("%y%", "$y"),
            PlaceholderReplacement.plain("%z%", "$z")
        )
    }

    /** How long ago a soul appeared, shown in its [SoulList] entry. */
    @Serializable
    data class TimeAgo(
        @SerialName("seconds")
        private val seconds: LocalizedText = LocalizedText.build {
            translation(MinecraftLocales.EN_US, "%time% seconds ago")
            translation(MinecraftLocales.RU_RU, "%time% секунд назад")
        },
        @SerialName("minutes")
        private val minutes: LocalizedText = LocalizedText.build {
            translation(MinecraftLocales.EN_US, "%time% minutes ago")
            translation(MinecraftLocales.RU_RU, "%time% минут назад")
        },
        @SerialName("hours")
        private val hours: LocalizedText = LocalizedText.build {
            translation(MinecraftLocales.EN_US, "%time% hours ago")
            translation(MinecraftLocales.RU_RU, "%time% часов назад")
        },
        @SerialName("days")
        private val days: LocalizedText = LocalizedText.build {
            translation(MinecraftLocales.EN_US, "%time% days ago")
            translation(MinecraftLocales.RU_RU, "%time% дней назад")
        },
        @SerialName("months")
        private val months: LocalizedText = LocalizedText.build {
            translation(MinecraftLocales.EN_US, "%time% months ago")
            translation(MinecraftLocales.RU_RU, "%time% месяцев назад")
        }
    ) {
        fun seconds(time: Duration): LocalizableComponent = seconds.replace("%time%", time.inWholeSeconds.toString())

        fun minutes(time: Duration): LocalizableComponent = minutes.replace("%time%", time.inWholeMinutes.toString())

        fun hours(time: Duration): LocalizableComponent = hours.replace("%time%", time.inWholeHours.toString())

        fun days(time: Duration): LocalizableComponent = days.replace("%time%", time.inWholeDays.toString())

        @Suppress("MagicNumber")
        fun months(time: Duration): LocalizableComponent = months.replace("%time%", time.inWholeDays.div(30).toString())
    }

    /** Results of actions on one soul, and the name shown above it. */
    @Serializable
    data class Soul(
        @SerialName("freed")
        val freed: LocalizedText = PREFIX.concat(
            LocalizedText.build {
                translation(MinecraftLocales.EN_US, "&#42f596The soul is free now!")
                translation(MinecraftLocales.RU_RU, "&#42f596Душа теперь свободна!")
            }
        ),
        @SerialName("free_failed")
        val freeFailed: LocalizedText = PREFIX.concat(
            LocalizedText.build {
                translation(MinecraftLocales.EN_US, "&#db2c18Could not free the soul!")
                translation(MinecraftLocales.RU_RU, "&#db2c18Не удалось освободить душу!")
            }
        ),
        @SerialName("not_found")
        val notFound: LocalizedText = PREFIX.concat(
            LocalizedText.build {
                translation(MinecraftLocales.EN_US, "&#db2c18Soul not found!")
                translation(MinecraftLocales.RU_RU, "&#db2c18Душа не найдена!")
            }
        ),
        @SerialName("name")
        private val name: LocalizedText = LocalizedText.build {
            translation(MinecraftLocales.EN_US, "&#317dd4Soul of &#31d43c%player%")
            translation(MinecraftLocales.RU_RU, "&#317dd4Душа игрока &#31d43c%player%")
        }
    ) {
        fun name(owner: String): LocalizableComponent = name.replace("%player%", owner)
    }

    companion object {
        private val PREFIX = LocalizedText.shared("&#18dbd1[SoulKeeper] ")
    }
}
