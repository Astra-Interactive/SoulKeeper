package ru.astrainteractive.aspekt.module.souls.command

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import ru.astrainteractive.aspekt.module.souls.database.dao.SoulsDao
import ru.astrainteractive.aspekt.module.souls.database.model.DatabaseSoul
import ru.astrainteractive.aspekt.module.souls.database.model.Soul
import ru.astrainteractive.aspekt.module.souls.util.clickable
import ru.astrainteractive.aspekt.module.souls.util.isEmpty
import ru.astrainteractive.aspekt.module.souls.util.isNotEmpty
import ru.astrainteractive.aspekt.module.souls.util.orEmpty
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

        private fun sendPagingMessage(input: Intent.List, maxPages: Int) = with(kyori) {
            val nextPageComponent = translation.souls.nextPage.component
                .clickable { execute(input.copy(page = input.page.plus(1))) }
                .takeIf { input.page < maxPages }
                .orEmpty()

            val prevPageComponent = translation.souls.prevPage.component
                .clickable { execute(input.copy(page = input.page.plus(-1))) }
                .appendSpace().takeIf { input.page > 0 }
                .orEmpty()

            if (nextPageComponent.isEmpty().and(prevPageComponent.isEmpty())) return@with
            input.sender.sendMessage(prevPageComponent.append(nextPageComponent))
        }

        private fun createTeleportComponent(
            sender: CommandSender,
            soul: Soul
        ): Component = with(kyori) {
            if (sender !is Player) return@with Component.empty()
            if (!sender.toPermissible().hasPermission(PluginPermission.TeleportToSouls)) {
                return@with Component.empty()
            }
            translation.souls.teleportToSoul
                .component
                .clickable { sender.teleportAsync(soul.location) }
        }

        private fun createFreeComponent(
            sender: CommandSender,
            soul: DatabaseSoul
        ) = with(kyori) {
            val hasPermission = sender.toPermissible().hasPermission(PluginPermission.FreeAllSouls)
            val isOwner = (sender as? Player)?.uniqueId == soul.ownerUUID
            val isSoulFree = soul.isFree
            if (isSoulFree) return@with Component.empty()
            if (!hasPermission || !isOwner) return@with Component.empty()
            translation.souls.freeSoul
                .component
                .appendSpace()
                .clickable { audience ->
                    scope.launch {
                        soulsDao.updateSoul(soul.copy(isFree = true))
                            .onSuccess {
                                audience.sendMessage(translation.souls.soulFreed.component)
                            }
                            .onFailure {
                                audience.sendMessage(translation.souls.couldNotFreeSoul.component)
                            }
                    }
                }
        }

        private suspend fun getFilteredSouls(sender: CommandSender): List<DatabaseSoul> {
            return soulsDao.getSouls()
                .getOrNull()
                .orEmpty()
                .filter {
                    (sender as? Player)?.world?.name?.let { worldName ->
                        it.location.world.name == worldName
                    } ?: true
                }
                .filter { soul ->
                    soul.isFree
                        .or(sender.toPermissible().hasPermission(PluginPermission.ViewAllSouls))
                        .or((sender as? Player)?.uniqueId == soul.ownerUUID)
                }
        }

        private fun getPageSouls(souls: List<DatabaseSoul>, page: Int): List<DatabaseSoul> {
            val start = page.times(PAGE_SIZE).coerceIn(0, souls.size)
            val end = (page * PAGE_SIZE + PAGE_SIZE).coerceAtMost(souls.size)
            return if (start == end) {
                emptyList()
            } else if (end == 0) {
                emptyList()
            } else {
                souls.subList(start, end)
            }
        }

        override fun execute(input: Intent) {
            when (input) {
                is Intent.List -> with(kyori) {
                    scope.launch {
                        val filteredSouls = getFilteredSouls(input.sender)
                        val maxPages = filteredSouls.size.div(PAGE_SIZE)
                        val pageSouls = getPageSouls(filteredSouls, input.page)
                        if (pageSouls.isEmpty()) {
                            val title = translation.souls.noSoulsOnPage(input.page.plus(1)).component
                            input.sender.sendMessage(title)
                            sendPagingMessage(input, maxPages)
                            return@launch
                        }

                        val title = translation.souls.listSoulsTitle.component
                        input.sender.sendMessage(title)

                        pageSouls.forEachIndexed { i, soul ->
                            val timeAgo = TimeAgoFormatter.format(soul.createdAt)
                            val timeAgoFormatted = TimeAgoTranslationFormatter(translation)
                                .format(timeAgo)

                            val listingComponent = translation.souls.listingFormat(
                                index = input.page.times(PAGE_SIZE).plus(i.plus(1)),
                                owner = soul.ownerLastName,
                                timeAgo = timeAgoFormatted.raw,
                                x = soul.location.x.toInt(),
                                y = soul.location.y.toInt(),
                                z = soul.location.z.toInt(),
                                distance = (input.sender as? Player)
                                    ?.location
                                    ?.distance(soul.location)
                                    ?.toInt()
                                    ?: 0
                            ).component
                            input.sender.sendMessage(listingComponent)

                            val freeComponent = createFreeComponent(
                                sender = input.sender,
                                soul = soul
                            )
                            val teleportComponent = createTeleportComponent(
                                sender = input.sender,
                                soul = soul
                            )
                            if (freeComponent.isNotEmpty().or(teleportComponent.isNotEmpty())) {
                                input.sender.sendMessage(freeComponent.append(teleportComponent))
                            }
                        }
                        sendPagingMessage(input, maxPages)
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
