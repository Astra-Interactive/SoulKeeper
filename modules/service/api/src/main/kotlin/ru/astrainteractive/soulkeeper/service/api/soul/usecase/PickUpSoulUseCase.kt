package ru.astrainteractive.soulkeeper.service.api.soul.usecase

import kotlinx.coroutines.withContext
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.api.getValue
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.soulkeeper.core.platform.api.EffectEmitter
import ru.astrainteractive.soulkeeper.core.platform.api.Experienced
import ru.astrainteractive.soulkeeper.core.plugin.model.SoulsConfig
import ru.astrainteractive.soulkeeper.data.dao.soul.api.SoulRepository
import ru.astrainteractive.soulkeeper.data.dao.soul.model.ItemDatabaseSoul
import ru.astrainteractive.soulkeeper.data.dao.soul.model.StringFormatObject
import ru.astrainteractive.soulkeeper.data.dao.soul.util.hasExp
import ru.astrainteractive.soulkeeper.data.dao.soul.util.withItems
import ru.astrainteractive.soulkeeper.data.dao.soul.util.withoutExp
import ru.astrainteractive.soulkeeper.service.api.soul.api.PlayerInventory
import ru.astrainteractive.soulkeeper.service.api.soul.model.PickUpSoulResult

@Suppress("LongParameterList")
internal class PickUpSoulUseCase(
    private val soulRepository: SoulRepository,
    private val playerInventory: PlayerInventory,
    private val experiencedFactory: Experienced.Resolver<OnlineKPlayer>,
    private val effectEmitter: EffectEmitter,
    private val dispatchers: KotlinDispatchers,
    soulsConfigKrate: CachedKrate<SoulsConfig>
) {
    private val soulsConfig by soulsConfigKrate

    private suspend fun pickUpExp(player: OnlineKPlayer, soul: ItemDatabaseSoul): ItemDatabaseSoul {
        val soulWithoutExp = soul.withoutExp()
        if (!soul.hasExp()) return soulWithoutExp
        withContext(dispatchers.Main) {
            effectEmitter.playSoundForPlayer(
                location = soul.location,
                player = player,
                sound = soulsConfig.sounds.collectXp
            )
            experiencedFactory.create(player).giveExperience(soul.exp)
        }
        soulRepository.updateSoul(soulWithoutExp)
        return soulWithoutExp
    }

    private suspend fun pickUpItems(player: OnlineKPlayer, soul: ItemDatabaseSoul): List<StringFormatObject> {
        if (soul.items.isEmpty()) return emptyList()
        if (!playerInventory.canPickUpItems(player)) return soul.items
        val result = withContext(dispatchers.Main) {
            playerInventory.addItems(player, soul.items)
        }
        if (!result.isAnyItemAdded) return soul.items
        withContext(dispatchers.Main) {
            effectEmitter.playSoundForPlayer(
                location = soul.location,
                player = player,
                sound = soulsConfig.sounds.collectItem
            )
        }
        soulRepository.updateSoul(soul.withItems(result.leftovers))
        return result.leftovers
    }

    private suspend fun showContentLeft(player: OnlineKPlayer, soul: ItemDatabaseSoul) {
        withContext(dispatchers.Main) {
            effectEmitter.playSoundForPlayer(
                location = soul.location,
                player = player,
                sound = soulsConfig.sounds.soulContentLeft
            )
            effectEmitter.spawnParticleForPlayer(
                location = soul.location,
                player = player,
                config = soulsConfig.particles.soulContentLeft
            )
        }
    }

    private suspend fun showSoulGone(player: OnlineKPlayer, soul: ItemDatabaseSoul) {
        withContext(dispatchers.Main) {
            effectEmitter.playSoundForPlayer(
                location = soul.location,
                player = player,
                sound = soulsConfig.sounds.soulDisappear
            )
            effectEmitter.spawnParticleForPlayer(
                location = soul.location,
                player = player,
                config = soulsConfig.particles.soulGone
            )
        }
    }

    suspend operator fun invoke(player: OnlineKPlayer, soul: ItemDatabaseSoul): PickUpSoulResult {
        val soulWithoutExp = pickUpExp(player, soul)
        val leftovers = pickUpItems(player, soulWithoutExp)
        if (leftovers.isNotEmpty()) {
            showContentLeft(player, soulWithoutExp)
            return PickUpSoulResult.SomethingRest
        }
        soulRepository.deleteSoul(soul.id)
        showSoulGone(player, soulWithoutExp)
        return PickUpSoulResult.AllPickedUp
    }
}
