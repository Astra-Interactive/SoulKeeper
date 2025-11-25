package ru.astrainteractive.soulkeeper.module.souls.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import ru.astrainteractive.astralibs.async.withTimings
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.server.MinecraftNativeBridge
import ru.astrainteractive.astralibs.server.location.Location
import ru.astrainteractive.astralibs.server.location.dist
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.klibs.mikro.core.coroutines.CoroutineFeature
import ru.astrainteractive.klibs.mikro.core.coroutines.TickFlow
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.util.ThrottleExecutor
import ru.astrainteractive.soulkeeper.core.util.combineInstantly
import ru.astrainteractive.soulkeeper.core.util.toDatabaseLocation
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.platform.event.EventProvider
import ru.astrainteractive.soulkeeper.module.souls.renderer.ArmorStandRenderer
import ru.astrainteractive.soulkeeper.module.souls.renderer.SoulParticleRenderer
import ru.astrainteractive.soulkeeper.module.souls.renderer.SoulSoundRenderer
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

/**
 * This worker is required to display soul particles near players
 */
@Suppress("LongParameterList")
internal class SoulCallWorker(
    private val soulsDao: SoulsDao,
    private val config: SoulsConfig,
    private val soulParticleRenderer: SoulParticleRenderer,
    private val soulSoundRenderer: SoulSoundRenderer,
    private val soulArmorStandRenderer: ArmorStandRenderer,
    private val eventProvider: EventProvider,
    minecraftNativeBridge: MinecraftNativeBridge
) : Logger by JUtiltLogger("AspeKt-SoulCallWorker"),
    Lifecycle,
    MinecraftNativeBridge by minecraftNativeBridge {
    private val scope = CoroutineFeature.IO.withTimings()

    private val playerRendererJobMap = mutableMapOf<UUID, Job>()

    /**
     * Emit event only when player moved distance away from previous location
     */
    private fun distancedPlayerMoveEvent(player: OnlineMinecraftPlayer) = flow {
        var savedLocation = Location(
            x = Double.MIN_VALUE,
            y = Double.MIN_VALUE,
            z = Double.MIN_VALUE,
            worldName = player.asLocatable()
                .getLocation()
                .worldName
        )
        eventProvider.getPlayerMoveEvent()
            .filter { event -> event.player.uuid == player.uuid }
            .onEach { event ->
                val newLocation = event.to
                if (savedLocation.worldName != newLocation.worldName) {
                    emit(event)
                } else if (savedLocation.dist(newLocation) >= config.soulCallRadius.div(2)) {
                    emit(event)
                } else {
                    return@onEach
                }
                savedLocation = newLocation
            }
            .collect()
    }

    private fun getPlayerVisibleSoulsFlow(
        scope: CoroutineScope,
        player: OnlineMinecraftPlayer
    ): Flow<List<DatabaseSoul>> {
        return combineInstantly(
            flow = distancedPlayerMoveEvent(player).shareIn(scope, SharingStarted.Eagerly, 1),
            flow2 = soulsDao.getSoulsChangeFlow(),
            transform = { event, _ -> event }
        ).filterNotNull().map { event ->
            val location = event.player.asLocatable().getLocation().toDatabaseLocation()
            soulsDao.getSoulsNear(location, config.soulCallRadius)
                .getOrNull()
                .orEmpty()
                .filter { soul -> soul.ownerUUID == player.uuid || soul.isFree }
        }
    }

    private fun startPlayerRendererJob(player: OnlineMinecraftPlayer) {
        playerRendererJobMap[player.uuid]?.cancel()
        playerRendererJobMap[player.uuid] = scope.launch {
            val soundThrottleExecutor = ThrottleExecutor(5.seconds)
            val particleThrottleExecutor = ThrottleExecutor(2.seconds)
            combineInstantly(
                flow = TickFlow(1.seconds),
                flow2 = getPlayerVisibleSoulsFlow(this, player).shareIn(this, SharingStarted.Eagerly, 1),
                transform = { _, souls -> souls }
            ).filterNotNull().onEach { souls ->
                listOf(
                    async { particleThrottleExecutor.execute { soulParticleRenderer.renderOnce(player, souls) } },
                    async { soundThrottleExecutor.execute { soulSoundRenderer.renderOnce(player, souls) } },
                    async { soulArmorStandRenderer.renderOnce(player, souls) }
                ).awaitAll()
            }.collect()
        }
    }

    private fun startPlayerJoinEventJob() {
        eventProvider.getPlayerJoinEvent()
            .onEach { event -> startPlayerRendererJob(event.player) }
            .launchIn(scope)
    }

    private fun startPlayerLeaveEventJob() {
        eventProvider.getPlayerLeaveEvent()
            .onEach { event -> playerRendererJobMap[event.player.uuid]?.cancelAndJoin() }
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
