package ru.astrainteractive.soulkeeper.command.souls

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import ru.astrainteractive.astralibs.command.api.brigadier.sender.ConsoleKCommandSender
import ru.astrainteractive.astralibs.command.api.brigadier.sender.KCommandSender
import ru.astrainteractive.astralibs.command.api.brigadier.sender.KPlayerKCommandSender
import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.astralibs.kyori.unwrap
import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.astralibs.server.location.dist
import ru.astrainteractive.astralibs.util.clickable
import ru.astrainteractive.astralibs.util.isEmpty
import ru.astrainteractive.astralibs.util.orEmpty
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.api.getValue
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.klibs.mikro.core.util.tryCast
import ru.astrainteractive.soulkeeper.core.datetime.TimeAgoFormatter
import ru.astrainteractive.soulkeeper.core.datetime.TimeAgoTranslationFormatter
import ru.astrainteractive.soulkeeper.core.plugin.PluginPermission
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.Soul

private fun Component.append(
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

internal class SoulsCommandExecutor(
    private val ioScope: CoroutineScope,
    private val soulsDao: SoulsDao,
    private val dispatchers: KotlinDispatchers,
    private val accessPolicy: SoulsAccessPolicy,
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

    private suspend fun getFilteredSouls(sender: KCommandSender): List<DatabaseSoul> {
        val souls = soulsDao.getSouls()
            .getOrNull()
            .orEmpty()
        return when (sender) {
            is KPlayerKCommandSender -> {
                souls
                    .filter { soul ->
                        sender
                            .instance
                            .getLocation()
                            .worldName == soul.location.worldName
                    }
                    .filter { soul ->
                        soul.isFree
                            .or(sender.instance.hasPermission(PluginPermission.ViewAllSouls))
                            .or(sender.instance.uuid == soul.ownerUUID)
                    }
            }

            is ConsoleKCommandSender -> souls
        }
    }

    private fun getPageSouls(souls: List<DatabaseSoul>, page: Int): List<DatabaseSoul> {
        val start = page.times(SoulsCommand.PAGE_SIZE).coerceIn(0, souls.size)
        val end = page.times(SoulsCommand.PAGE_SIZE)
            .plus(SoulsCommand.PAGE_SIZE)
            .coerceAtMost(souls.size)
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
        location: KLocation?
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
                ?.dist(soul.location)
                ?.toInt()
                ?: 0
        ).component
    }

    private fun createFreeSoulComponent(sender: KCommandSender, soul: DatabaseSoul): Component? {
        if (soul.isFree) return null
        if (!accessPolicy.canFreeSoul(sender, soul)) return null
        return translation.souls.freeSoul
            .component
            .appendSpace()
            .clickable { execute(SoulsCommand.Intent.Free(sender, soul.id)) }
    }

    private fun createTeleportSoulComponent(sender: KCommandSender, soul: DatabaseSoul): Component? {
        if (sender !is KPlayerKCommandSender) return null
        if (!accessPolicy.canTeleportToSoul(sender)) return null
        return translation.souls.teleportToSoul
            .component
            .clickable { execute(SoulsCommand.Intent.TeleportToSoul(sender.instance, soul.id)) }
    }

    private fun executeList(input: SoulsCommand.Intent.List) {
        ioScope.launch {
            val filteredSouls = getFilteredSouls(input.sender)
            val maxPages = filteredSouls.size.div(SoulsCommand.PAGE_SIZE)
            val pageSouls = getPageSouls(filteredSouls, input.page)
            if (pageSouls.isEmpty()) {
                val title = translation.souls.noSoulsOnPage(input.page.plus(1)).component
                input.sender.sendMessage(title)
                return@launch
            }

            input.sender.sendMessage(translation.souls.listSoulsTitle.component)

            pageSouls.forEachIndexed { i, soul ->
                val component = createListingItemComponent(
                    soul = soul,
                    page = input.page,
                    i = i,
                    location = input.sender
                        .tryCast<KPlayerKCommandSender>()
                        ?.instance
                        ?.getLocation()
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
                )
                input.sender.sendMessage(component)
            }
            input.sender.sendMessage(createPagingMessage(input, maxPages))
        }
    }

    private fun executeFree(input: SoulsCommand.Intent.Free) {
        ioScope.launch {
            val soul = soulsDao.getSoul(input.soulId).getOrNull()
            if (soul == null) {
                input.sender.sendMessage(translation.souls.soulNotFound.component)
                return@launch
            }
            if (!accessPolicy.canFreeSoul(input.sender, soul)) {
                input.sender.sendMessage(translation.general.noPermission.component)
                return@launch
            }
            soulsDao.updateSoul(soul.copy(isFree = true))
                .onSuccess {
                    input.sender.sendMessage(translation.souls.soulFreed.component)
                }
                .onFailure {
                    input.sender.sendMessage(translation.souls.couldNotFreeSoul.component)
                }
        }
    }

    private fun executeTeleport(input: SoulsCommand.Intent.TeleportToSoul) {
        ioScope.launch {
            val location = soulsDao.getSoul(input.soulId)
                .getOrNull()
                ?.location
            if (location == null) {
                input.player.sendMessage(translation.souls.soulNotFound.component)
                return@launch
            }
            withContext(dispatchers.Main) {
                input.player.teleport(location)
            }
        }
    }

    fun execute(input: SoulsCommand.Intent) {
        when (input) {
            is SoulsCommand.Intent.List -> executeList(input)
            is SoulsCommand.Intent.Free -> executeFree(input)
            is SoulsCommand.Intent.TeleportToSoul -> executeTeleport(input)
        }
    }
}
