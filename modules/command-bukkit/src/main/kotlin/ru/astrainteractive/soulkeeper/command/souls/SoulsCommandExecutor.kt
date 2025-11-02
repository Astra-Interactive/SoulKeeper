package ru.astrainteractive.soulkeeper.command.souls

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.astralibs.kyori.unwrap
import ru.astrainteractive.astralibs.permission.BukkitPermissibleExt.toPermissible
import ru.astrainteractive.astralibs.util.clickable
import ru.astrainteractive.astralibs.util.isEmpty
import ru.astrainteractive.astralibs.util.orEmpty
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.klibs.mikro.core.util.tryCast
import ru.astrainteractive.soulkeeper.core.datetime.TimeAgoFormatter
import ru.astrainteractive.soulkeeper.core.datetime.TimeAgoTranslationFormatter
import ru.astrainteractive.soulkeeper.core.plugin.PluginPermission
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation
import ru.astrainteractive.soulkeeper.core.util.toBukkitLocation
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.Soul
import kotlin.collections.filter

internal class SoulsCommandExecutor(
    private val ioScope: CoroutineScope,
    private val soulsDao: SoulsDao,
    translationKrate: CachedKrate<PluginTranslation>,
    kyoriKrate: CachedKrate<KyoriComponentSerializer>
) : KyoriComponentSerializer by kyoriKrate.unwrap() {
    private val translation by translationKrate

    private fun createPagingMessage(input: SoulsCommand.Intent.List, maxPages: Int): Component {
        val nextPageComponent = translation.souls.nextPage.component
            .clickable { execute(input.copy(page = input.page.plus(1))) }
            .takeIf { input.page < maxPages }
            .orEmpty()
        val prevPageComponent = translation.souls.prevPage.component
            .clickable { execute(input.copy(page = input.page.plus(-1))) }
            .appendSpace().takeIf { input.page > 0 }
            .orEmpty()
        return nextPageComponent.append(prevPageComponent, true)
    }

    private suspend fun getFilteredSouls(sender: CommandSender): List<DatabaseSoul> {
        return soulsDao.getSouls()
            .getOrNull()
            .orEmpty()
            .filter {
                (sender as? Player)?.world?.name?.let { worldName ->
                    it.location.worldName == worldName
                } ?: true
            }
            .filter { soul ->
                soul.isFree
                    .or(sender.toPermissible().hasPermission(PluginPermission.ViewAllSouls))
                    .or((sender as? Player)?.uniqueId == soul.ownerUUID)
            }
    }

    private fun getPageSouls(souls: List<DatabaseSoul>, page: Int): List<DatabaseSoul> {
        val start = page.times(SoulsCommand.PAGE_SIZE).coerceIn(0, souls.size)
        val end =
            (page * SoulsCommand.PAGE_SIZE + SoulsCommand.PAGE_SIZE).coerceAtMost(
                souls.size
            )
        return if (start == end) {
            emptyList()
        } else if (end == 0) {
            emptyList()
        } else {
            souls.subList(start, end)
        }
    }

    private fun createListingItemComponent(
        soul: Soul,
        page: Int,
        i: Int,
        location: Location?
    ): Component {
        val timeAgo = TimeAgoFormatter.format(soul.createdAt)
        val timeAgoFormatted = TimeAgoTranslationFormatter(translation)
            .format(timeAgo)

        return translation.souls.listingFormat(
            index = page.times(SoulsCommand.PAGE_SIZE).plus(i.plus(1)),
            owner = soul.ownerLastName,
            timeAgo = timeAgoFormatted.raw,
            x = soul.location.x.toInt(),
            y = soul.location.y.toInt(),
            z = soul.location.z.toInt(),
            distance = location
                ?.distance(soul.location.toBukkitLocation())
                ?.toInt()
                ?: 0
        ).component
    }

    private fun CommandSender.canFreeSouls(soul: DatabaseSoul): Boolean {
        val sender = this
        val hasPermission = sender.toPermissible().hasPermission(PluginPermission.FreeAllSouls)
        val isOwner = (sender as? Player)?.uniqueId == soul.ownerUUID
        if (soul.isFree) return false
        if (!hasPermission) return false
        if (!isOwner) return false
        return true
    }

    private fun createFreeSoulComponent(sender: CommandSender, soul: DatabaseSoul): Component? {
        return if (!sender.canFreeSouls(soul)) {
            null
        } else {
            translation.souls.freeSoul
                .component
                .appendSpace()
                .clickable { audience ->
                    val executor = audience
                        .tryCast<Player>()
                        ?: return@clickable
                    executor.performCommand("/souls free ${soul.id}")
                }
        }
    }

    private fun CommandSender.canTeleportToSoul(): Boolean {
        val sender = this
        if (sender !is Player) return false
        if (!sender.toPermissible().hasPermission(PluginPermission.TeleportToSouls)) {
            return false
        }
        return true
    }

    private fun createTeleportSoulComponent(sender: CommandSender, soul: DatabaseSoul): Component? {
        if (!sender.canTeleportToSoul()) return null
        return translation.souls.teleportToSoul
            .component
            .clickable { audience ->
                val executor = audience
                    .tryCast<Player>()
                    ?: return@clickable
                executor.performCommand("/souls teleport ${soul.id}")
            }
    }

    fun Component.append(
        other: Component?,
        addSpace: Boolean = false
    ): Component {
        return if (other == null || other.isEmpty()) {
            this
        } else if (addSpace) {
            this.appendSpace().append(other)
        } else {
            this.append(other)
        }
    }

    @Suppress("LongMethod")
    fun execute(input: SoulsCommand.Intent) {
        when (input) {
            is SoulsCommand.Intent.List -> {
                ioScope.launch {
                    val filteredSouls = getFilteredSouls(input.sender)
                    val maxPages = filteredSouls.size.div(SoulsCommand.PAGE_SIZE)
                    val pageSouls = getPageSouls(filteredSouls, input.page)
                    if (pageSouls.isEmpty()) {
                        val title = translation.souls.noSoulsOnPage(input.page.plus(1)).component
                        input.sender.sendMessage(title)
                        return@launch
                    }

                    translation.souls.listSoulsTitle.component
                        .run(input.sender::sendMessage)

                    pageSouls.forEachIndexed { i, soul ->
                        createListingItemComponent(
                            soul = soul,
                            page = input.page,
                            i = i,
                            location = input.sender
                                .tryCast<Player>()
                                ?.location
                        ).append(
                            addSpace = true,
                            other = createFreeSoulComponent(
                                sender = input.sender,
                                soul = soul
                            )
                        ).append(
                            addSpace = true,
                            other = createTeleportSoulComponent(
                                sender = input.sender,
                                soul = soul
                            )
                        ).run(input.sender::sendMessage)
                    }
                    createPagingMessage(input, maxPages)
                        .run(input.sender::sendMessage)
                }
            }

            is SoulsCommand.Intent.Free -> {
                ioScope.launch {
                    val newSoul = soulsDao.getSoul(input.soulId)
                        .getOrNull()
                        ?.copy(isFree = true)
                        ?: return@launch
                    soulsDao.updateSoul(newSoul)
                        .onSuccess {
                            input.sender.sendMessage(translation.souls.soulFreed.component)
                        }
                        .onFailure {
                            input.sender.sendMessage(translation.souls.couldNotFreeSoul.component)
                        }
                }
            }

            is SoulsCommand.Intent.TeleportToSoul -> {
                ioScope.launch {
                    val player = input.sender as? Player ?: return@launch
                    val location = soulsDao.getSoul(input.soulId)
                        .getOrNull()
                        ?.location
                        ?.toBukkitLocation()
                        ?: return@launch
                    player.teleportAsync(location)
                }
            }
        }
    }
}
