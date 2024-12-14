package ru.astrainteractive.soulkeeper.module.souls.domain

import kotlinx.coroutines.withContext
import org.bukkit.entity.Player
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.util.playSound
import ru.astrainteractive.soulkeeper.core.util.spawnParticle
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.io.model.BukkitSoul

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
) : Logger by JUtiltLogger("AspeKt-PickUpSoulUseCase") {
    sealed interface Output {
        data object SomethingRest : Output
        data object AllPickedUp : Output
    }

    suspend fun invoke(player: Player, bukkitSoul: BukkitSoul): Output {
        return withContext(dispatchers.Main) {
            pickUpExpUseCase.invoke(player, bukkitSoul)

            val isAllItemsPickedUp = pickUpItemsUseCase.invoke(
                player = player,
                bukkitSoul = bukkitSoul
            ) !is PickUpItemsUseCase.Output.SomeItemsRemain

            if (!isAllItemsPickedUp) {
                bukkitSoul.location.playSound(soulContentLeftSoundProvider.invoke())
                bukkitSoul.location.spawnParticle(soulContentLeftParticleProvider.invoke())
                return@withContext Output.SomethingRest
            }
            soulsDao.deleteSoul(bukkitSoul)
            bukkitSoul.location.playSound(soulDisappearSoundProvider.invoke())
            bukkitSoul.location.spawnParticle(soulGoneParticleProvider.invoke())
            return@withContext Output.AllPickedUp
        }
    }
}
