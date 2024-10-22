@file:Suppress("MaxLineLength", "MaximumLineLength", "LongParameterList")

package ru.astrainteractive.aspekt.plugin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.astrainteractive.astralibs.string.StringDesc
import ru.astrainteractive.astralibs.string.StringDescExt.replace
import java.text.DecimalFormat
import kotlin.time.Duration

/**
 * All translation stored here
 */
@Serializable
class PluginTranslation(
    @SerialName("general")
    val general: General = General(),
    @SerialName("sit")
    val sit: Sit = Sit(),
    @SerialName("adminprivate")
    val adminPrivate: AdminPrivate = AdminPrivate(),
    @SerialName("money_advancement")
    val moneyAdvancement: MoneyAdvancement = MoneyAdvancement(),
    @SerialName("newbee")
    val newBee: NewBee = NewBee(),
    @SerialName("swear")
    val swear: Swear = Swear(),
    @SerialName("chat_game")
    val chatGame: ChatGame = ChatGame(),
    @SerialName("economy")
    val economy: Economy = Economy(),
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
    class Economy(
        val prefix: StringDesc.Raw = StringDesc.Raw("&7[&#DBB72BECO&7]"),
        val playerNotFound: StringDesc.Raw = StringDesc.Raw("${prefix.raw} &#db2c18Игрок не найден"),
        val errorTransferMoney: StringDesc.Raw = StringDesc.Raw("${prefix.raw} &#db2c18Не удалось выдать валюту"),
        val moneyTransferred: StringDesc.Raw = StringDesc.Raw("${prefix.raw} &#42f596Валюта успешно выдана игроку"),
        private val playerBalance: StringDesc.Raw = StringDesc.Raw("${prefix.raw} &#42f596Баланс игрока %balance%"),
        private val currencies: StringDesc.Raw = StringDesc.Raw("${prefix.raw} &#42f596Доступные валюту: %currencies%"),
        val topsTitle: StringDesc.Raw = StringDesc.Raw("${prefix.raw} &#42f596Топ игроков по балансу:"),
        val topsEmpty: StringDesc.Raw = StringDesc.Raw("${prefix.raw} &#42f596Топ игроков пуст!"),
        private val topItem: StringDesc.Raw = StringDesc.Raw("${prefix.raw} &#42f596%index%. %name% → %balance%"),
        @SerialName("currency_not_found")
        val currencyNotFound: StringDesc.Raw = StringDesc.Raw("&#db2c18Валюта не найдена!"),
    ) {
        fun playerBalance(amount: Number) = playerBalance.replace("%balance%", DecimalFormat("0.00").format(amount))
        fun currencies(value: String) = currencies.replace("%currencies%", value)
        fun topItem(index: Int, name: String, balance: Number) = topItem
            .replace("%index%", "$index")
            .replace("%name%", "$name")
            .replace("%balance%", DecimalFormat("0.00").format(balance))
    }

    @Serializable
    class ChatGame(
        @SerialName("solve_riddle")
        private val solveRiddle: StringDesc.Raw = StringDesc.Raw("&7[&#DBB72BКВИЗ&7] Загадка: %quiz% → &2/quiz ОТВЕТ"),
        @SerialName("solve_example")
        private val solveExample: StringDesc.Raw = StringDesc.Raw("&7[&#DBB72BКВИЗ&7] Пример: %quiz% → &2/quiz ОТВЕТ"),
        @SerialName("solve_anagram")
        private val solveAnagram: StringDesc.Raw = StringDesc.Raw(
            "&7[&#DBB72BКВИЗ&7] Анаграмма: %quiz% → &2/quiz ОТВЕТ"
        ),
        @SerialName("solve_quadratic")
        private val solveQuadratic: StringDesc.Raw = StringDesc.Raw(
            "&7[&#DBB72BКВИЗ&7] Квадратное уравнение: %quiz% → &2/quiz ОТВЕТ &7Любой вариант ответа с точностью до сотой. Например: 0.02, 0.3, 1.0"
        ),
        @SerialName("no_quiz_available")
        val noQuizAvailable: StringDesc.Raw = StringDesc.Raw(
            "&7[&#DBB72BКВИЗ&7] &#db2c18В данный момент нет активного квиза!"
        ),
        @SerialName("wrong_answer")
        val wrongAnswer: StringDesc.Raw = StringDesc.Raw("&7[&#DBB72BКВИЗ&7] &#db2c18Ответ неверный!"),
        @SerialName("game_ended")
        val gameEndedMoneyReward: StringDesc.Raw = StringDesc.Raw(
            "&7[&#DBB72BКВИЗ&7] &6%player% &7угадал верный ответ! И получил &6%amount% &7монет!"
        ),
    ) {
        fun solveRiddle(quiz: String) = solveRiddle.replace("%quiz%", quiz)
        fun solveExample(quiz: String) = solveExample.replace("%quiz%", quiz)
        fun solveAnagram(quiz: String) = solveAnagram.replace("%quiz%", quiz)
        fun solveQuadratic(quiz: String) = solveQuadratic.replace("%quiz%", quiz)

        fun gameEndedMoneyReward(player: String, amount: Number) = gameEndedMoneyReward
            .replace("%player%", player)
            .replace("%amount%", DecimalFormat("0.00").format(amount))
    }

    @Serializable
    class MoneyAdvancement(
        @SerialName("reload_complete")
        private val challengeCompleted: StringDesc.Raw = StringDesc.Raw(
            "&7[&#DBB72BДОСТИЖЕНИЕ&7] Вы выполднили достижение-челлендж и получили награду: %money% монет"
        ),
        @SerialName("goal_completed")
        private val goalCompleted: StringDesc.Raw = StringDesc.Raw(
            "&7[&#DBB72BДОСТИЖЕНИЕ&7] Вы выполднили целевое достижение и получили награду: %money% монет"
        ),
        @SerialName("task_completed")
        private val taskCompleted: StringDesc.Raw = StringDesc.Raw(
            "&7[&#DBB72BДОСТИЖЕНИЕ&7] Вы выполднили достижение и получили награду: %money% монет"
        ),
    ) {
        fun challengeCompleted(money: Number) = challengeCompleted.replace(
            "%money%",
            DecimalFormat("0.00").format(money)
        )

        fun goalCompleted(money: Number) = goalCompleted.replace("%money%", DecimalFormat("0.00").format(money))
        fun taskCompleted(money: Number) = taskCompleted.replace("%money%", DecimalFormat("0.00").format(money))
    }

    @Serializable
    class General(
        @SerialName("prefix")
        val prefix: StringDesc.Raw = StringDesc.Raw("&#18dbd1[AspeKt]"),
        @SerialName("reload")
        val reload: StringDesc.Raw = StringDesc.Raw("&#dbbb18Перезагрузка плагина"),
        @SerialName("reload_complete")
        val reloadComplete: StringDesc.Raw = StringDesc.Raw("&#42f596Перезагрузка успешно завершена"),
        @SerialName("no_permission")
        val noPermission: StringDesc.Raw = StringDesc.Raw("&#db2c18У вас нет прав!"),
        @SerialName("not_enough_money")
        val notEnoughMoney: StringDesc.Raw = StringDesc.Raw("&#db2c18Недостаточно средств!"),
        @SerialName("wrong_usage")
        val wrongUsage: StringDesc.Raw = StringDesc.Raw("&#db2c18Неверное использование!"),
        @SerialName("only_player_command")
        val onlyPlayerCommand: StringDesc.Raw = StringDesc.Raw("&#db2c18Эта команда только для игроков!"),
        @SerialName("menu_not_found")
        val menuNotFound: StringDesc.Raw = StringDesc.Raw("&#db2c18Меню с заданным ID не найдено"),
        @SerialName("discord_link_reward")
        private val discordLinkReward: StringDesc.Raw = StringDesc.Raw(
            "&#42f596Вы получили {AMOUNT}$ за привязку дискорда!"
        ),
        @SerialName("picked_up_money")
        private val pickedUpMoney: StringDesc.Raw = StringDesc.Raw("&#42f596Вы подобрали {AMOUNT} загадочных монет"),
        @SerialName("dropped_money")
        val droppedMoney: StringDesc.Raw = StringDesc.Raw("&6Монетка"),
        @SerialName("maybe_tpr")
        val maybeTpr: StringDesc.Raw = StringDesc.Raw("&#db2c18Возможно, вы хотели ввести /tpr")
    ) {

        fun discordLinkReward(amount: Number): StringDesc.Raw {
            return discordLinkReward.replace("{AMOUNT}", DecimalFormat("0.00").format(amount))
        }

        fun pickedUpMoney(amount: Number): StringDesc.Raw {
            return pickedUpMoney
                .replace("{AMOUNT}", DecimalFormat("0.00").format(amount))
        }
    }

    @Serializable
    class AdminPrivate(
        // Admin claim
        @SerialName("flag_changed")
        val chunkFlagChanged: StringDesc.Raw = StringDesc.Raw("&#db2c18Флаг чанка изменен!"),
        @SerialName("claimed")
        val chunkClaimed: StringDesc.Raw = StringDesc.Raw("&#db2c18Вы заняли чанк!"),
        @SerialName("unclaimed")
        val chunkUnClaimed: StringDesc.Raw = StringDesc.Raw("&#db2c18Чанк свободен!"),
        @SerialName("error")
        val error: StringDesc.Raw = StringDesc.Raw("&#db2c18Ошибка! Смотрите консоль"),
        @SerialName("map")
        val blockMap: StringDesc.Raw = StringDesc.Raw("&#18dbd1Карта блоков:"),
        @SerialName("action_blocked")
        private val actionIsBlockByAdminClaim: StringDesc.Raw = StringDesc.Raw(
            "&#db2c18Ошибка! Действией %action% заблокировано на этом чанке!"
        ),
    ) {
        fun actionIsBlockByAdminClaim(action: String): StringDesc.Raw {
            return actionIsBlockByAdminClaim.replace("%action%", action)
        }
    }

    @Serializable
    class Sit(
        @SerialName("already")
        val sitAlready: StringDesc.Raw = StringDesc.Raw("&#dbbb18Вы уже сидите"),
        @SerialName("air")
        val sitInAir: StringDesc.Raw = StringDesc.Raw("&#dbbb18Нельзя сидеть в воздухе"),
        @SerialName("too_far")
        val tooFar: StringDesc.Raw = StringDesc.Raw("&#dbbb18Слишком далеко"),
        @SerialName("cant_sit_in_block")
        val cantSitInBlock: StringDesc.Raw = StringDesc.Raw("&#dbbb18Нельзя сидеть в блоке")
    )

    @Serializable
    class NewBee(
        val youAreNewBee: StringDesc.Raw = StringDesc.Raw(
            "&7[&#DBB72BЗАЩИТА&7] &#1D72F2Вы новичок! &6Поэтому в первые 50 минут вам будет играть легче! Наслаждайтесь игрой!"
        ),
        val newBeeTitle: StringDesc.Raw = StringDesc.Raw("&#DBB72BЗащита новичка"),
        val newBeeSubtitle: StringDesc.Raw = StringDesc.Raw("&#db2c18Включена"),
        val newBeeShieldForceDisabled: StringDesc.Raw = StringDesc.Raw(
            "&7[&#DBB72BЗАЩИТА&7] &#DBB72BВы вступили в бой с игроком. Защита новичка была удалена"
        )
    )

    @Serializable
    class Swear(
        val prefix: StringDesc.Raw = StringDesc.Raw("&7[&#DBB72BSF&7] "),
        val swearFilterEnabled: StringDesc.Raw = StringDesc.Raw("${prefix.raw}&#db2c18Фильтр плохих слов включен"),
        val swearFilterDisabled: StringDesc.Raw = StringDesc.Raw("${prefix.raw}&#db2c18Фильтр плохих слов выключен"),
        private val swearFilterEnabledFor: StringDesc.Raw = StringDesc.Raw(
            "${prefix.raw}&#db2c18Фильтр плохих слов включен для {player}"
        ),
        private val swearFilterDisabledFor: StringDesc.Raw = StringDesc.Raw(
            "${prefix.raw}&#db2c18Фильтр плохих слов выключен для {player}"
        ),
    ) {
        fun swearFilterEnabledFor(name: String) = swearFilterEnabledFor.replace("{player}", name)
        fun swearFilterDisabledFor(name: String) = swearFilterDisabledFor.replace("{player}", name)
    }
}
