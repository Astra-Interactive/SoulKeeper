package ru.astrainteractive.soulkeeper.command.souls

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import ru.astrainteractive.astralibs.command.api.brigadier.sender.KCommandSender
import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.astralibs.kyori.unwrap
import ru.astrainteractive.astralibs.server.KAudience
import ru.astrainteractive.astralibs.server.Locatable
import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.astralibs.server.location.dist
import ru.astrainteractive.astralibs.server.permission.KPermissible
import ru.astrainteractive.astralibs.server.player.KPlayer
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.astralibs.util.clickable
import ru.astrainteractive.astralibs.util.isEmpty
import ru.astrainteractive.astralibs.util.orEmpty
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.klibs.mikro.core.util.tryCast
import ru.astrainteractive.soulkeeper.core.datetime.TimeAgoFormatter
import ru.astrainteractive.soulkeeper.core.datetime.TimeAgoTranslationFormatter
import ru.astrainteractive.soulkeeper.core.plugin.PluginPermission
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.Soul
import kotlin.collections.filter

internal class SoulsCommandExecutor(
    private val ioScope: CoroutineScope,
    private val soulsDao: SoulsDao,
    private val dispatchers: KotlinDispatchers,
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
        return soulsDao.getSouls()
            .getOrNull()
            .orEmpty()
            .filter { soul ->
                sender.tryCast<Locatable>()
                    ?.getLocation()
                    ?.worldName == soul.location.worldName
            }
            .filter { soul ->
                soul.isFree
                    .or(sender.tryCast<KPermissible>()?.hasPermission(PluginPermission.ViewAllSouls) == true)
                    .or(sender.tryCast<KPlayer>()?.uuid == soul.ownerUUID)
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

    private fun KCommandSender.canFreeSouls(soul: DatabaseSoul): Boolean {
        val sender = this
        val hasPermission = sender.tryCast<KPermissible>()?.hasPermission(PluginPermission.FreeAllSouls) == true
        val isOwner = sender.tryCast<KPlayer>()?.uuid == soul.ownerUUID
        if (soul.isFree) return false
        if (!hasPermission) return false
        if (!isOwner) return false
        return true
    }

    private fun createFreeSoulComponent(sender: KCommandSender, soul: DatabaseSoul): Component? {
        return if (!sender.canFreeSouls(soul)) {
            null
        } else {
            translation.souls.freeSoul
                .component
                .appendSpace()
                .clickable { execute(SoulsCommand.Intent.Free(sender, soul.id)) }
        }
    }

    private fun KCommandSender.canTeleportToSoul(): Boolean {
        val sender = this
        if (sender !is OnlineKPlayer) return false
        if (!sender.hasPermission(PluginPermission.TeleportToSouls)) {
            return false
        }
        return true
    }

    private fun createTeleportSoulComponent(sender: KCommandSender, soul: DatabaseSoul): Component? {
        if (!sender.canTeleportToSoul()) return null
        return translation.souls.teleportToSoul
            .component
            .clickable { execute(SoulsCommand.Intent.TeleportToSoul(sender, soul.id)) }
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
                        input.sender.tryCast<KAudience>()?.sendMessage(title)
                        return@launch
                    }

                    input.sender.tryCast<KAudience>()?.sendMessage(translation.souls.listSoulsTitle.component)

                    pageSouls.forEachIndexed { i, soul ->
                        val component = createListingItemComponent(
                            soul = soul,
                            page = input.page,
                            i = i,
                            location = input.sender
                                .tryCast<Locatable>()
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
                        input.sender.tryCast<KAudience>()?.sendMessage(component)
                    }
                    input.sender.tryCast<KAudience>()?.sendMessage(createPagingMessage(input, maxPages))
                }
            }

            is SoulsCommand.Intent.Free -> {
                ioScope.launch {
                    val newSoul = soulsDao.getSoul(input.soulId)
                        .getOrNull()
                        ?.copy(isFree = true)
                    if (newSoul == null) {
                        input.sender.tryCast<KAudience>()?.sendMessage(translation.souls.soulNotFound.component)
                        return@launch
                    }
                    soulsDao.updateSoul(newSoul)
                        .onSuccess {
                            input.sender.tryCast<KAudience>()?.sendMessage(translation.souls.soulFreed.component)
                        }
                        .onFailure {
                            input.sender.tryCast<KAudience>()?.sendMessage(translation.souls.couldNotFreeSoul.component)
                        }
                }
            }

            is SoulsCommand.Intent.TeleportToSoul -> {
                ioScope.launch {
                    val player = input.sender.tryCast<OnlineKPlayer>() ?: return@launch
                    val location = soulsDao.getSoul(input.soulId)
                        .getOrNull()
                        ?.location
                    if (location == null) {
                        player.sendMessage(translation.souls.soulNotFound.component)
                        return@launch
                    }
                    withContext(dispatchers.Main) {
                        player.teleport(location)
                    }
                }
            }
        }
    }
}
