package ru.astrainteractive.soulkeeper.module.souls.worker

import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.GameMode
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.klibs.kstorage.api.Krate
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.soulkeeper.core.job.LifecycleCoroutineWorker
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.util.getValue
import ru.astrainteractive.soulkeeper.core.util.playSound
import ru.astrainteractive.soulkeeper.core.util.spawnParticle
import ru.astrainteractive.soulkeeper.module.souls.database.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandStubUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandUseCase
import kotlin.time.Duration.Companion.seconds

/**
 * This worker is required to display soul particles near players
 */
internal class ParticleWorker(
    private val soulsDao: SoulsDao,
    private val dispatchers: KotlinDispatchers,
    private val showArmorStandUseCase: ShowArmorStandUseCase,
    soulsConfigKrate: Krate<SoulsConfig>
) : LifecycleCoroutineWorker("ParticleWorker"), Logger by JUtiltLogger("AspeKt-ParticleWorker") {
    private val soulsConfig by soulsConfigKrate

    override fun getConfig(): Config = Config(
        delay = 10.seconds,
        initialDelay = 0.seconds
    )

    private val mutex = Mutex()
    private var lastJob: Job? = null

    private val soulByArmorStandId = HashMap<Long, Int>()
    private fun rememberSoulArmorStandAndGetId(soul: DatabaseSoul): Int {
        if (showArmorStandUseCase is ShowArmorStandStubUseCase) return -1
        return soulByArmorStandId.getOrPut(soul.id) { showArmorStandUseCase.generateEntityId() }
    }

    override fun execute() {
        scope.launch {
            mutex.withLock {
                lastJob?.cancelAndJoin()
                lastJob = coroutineContext.job
                Bukkit.getOnlinePlayers()
                    .onEach { player -> showArmorStandUseCase.destroy(player, soulByArmorStandId.values) }
                    .flatMap { player ->
                        soulsDao.getSoulsNear(location = player.location, radius = soulsConfig.soulCallRadius)
                            .getOrNull()
                            .orEmpty()
                            .filter {
                                it.isFree
                                    .or(it.ownerUUID == player.uniqueId)
                                    .or(player.gameMode == GameMode.SPECTATOR)
                            }
                            .map { soul ->
                                showArmorStandUseCase.show(rememberSoulArmorStandAndGetId(soul), player, soul)
                                withContext(dispatchers.Main) {
                                    player.playSound(soul.location, soulsConfig.sounds.calling)
                                }
                                async {
                                    repeat(getConfig().delay.inWholeMilliseconds.div(1000L).toInt()) {
                                        delay(1000L)
                                        if (soul.hasXp) {
                                            player.spawnParticle(soul.location, soulsConfig.particles.soulXp)
                                        }
                                        if (soul.hasItems) {
                                            player.spawnParticle(soul.location, soulsConfig.particles.soulItems)
                                        }
                                    }
                                }
                            }
                    }.awaitAll()
            }
        }
    }

    override fun onDisable() {
        super.onDisable()
        Bukkit.getOnlinePlayers()
            .onEach { player -> showArmorStandUseCase.destroy(player, soulByArmorStandId.values) }
    }
}
