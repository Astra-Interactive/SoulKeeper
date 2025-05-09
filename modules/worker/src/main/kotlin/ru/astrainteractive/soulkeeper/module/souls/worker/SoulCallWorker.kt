package ru.astrainteractive.soulkeeper.module.souls.worker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.Plugin
import ru.astrainteractive.astralibs.async.CoroutineFeature
import ru.astrainteractive.astralibs.event.flowEvent
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.util.combineInstantly
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.domain.TickFlow
import ru.astrainteractive.soulkeeper.module.souls.renderer.ArmorStandRenderer
import ru.astrainteractive.soulkeeper.module.souls.renderer.SoulParticleRenderer
import ru.astrainteractive.soulkeeper.module.souls.renderer.SoulSoundRenderer
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

/**
 * This worker is required to display soul particles near players
 */
internal class SoulCallWorker(
    private val soulsDao: SoulsDao,
    private val plugin: Plugin,
    private val config: SoulsConfig,
    private val soulParticleRenderer: SoulParticleRenderer,
    private val soulSoundRenderer: SoulSoundRenderer,
    private val soulArmorStandRenderer: ArmorStandRenderer
) : Logger by JUtiltLogger("AspeKt-SoulCallWorker"), Lifecycle {
    private val scope = CoroutineFeature.Default(Dispatchers.IO)

    private val playerRendererJobMap = mutableMapOf<UUID, Job>()

    /**
     * Emit event only when player moved distance away from previous location
     */
    private fun distancedPlayerMoveEvent(player: Player) = flow {
        var savedLocation = org.bukkit.Location(player.world, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE)
        flowEvent<PlayerMoveEvent>(plugin)
            .filterIsInstance<PlayerMoveEvent>()
            .filter { event -> event.player.uniqueId == player.uniqueId }
            .onEach { event ->
                val oldLocation = savedLocation
                val newLocation = event.to
                if (savedLocation.world.uid != newLocation.world.uid) {
                    emit(event)
                } else if (oldLocation.distance(newLocation) >= config.soulCallRadius.div(2)) {
                    emit(event)
                } else {
                    return@onEach
                }
                savedLocation = newLocation.clone()
            }
            .collect()
    }

    private fun getPlayerVisibleSoulsFlow(scope: CoroutineScope, player: Player): Flow<List<DatabaseSoul>> {
        return combineInstantly(
            flow = distancedPlayerMoveEvent(player).shareIn(scope, SharingStarted.Eagerly),
            flow2 = soulsDao.getSoulsChangeFlow(),
            transform = { event, _ -> event }
        ).filterNotNull().map { event ->
            soulsDao.getSoulsNear(event.player.location, config.soulCallRadius)
                .getOrNull()
                .orEmpty()
        }
    }

    private fun startPlayerRendererJob(player: Player) {
        playerRendererJobMap[player.uniqueId]?.cancel()
        playerRendererJobMap[player.uniqueId] = scope.launch {
            combineInstantly(
                flow = TickFlow(1.seconds),
                flow2 = getPlayerVisibleSoulsFlow(this, player).shareIn(this, SharingStarted.Eagerly),
                transform = { _, souls -> souls }
            ).filterNotNull().onEach { souls ->
                coroutineScope {
                    listOf(
                        async { soulParticleRenderer.renderOnce(player, souls) },
                        async { soulSoundRenderer.renderOnce(player, souls) },
                        async { soulArmorStandRenderer.renderOnce(player, souls) }
                    ).awaitAll()
                }
            }.launchIn(this)
        }
    }

    private fun startPlayerJoinEventJob() {
        flowEvent<PlayerJoinEvent>(plugin)
            .filterIsInstance<PlayerJoinEvent>()
            .onEach { event -> startPlayerRendererJob(event.player) }
            .launchIn(scope)
    }

    private fun startPlayerLeaveEventJob() {
        flowEvent<PlayerJoinEvent>(plugin)
            .filterIsInstance<PlayerJoinEvent>()
            .onEach { event -> playerRendererJobMap[event.player.uniqueId]?.cancelAndJoin() }
            .launchIn(scope)
    }

    override fun onEnable() {
        super.onEnable()
        startPlayerLeaveEventJob()
        startPlayerJoinEventJob()
    }

    override fun onDisable() {
        super.onDisable()
        playerRendererJobMap.values.onEach(Job::cancel).clear()
        scope.cancel()
    }
}
