package ru.astrainteractive.aspekt.module.souls.domain

import kotlinx.coroutines.withContext
import org.bukkit.entity.Player
import ru.astrainteractive.aspekt.module.souls.database.dao.SoulsDao
import ru.astrainteractive.aspekt.module.souls.database.model.ItemStackSoul
import ru.astrainteractive.aspekt.module.souls.model.SoulsConfig
import ru.astrainteractive.aspekt.module.souls.util.playSound
import ru.astrainteractive.aspekt.module.souls.util.spawnParticle
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers

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

    suspend fun invoke(player: Player, itemStackSoul: ItemStackSoul): Output {
        return withContext(dispatchers.Main) {
            pickUpExpUseCase.invoke(player, itemStackSoul)

            val isAllItemsPickedUp = pickUpItemsUseCase.invoke(
                player = player,
                itemStackSoul = itemStackSoul
            ) !is PickUpItemsUseCase.Output.SomeItemsRemain

            if (!isAllItemsPickedUp) {
                itemStackSoul.location.playSound(soulContentLeftSoundProvider.invoke())
                itemStackSoul.location.spawnParticle(soulContentLeftParticleProvider.invoke())
                return@withContext Output.SomethingRest
            }
            soulsDao.deleteSoul(itemStackSoul)
            itemStackSoul.location.playSound(soulDisappearSoundProvider.invoke())
            itemStackSoul.location.spawnParticle(soulGoneParticleProvider.invoke())
            return@withContext Output.AllPickedUp
        }
    }
}
