package ru.astrainteractive.soulkeeper.module.souls.domain

import kotlinx.coroutines.withContext
import org.bukkit.entity.Player
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.util.playSoundForPlayer
import ru.astrainteractive.soulkeeper.core.util.spawnParticleForPlayer
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemDatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.util.toBukkitLocation

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

    suspend fun invoke(player: Player, bukkitSoul: ItemDatabaseSoul): Output {
        return withContext(dispatchers.Main) {
            pickUpExpUseCase.invoke(player, bukkitSoul)

            val isAllItemsPickedUp = pickUpItemsUseCase.invoke(
                player = player,
                soul = bukkitSoul
            ) !is PickUpItemsUseCase.Output.SomeItemsRemain

            if (!isAllItemsPickedUp) {
                bukkitSoul.location.toBukkitLocation().playSoundForPlayer(player, soulContentLeftSoundProvider.invoke())
                bukkitSoul.location.toBukkitLocation()
                    .spawnParticleForPlayer(player, soulContentLeftParticleProvider.invoke())
                info { "not all items picked up: $bukkitSoul" }
                return@withContext Output.SomethingRest
            }
            soulsDao.deleteSoul(bukkitSoul.id)
            bukkitSoul.location.toBukkitLocation().playSoundForPlayer(player, soulDisappearSoundProvider.invoke())
            bukkitSoul.location.toBukkitLocation().spawnParticleForPlayer(player, soulGoneParticleProvider.invoke())
            return@withContext Output.AllPickedUp
        }
    }
}
