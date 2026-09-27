@file:Suppress("MaxLineLength", "MaximumLineLength", "LongParameterList")

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

@Serializable
class PluginTranslation(
    @SerialName("general")
    val general: General = General(),
    @SerialName("souls")
    val souls: Souls = Souls()
) {
    @Serializable
    data class Souls(
        @SerialName("days_ago_format")
        private val daysAgoFormat: LocalizedText = LocalizedText.build {
            translation(MinecraftLocales.RU_RU, "%time% дней назад")
            translation(MinecraftLocales.EN_US, "%time% days ago")
        },
        @SerialName("hours_ago_format")
        private val hoursAgoFormat: LocalizedText = LocalizedText.build {
            translation(MinecraftLocales.RU_RU, "%time% часов назад")
            translation(MinecraftLocales.EN_US, "%time% hours ago")
        },
        @SerialName("minutes_ago_format")
        private val minutesAgoFormat: LocalizedText = LocalizedText.build {
            translation(MinecraftLocales.RU_RU, "%time% минут назад")
            translation(MinecraftLocales.EN_US, "%time% minutes ago")
        },
        @SerialName("months_ago_format")
        private val monthsAgoFormat: LocalizedText = LocalizedText.build {
            translation(MinecraftLocales.RU_RU, "%time% месяцеев назад")
            translation(MinecraftLocales.EN_US, "%time% months ago")
        },
        @SerialName("seconds_ago_format")
        private val secondsAgoFormat: LocalizedText = LocalizedText.build {
            translation(MinecraftLocales.RU_RU, "%time% секунд назад")
            translation(MinecraftLocales.EN_US, "%time% seconds ago")
        },
        @SerialName("no_souls_on_page")
        private val noSoulsOnPage: LocalizedText = prefix.concat(
            LocalizedText.build {
                translation(MinecraftLocales.RU_RU, "&#db2c18Нет душ на странице %page%")
                translation(MinecraftLocales.EN_US, "&#db2c18No souls on page %page%")
            }
        ),
        @SerialName("listing_format")
        private val listingFormat: LocalizedText = LocalizedText.shared(
            "&#b8b8b8%index%. &#d1a71d%owner% &#b8b8b8(%time_ago%) &#b8b8b8(%x%; %y%; %z%) %dist%m"
        ),
        @SerialName("list_souls_title")
        val listSoulsTitle: LocalizedText = prefix.concat(
            LocalizedText.build {
                translation(MinecraftLocales.RU_RU, "&#42f596Список видимых вам душ:")
                translation(MinecraftLocales.EN_US, "&#42f596Souls you can see:")
            }
        ),
        @SerialName("free_soul")
        val freeSoul: LocalizedText = LocalizedText.shared("&#b50b05[FREE]"),
        @SerialName("teleport_to_soul")
        val teleportToSoul: LocalizedText = LocalizedText.shared("&#1db2b8[TP]"),
        @SerialName("soul_freed")
        val soulFreed: LocalizedText = prefix.concat(
            LocalizedText.build {
                translation(MinecraftLocales.RU_RU, "&#42f596Душа теперь свободна!")
                translation(MinecraftLocales.EN_US, "&#42f596The soul is free now!")
            }
        ),
        @SerialName("could_not_free_soul")
        val couldNotFreeSoul: LocalizedText = prefix.concat(
            LocalizedText.build {
                translation(MinecraftLocales.RU_RU, "&#db2c18Не удалось освободить душу!")
                translation(MinecraftLocales.EN_US, "&#db2c18Could not free the soul!")
            }
        ),
        @SerialName("soul_not_found")
        val soulNotFound: LocalizedText = prefix.concat(
            LocalizedText.build {
                translation(MinecraftLocales.RU_RU, "&#db2c18Душа не найдена!")
                translation(MinecraftLocales.EN_US, "&#db2c18Soul not found!")
            }
        ),
        @SerialName("next_page")
        val nextPage: LocalizedText = LocalizedText.build {
            translation(MinecraftLocales.RU_RU, "&#42f596[>>ДАЛЬШЕ>>]")
            translation(MinecraftLocales.EN_US, "&#42f596[>>NEXT>>]")
        },
        @SerialName("prev_page")
        val prevPage: LocalizedText = LocalizedText.build {
            translation(MinecraftLocales.RU_RU, "&#42f596[<<РАНЬШЕ<<]")
            translation(MinecraftLocales.EN_US, "&#42f596[<<BACK<<]")
        },
        @SerialName("soul_of")
        private val soulOf: LocalizedText = LocalizedText.build {
            translation(MinecraftLocales.RU_RU, "&#317dd4Душа игрока &#31d43c%player%")
            translation(MinecraftLocales.EN_US, "&#317dd4Soul of &#31d43c%player%")
        }
    ) {
        fun listingFormat(
            index: Int,
            owner: String,
            timeAgo: LocalizableComponent,
            distance: Int,
            x: Int,
            y: Int,
            z: Int
        ): LocalizableComponent = listingFormat.replaceAll(
            PlaceholderReplacement.plain("%index%", "$index"),
            PlaceholderReplacement.plain("%owner%", owner),
            PlaceholderReplacement.plain("%dist%", "$distance"),
            PlaceholderReplacement(placeholder = "%time_ago%", value = timeAgo),
            PlaceholderReplacement.plain("%x%", "$x"),
            PlaceholderReplacement.plain("%y%", "$y"),
            PlaceholderReplacement.plain("%z%", "$z")
        )

        fun noSoulsOnPage(page: Int): LocalizableComponent = noSoulsOnPage.replace("%page%", page.toString())

        fun soulOf(player: String): LocalizableComponent = soulOf.replace("%player%", player)

        fun daysAgoFormat(time: Duration): LocalizableComponent = daysAgoFormat
            .replace("%time%", time.inWholeDays.toString())

        fun hoursAgoFormat(time: Duration): LocalizableComponent = hoursAgoFormat
            .replace("%time%", time.inWholeHours.toString())

        fun minutesAgoFormat(time: Duration): LocalizableComponent = minutesAgoFormat
            .replace("%time%", time.inWholeMinutes.toString())

        @Suppress("MagicNumber")
        fun monthsAgoFormat(time: Duration): LocalizableComponent = monthsAgoFormat
            .replace("%time%", time.inWholeDays.div(30).toString())

        fun secondsAgoFormat(time: Duration): LocalizableComponent = secondsAgoFormat
            .replace("%time%", time.inWholeSeconds.toString())
    }

    @Serializable
    class General(
        @SerialName("reload")
        val reload: LocalizedText = prefix.concat(
            LocalizedText.build {
                translation(MinecraftLocales.RU_RU, "&#dbbb18Перезагрузка плагина")
                translation(MinecraftLocales.EN_US, "&#dbbb18Reloading the plugin")
            }
        ),
        @SerialName("reload_complete")
        val reloadComplete: LocalizedText = prefix.concat(
            LocalizedText.build {
                translation(MinecraftLocales.RU_RU, "&#42f596Перезагрузка успешно завершена")
                translation(MinecraftLocales.EN_US, "&#42f596Reload complete")
            }
        ),
        @SerialName("no_permission")
        val noPermission: LocalizedText = prefix.concat(
            LocalizedText.build {
                translation(MinecraftLocales.RU_RU, "&#db2c18У вас нет прав!")
                translation(MinecraftLocales.EN_US, "&#db2c18You don't have permission!")
            }
        ),
        @SerialName("wrong_usage")
        val wrongUsage: LocalizedText = prefix.concat(
            LocalizedText.build {
                translation(MinecraftLocales.RU_RU, "&#db2c18Неверное использование!")
                translation(MinecraftLocales.EN_US, "&#db2c18Wrong usage!")
            }
        ),
        @SerialName("only_player_command")
        val onlyPlayerCommand: LocalizedText = prefix.concat(
            LocalizedText.build {
                translation(MinecraftLocales.RU_RU, "&#db2c18Эта команда только для игроков!")
                translation(MinecraftLocales.EN_US, "&#db2c18This command is for players only!")
            }
        ),
    )

    companion object {
        val prefix: LocalizedText = LocalizedText.shared("&#18dbd1[SoulKeeper] ")
    }
}
