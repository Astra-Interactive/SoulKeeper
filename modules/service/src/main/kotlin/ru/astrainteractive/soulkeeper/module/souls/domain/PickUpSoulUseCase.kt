package ru.astrainteractive.soulkeeper.module.souls.domain

import kotlinx.coroutines.withContext
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemDatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter

@Suppress("LongParameterList")
internal class PickUpSoulUseCase(
    private val dispatchers: KotlinDispatchers,
    private val pickUpExpUseCase: PickUpExpUseCase,
    private val pickUpItemsUseCase: PickUpItemsUseCase,
    private val soulsDao: SoulsDao,
    private val soulDisappearSoundProvider: () -> SoulsConfig.Sounds.SoundConfig,
    private val soulGoneParticleProvider: () -> SoulsConfig.Particles.Particle,
    private val soulContentLeftSoundProvider: () -> SoulsConfig.Sounds.SoundConfig,
    private val soulContentLeftParticleProvider: () -> SoulsConfig.Particles.Particle,
    private val effectEmitter: EffectEmitter
) : Logger by JUtiltLogger("SoulKeeper-PickUpSoulUseCase") {
    sealed interface Output {
        data object SomethingRest : Output
        data object AllPickedUp : Output
    }

    suspend fun invoke(player: OnlineMinecraftPlayer, soul: ItemDatabaseSoul): Output {
        return withContext(dispatchers.Main) {
            pickUpExpUseCase.invoke(player, soul)
            val updatedSoul = soul.copy(exp = 0)

            val pickUpOutput = pickUpItemsUseCase.invoke(
                player = player,
                soul = updatedSoul
            )

            val isAllItemsPickedUp = when (pickUpOutput) {
                PickUpItemsUseCase.Output.NoItemsPresent,
                PickUpItemsUseCase.Output.ItemsCollected -> true

                PickUpItemsUseCase.Output.SomeItemsRemain -> false
            }

            if (!isAllItemsPickedUp) {
                effectEmitter.playSoundForPlayer(
                    location = updatedSoul.location,
                    player = player,
                    sound = soulContentLeftSoundProvider.invoke()
                )
                effectEmitter.spawnParticleForPlayer(
                    location = updatedSoul.location,
                    player = player,
                    config = soulContentLeftParticleProvider.invoke()
                )
                return@withContext Output.SomethingRest
            }
            soulsDao.deleteSoul(updatedSoul.id)
            effectEmitter.playSoundForPlayer(
                location = updatedSoul.location,
                player = player,
                sound = soulDisappearSoundProvider.invoke()
            )
            effectEmitter.spawnParticleForPlayer(
                location = updatedSoul.location,
                player = player,
                config = soulGoneParticleProvider.invoke()
            )
            return@withContext Output.AllPickedUp
        }
    }
}
