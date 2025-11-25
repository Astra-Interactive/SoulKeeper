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
) : Logger by JUtiltLogger("AspeKt-PickUpSoulUseCase") {
    sealed interface Output {
        data object SomethingRest : Output
        data object AllPickedUp : Output
    }

    suspend fun invoke(player: OnlineMinecraftPlayer, bukkitSoul: ItemDatabaseSoul): Output {
        return withContext(dispatchers.Main) {
            pickUpExpUseCase.invoke(player, bukkitSoul)

            val isAllItemsPickedUp = pickUpItemsUseCase.invoke(
                player = player,
                soul = bukkitSoul
            ) !is PickUpItemsUseCase.Output.SomeItemsRemain

            if (!isAllItemsPickedUp) {
                effectEmitter.playSoundForPlayer(
                    location = bukkitSoul.location,
                    player = player,
                    sound = soulContentLeftSoundProvider.invoke()
                )
                effectEmitter.spawnParticleForPlayer(
                    location = bukkitSoul.location,
                    player = player,
                    config = soulContentLeftParticleProvider.invoke()
                )
                info { "not all items picked up: $bukkitSoul" }
                return@withContext Output.SomethingRest
            }
            soulsDao.deleteSoul(bukkitSoul.id)
            effectEmitter.playSoundForPlayer(
                location = bukkitSoul.location,
                player = player,
                sound = soulDisappearSoundProvider.invoke()
            )
            effectEmitter.spawnParticleForPlayer(
                location = bukkitSoul.location,
                player = player,
                config = soulGoneParticleProvider.invoke()
            )
            return@withContext Output.AllPickedUp
        }
    }
}
