package ru.astrainteractive.soulkeeper.command.soul.command

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import ru.astrainteractive.astralibs.command.api.brigadier.sender.ConsoleKCommandSender
import ru.astrainteractive.astralibs.command.api.brigadier.sender.KCommandSender
import ru.astrainteractive.astralibs.command.api.brigadier.sender.KPlayerKCommandSender
import ru.astrainteractive.astralibs.localization.component.clickable
import ru.astrainteractive.astralibs.localization.component.orEmpty
import ru.astrainteractive.astralibs.server.location.KLocation
import ru.astrainteractive.astralibs.server.location.dist
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.api.getValue
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.klibs.mikro.core.util.tryCast
import ru.astrainteractive.soulkeeper.command.soul.api.SoulsCommand
import ru.astrainteractive.soulkeeper.command.soul.util.append
import ru.astrainteractive.soulkeeper.command.soul.util.canFreeSoul
import ru.astrainteractive.soulkeeper.command.soul.util.canTeleportToSoul
import ru.astrainteractive.soulkeeper.command.soul.util.format
import ru.astrainteractive.soulkeeper.command.soul.util.timeAgo
import ru.astrainteractive.soulkeeper.core.plugin.model.PluginPermission
import ru.astrainteractive.soulkeeper.core.plugin.model.PluginTranslation
import ru.astrainteractive.soulkeeper.data.dao.soul.api.SoulRepository
import ru.astrainteractive.soulkeeper.data.dao.soul.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.data.dao.soul.model.Soul
import java.util.Locale
import kotlin.time.Clock

internal class SoulsCommandExecutor(
    private val ioScope: CoroutineScope,
    private val soulRepository: SoulRepository,
    private val clock: Clock,
    private val dispatchers: KotlinDispatchers,
    translationKrate: CachedKrate<PluginTranslation>
) {
    private val translation by translationKrate

    private val clickedIntents = MutableSharedFlow<SoulsCommand.Intent>(
        extraBufferCapacity = CLICK_BUFFER_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private fun createPagingMessage(
        input: SoulsCommand.Intent.List,
        maxPages: Int
    ): Component {
        val locale = input.sender.locale
        val nextPageComponent = translation.soulList.nextPage.toComponent(locale)
            .clickable { clickedIntents.tryEmit(input.copy(page = input.page.plus(1))) }
            .takeIf { input.page < maxPages }
            .orEmpty()
        val prevPageComponent = translation.soulList.previousPage.toComponent(locale)
            .clickable { clickedIntents.tryEmit(input.copy(page = input.page.plus(-1))) }
            .appendSpace().takeIf { input.page > 0 }
            .orEmpty()
        return nextPageComponent.append(prevPageComponent, true)
    }

    private suspend fun loadFilteredSouls(sender: KCommandSender): List<DatabaseSoul> {
        val souls = soulRepository.loadSouls()
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

    private fun findPageSouls(souls: List<DatabaseSoul>, page: Int): List<DatabaseSoul> {
        val pageOffset = page.times(SoulsCommand.PAGE_SIZE)
        val start = pageOffset.coerceIn(0, souls.size)
        val end = pageOffset
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
        location: KLocation?,
        locale: Locale
    ): Component {
        val timeAgoFormatted = translation.format(soul.createdAt.timeAgo(now = clock.now()))
        val pageOffset = page.times(SoulsCommand.PAGE_SIZE)
        return translation.soulList.entry(
            index = pageOffset.plus(i.plus(1)),
            owner = soul.ownerLastName,
            timeAgo = timeAgoFormatted,
            x = soul.location.x.toInt(),
            y = soul.location.y.toInt(),
            z = soul.location.z.toInt(),
            distance = location
                ?.dist(soul.location)
                ?.toInt()
                ?: 0
        ).toComponent(locale)
    }

    private fun createFreeSoulComponent(
        sender: KCommandSender,
        soul: DatabaseSoul
    ): Component? {
        if (soul.isFree) return null
        if (!sender.canFreeSoul(soul)) return null
        return translation.soulList.freeButton
            .toComponent(sender.locale)
            .appendSpace()
            .clickable { clickedIntents.tryEmit(SoulsCommand.Intent.Free(sender, soul.id)) }
    }

    private fun createTeleportSoulComponent(
        sender: KCommandSender,
        soul: DatabaseSoul
    ): Component? {
        if (sender !is KPlayerKCommandSender) return null
        if (!sender.canTeleportToSoul()) return null
        return translation.soulList.teleportButton
            .toComponent(sender.locale)
            .clickable { clickedIntents.tryEmit(SoulsCommand.Intent.TeleportToSoul(sender.instance, soul.id)) }
    }

    private fun executeList(input: SoulsCommand.Intent.List) {
        ioScope.launch {
            val filteredSouls = loadFilteredSouls(input.sender)
            val maxPages = filteredSouls.size.div(SoulsCommand.PAGE_SIZE)
            val pageSouls = findPageSouls(filteredSouls, input.page)
            if (pageSouls.isEmpty()) {
                input.sender.sendMessage(translation.soulList.emptyPage(input.page.plus(1)))
                return@launch
            }

            input.sender.sendMessage(translation.soulList.title)

            pageSouls.forEachIndexed { i, soul ->
                val component = createListingItemComponent(
                    soul = soul,
                    page = input.page,
                    i = i,
                    location = input.sender
                        .tryCast<KPlayerKCommandSender>()
                        ?.instance
                        ?.getLocation(),
                    locale = input.sender.locale
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
            input.sender.sendMessage(
                createPagingMessage(
                    input = input,
                    maxPages = maxPages
                )
            )
        }
    }

    private fun executeFree(input: SoulsCommand.Intent.Free) {
        ioScope.launch {
            val soul = soulRepository.loadSoul(input.soulId).getOrNull()
            if (soul == null) {
                input.sender.sendMessage(translation.soul.notFound)
                return@launch
            }
            if (!input.sender.canFreeSoul(soul)) {
                input.sender.sendMessage(translation.commandError.noPermission)
                return@launch
            }
            soulRepository.updateSoul(soul.copy(isFree = true))
                .onSuccess {
                    input.sender.sendMessage(translation.soul.freed)
                }
                .onFailure {
                    input.sender.sendMessage(translation.soul.freeFailed)
                }
        }
    }

    private fun executeTeleport(input: SoulsCommand.Intent.TeleportToSoul) {
        ioScope.launch {
            val location = soulRepository.loadSoul(input.soulId)
                .getOrNull()
                ?.location
            if (location == null) {
                input.player.sendMessage(translation.soul.notFound)
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

    init {
        clickedIntents
            .onEach(::execute)
            .launchIn(ioScope)
    }

    companion object {
        private const val CLICK_BUFFER_CAPACITY = 16
    }
}
