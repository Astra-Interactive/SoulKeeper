package ru.astrainteractive.soulkeeper.module.souls.worker.call

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import ru.astrainteractive.klibs.kstorage.api.Krate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.util.playSoundForPlayer
import ru.astrainteractive.soulkeeper.core.util.spawnParticleForPlayer
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandStubUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandUseCase

@Suppress("TooManyFunctions")
internal class SoulCallRendererImpl(
    private val showArmorStandUseCase: ShowArmorStandUseCase,
    private val soulsDao: SoulsDao,
    private val dispatchers: KotlinDispatchers,
    soulsConfigKrate: Krate<SoulsConfig>
) : SoulCallRenderer {
    private val soulsConfig by soulsConfigKrate

    /**
     * [DatabaseSoul.id] to [DatabaseSoul]
     */
    private val trackedSouls = HashMap<Long, DatabaseSoul>()

    /**
     * [DatabaseSoul.id] to entity id
     */
    private val soulByArmorStandId = HashMap<Long, Int>()

    private fun rememberSoulArmorStandId(soul: DatabaseSoul) {
        if (showArmorStandUseCase is ShowArmorStandStubUseCase) return
        if (soulsConfig.displaySoulTitles) return
        soulByArmorStandId.getOrPut(soul.id) { showArmorStandUseCase.generateEntityId() }
    }

    private fun isNear(
        soul: DatabaseSoul,
        player: Player
    ): Boolean {
        if (soul.location.world.name != player.location.world.name) return false
        return soul.location.distance(player.location) < soulsConfig.soulCallRadius
    }

    private fun isVisible(
        soul: DatabaseSoul,
        player: Player
    ) = soul.isFree
        .or(soul.ownerUUID == player.uniqueId)
        .or(player.gameMode == GameMode.SPECTATOR)

    override suspend fun rememberSoul(soul: DatabaseSoul) {
        rememberSoulArmorStandId(soul)
        trackedSouls.getOrPut(soul.id) { soul }
    }

    override suspend fun updateSoulsForPlayer(player: Player) = withContext(dispatchers.IO) {
        soulsDao.getSoulsNear(location = player.location, radius = soulsConfig.soulCallRadius)
            .getOrNull()
            .orEmpty()
            .filter { soul -> isVisible(soul, player) }
            .filter { soul -> isNear(soul, player) }
            .onEach { soul -> rememberSoul(soul) }
            .forEach { soul -> trackedSouls.getOrPut(soul.id) { soul } }
    }

    override suspend fun displayArmorStands(player: Player) = withContext(dispatchers.IO) {
        showArmorStandUseCase.destroy(player, soulByArmorStandId.values)
        trackedSouls
            .values
            .filter { soul -> isVisible(soul, player) }
            .filter { soul -> isNear(soul, player) }
            .forEach { soul ->
                val armorStandId = soulByArmorStandId[soul.id] ?: return@forEach
                showArmorStandUseCase.show(armorStandId, player, soul)
            }
    }

    override suspend fun playSounds(player: Player) = withContext(dispatchers.Main) {
        trackedSouls
            .values
            .filter { soul -> isVisible(soul, player) }
            .filter { soul -> isNear(soul, player) }
            .forEach { soul -> soul.location.playSoundForPlayer(player, soulsConfig.sounds.calling) }
    }

    override suspend fun playSoundsContinuously(player: Player) = coroutineScope {
        while (isActive) {
            withContext(dispatchers.Main) {
                playSounds(player)
            }
            delay(5000L)
        }
    }

    override suspend fun displayParticlesContinuously(player: Player) = coroutineScope {
        while (isActive) {
            val filteredSouls = trackedSouls.values
                .filter { soul -> isVisible(soul, player) }
                .filter { soul -> isNear(soul, player) }
            withContext(dispatchers.Main) {
                filteredSouls.forEach { soul ->
                    if (soul.hasXp) {
                        soul.location.spawnParticleForPlayer(player, soulsConfig.particles.soulXp)
                    }
                    if (soul.hasItems) {
                        soul.location.spawnParticleForPlayer(player, soulsConfig.particles.soulItems)
                    }
                }
            }
            delay(1000L)
        }
    }

    override suspend fun removeSoul(soul: DatabaseSoul) {
        Bukkit.getOnlinePlayers().forEach { player ->
            val armorStandId = soulByArmorStandId.remove(soul.id) ?: return@forEach
            showArmorStandUseCase.destroy(player, listOf(armorStandId))
        }
        trackedSouls.remove(soul.id)
    }

    override fun clear() {
        Bukkit.getOnlinePlayers().forEach { player ->
            showArmorStandUseCase.destroy(player, soulByArmorStandId.values)
        }
        trackedSouls.clear()
        soulByArmorStandId.clear()
    }
}
