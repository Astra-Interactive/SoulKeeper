package ru.astrainteractive.aspekt.module.souls.command

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import ru.astrainteractive.aspekt.module.souls.database.dao.SoulsDao
import ru.astrainteractive.aspekt.plugin.PluginPermission
import ru.astrainteractive.aspekt.plugin.PluginTranslation
import ru.astrainteractive.aspekt.util.getValue
import ru.astrainteractive.astralibs.command.api.argumenttype.PrimitiveArgumentType
import ru.astrainteractive.astralibs.command.api.context.BukkitCommandContext
import ru.astrainteractive.astralibs.command.api.context.BukkitCommandContextExt.argumentOrElse
import ru.astrainteractive.astralibs.command.api.executor.CommandExecutor
import ru.astrainteractive.astralibs.command.api.parser.CommandParser
import ru.astrainteractive.astralibs.command.api.util.PluginExt.registerCommand
import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.astralibs.permission.BukkitPermissibleExt.toPermissible
import ru.astrainteractive.klibs.kstorage.api.Krate

internal class SoulsCommandRegistry(
    private val plugin: JavaPlugin,
    private val scope: CoroutineScope,
    private val soulsDao: SoulsDao,
    translationKrate: Krate<PluginTranslation>,
    kyoriKrate: Krate<KyoriComponentSerializer>
) {
    private val kyori by kyoriKrate
    private val translation by translationKrate

    sealed interface Intent {
        data class List(val sender: CommandSender, val page: Int) : Intent
    }

    private inner class CommandParserImpl : CommandParser<Intent, BukkitCommandContext> {
        override fun parse(commandContext: BukkitCommandContext): Intent {
            return when (commandContext.args.getOrNull(0)) {
                else -> {
                    val page = commandContext.argumentOrElse(
                        index = 0,
                        type = PrimitiveArgumentType.Int,
                        default = { 1 }
                    ).minus(1)
                    Intent.List(sender = commandContext.sender, page = page)
                }
            }
        }
    }

    private inner class CommandExecutorImpl : CommandExecutor<Intent> {
        override fun execute(input: Intent) {
            when (input) {
                is Intent.List -> with(kyori) {
                    scope.launch {
                        val souls = soulsDao.getSouls()
                            .getOrNull()
                            .orEmpty()
                            .let {
                                val start = input.page.times(PAGE_SIZE).coerceIn(0, it.size)
                                val end = (input.page * PAGE_SIZE + PAGE_SIZE).coerceAtMost(it.size)
                                if (start == end) {
                                    emptyList()
                                } else if (end == 0) {
                                    emptyList()
                                } else {
                                    it.subList(start, end)
                                }
                            }
                            .filter {
                                (input.sender as? Player)?.world?.name?.let { worldName ->
                                    it.location.world.name == worldName
                                } ?: true
                            }
                            .filter { soul ->
                                soul.isFree
                                    .or(input.sender.toPermissible().hasPermission(PluginPermission.ViewAllSouls))
                                    .or((input.sender as? Player)?.uniqueId == soul.ownerUUID)
                            }
                        if (souls.isEmpty()) {
                            val title = translation.souls.noSoulsOnPage(input.page.plus(1)).component
                            input.sender.sendMessage(title)
                            return@launch
                        }

                        val title = translation.souls.listSoulsTitle.component
                        input.sender.sendMessage(title)
                        souls.forEachIndexed { i, soul ->
                            val timeAgoFormatted = when (val timeAgo = TimeAgoFormatter.format(soul.createdAt)) {
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
                            val listingComponent = translation.souls.listingFormat(
                                index = input.page.times(PAGE_SIZE).plus(i.plus(1)),
                                owner = soul.ownerLastName,
                                timeAgo = timeAgoFormatted.raw,
                                x = soul.location.x.toInt(),
                                y = soul.location.y.toInt(),
                                z = soul.location.z.toInt()
                            ).component
                            val freeComponent = when {
                                input.sender.toPermissible()
                                    .hasPermission(PluginPermission.FreeAllSouls)
                                    .or((input.sender as? Player)?.uniqueId == soul.ownerUUID) -> {
                                    translation.souls.freeSoul
                                        .component
                                        .clickEvent(
                                            ClickEvent.callback { audience ->
                                                scope.launch {
                                                    soulsDao.updateSoul(soul.copy(isFree = true))
                                                        .onSuccess {
                                                            audience.sendMessage(
                                                                translation.souls.soulFreed.component
                                                            )
                                                        }
                                                        .onFailure {
                                                            audience.sendMessage(
                                                                translation.souls.couldNotFreeSoul.component
                                                            )
                                                        }
                                                }
                                            }
                                        )
                                }

                                else -> Component.empty()
                            }
                            val teleportComponent = when {
                                input.sender.toPermissible().hasPermission(PluginPermission.TeleportToSouls) -> {
                                    translation.souls.teleportToSoul
                                        .component
                                        .clickEvent(
                                            ClickEvent.callback { audience ->
                                                (audience as? Player)?.teleportAsync(soul.location)
                                            }
                                        )
                                }

                                else -> Component.empty()
                            }
                            input.sender.sendMessage(listingComponent)
                            input.sender.sendMessage(freeComponent.appendSpace().append(teleportComponent))
                        }

                        val nextPageComponent = translation.souls.nextPage.component.clickEvent(
                            ClickEvent.callback {
                                execute(input.copy(page = input.page.plus(1)))
                            }
                        )

                        val prevPageComponent = translation.souls.prevPage.component.clickEvent(
                            ClickEvent.callback {
                                execute(input.copy(page = input.page.minus(1)))
                            }
                        ).appendSpace().takeIf { input.page > 0 } ?: Component.empty()
                        input.sender.sendMessage(prevPageComponent.append(nextPageComponent))
                    }
                }
            }
        }
    }

    fun register() {
        plugin.registerCommand(
            alias = "souls",
            commandParser = CommandParserImpl(),
            commandExecutor = CommandExecutorImpl(),
            errorHandler = { context, throwable ->
                throwable.printStackTrace()
            }
        )
    }

    companion object {
        private const val PAGE_SIZE = 5
    }
}
