@file:Suppress("MaxLineLength", "MaximumLineLength", "LongParameterList")

package ru.astrainteractive.soulkeeper.core.plugin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.astrainteractive.astralibs.string.StringDesc
import ru.astrainteractive.astralibs.string.StringDescExt.replace
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
        private val daysAgoFormat: StringDesc.Raw = StringDesc.Raw("%time% дней назад"),
        private val hoursAgoFormat: StringDesc.Raw = StringDesc.Raw("%time% часов назад"),
        private val minutesAgoFormat: StringDesc.Raw = StringDesc.Raw("%time% минут назад"),
        private val monthsAgoFormat: StringDesc.Raw = StringDesc.Raw("%time% месяцеев назад"),
        private val secondsAgoFormat: StringDesc.Raw = StringDesc.Raw("%time% секунд назад"),
        private val noSoulsOnPage: StringDesc.Raw = StringDesc.Raw("&#db2c18Нет душ на странице %page%"),
        private val listingFormat: StringDesc.Raw = StringDesc.Raw(
            "&#b8b8b8%index%. &#d1a71d%owner% &#b8b8b8(%time_ago%) &#b8b8b8(%x%; %y%; %z%) %dist%m"
        ),
        val listSoulsTitle: StringDesc.Raw = StringDesc.Raw("&#42f596Список видимых вам душ:"),
        val freeSoul: StringDesc.Raw = StringDesc.Raw("&#b50b05[FREE]"),
        val teleportToSoul: StringDesc.Raw = StringDesc.Raw("&#1db2b8[TP]"),
        val soulFreed: StringDesc.Raw = StringDesc.Raw("&#42f596Душа теперь свободна!"),
        val couldNotFreeSoul: StringDesc.Raw = StringDesc.Raw("&#db2c18Не удалось освободить душу!"),
        val nextPage: StringDesc.Raw = StringDesc.Raw("&#42f596[>>ДАЛЬШЕ>>]"),
        val prevPage: StringDesc.Raw = StringDesc.Raw("&#42f596[<<РАНЬШЕ<<]"),
        private val soulOf: StringDesc.Raw = StringDesc.Raw("&#317dd4Душа игрока &#31d43c%player%")
    ) {
        fun listingFormat(
            index: Int,
            owner: String,
            timeAgo: String,
            distance: Int,
            x: Int,
            y: Int,
            z: Int
        ) = listingFormat
            .replace("%index%", "$index")
            .replace("%owner%", owner)
            .replace("%dist%", "$distance")
            .replace("%time_ago%", timeAgo)
            .replace("%x%", "$x")
            .replace("%y%", "$y")
            .replace("%z%", "$z")

        fun noSoulsOnPage(page: Int) = noSoulsOnPage
            .replace("%page%", page.toString())

        fun soulOf(player: String) = soulOf
            .replace("%player%", player)

        fun daysAgoFormat(time: Duration) = daysAgoFormat
            .replace("%time%", time.inWholeDays.toString())

        fun hoursAgoFormat(time: Duration) = hoursAgoFormat
            .replace("%time%", time.inWholeHours.toString())

        fun minutesAgoFormat(time: Duration) = minutesAgoFormat
            .replace("%time%", time.inWholeMinutes.toString())

        fun monthsAgoFormat(time: Duration) = monthsAgoFormat
            .replace("%time%", time.inWholeDays.div(30).toString())

        fun secondsAgoFormat(time: Duration) = secondsAgoFormat
            .replace("%time%", time.inWholeSeconds.toString())
    }

    @Serializable
    class General(
        @SerialName("prefix")
        val prefix: StringDesc.Raw = StringDesc.Raw("&#18dbd1[SoulKeeper]"),
        @SerialName("reload")
        val reload: StringDesc.Raw = StringDesc.Raw("&#dbbb18Перезагрузка плагина"),
        @SerialName("reload_complete")
        val reloadComplete: StringDesc.Raw = StringDesc.Raw("&#42f596Перезагрузка успешно завершена"),
        @SerialName("no_permission")
        val noPermission: StringDesc.Raw = StringDesc.Raw("&#db2c18У вас нет прав!"),
        @SerialName("wrong_usage")
        val wrongUsage: StringDesc.Raw = StringDesc.Raw("&#db2c18Неверное использование!"),
        @SerialName("only_player_command")
        val onlyPlayerCommand: StringDesc.Raw = StringDesc.Raw("&#db2c18Эта команда только для игроков!"),
    )
}
