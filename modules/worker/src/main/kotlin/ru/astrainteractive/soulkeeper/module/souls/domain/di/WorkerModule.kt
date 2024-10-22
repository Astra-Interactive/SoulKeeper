package ru.astrainteractive.soulkeeper.module.souls.domain.di

import org.bukkit.event.HandlerList
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.souls.database.di.SoulsDbModule
import ru.astrainteractive.soulkeeper.module.souls.domain.GetNearestSoulUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpExpUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpSoulUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.di.factory.ShowArmorStandUseCaseFactory
import ru.astrainteractive.soulkeeper.module.souls.worker.DeleteSoulWorker
import ru.astrainteractive.soulkeeper.module.souls.worker.FreeSoulWorker
import ru.astrainteractive.soulkeeper.module.souls.worker.ParticleWorker
import ru.astrainteractive.soulkeeper.module.souls.worker.PickUpWorker

class WorkerModule(
    coreModule: CoreModule,
    soulsDbModule: SoulsDbModule
) {
    private val deleteSoulWorker = DeleteSoulWorker(
        soulsDao = soulsDbModule.soulsDao,
        configKrate = coreModule.soulsConfigKrate
    )

    private val freeSoulWorker = FreeSoulWorker(
        soulsDao = soulsDbModule.soulsDao,
        configKrate = coreModule.soulsConfigKrate
    )

    private val particleWorker = ParticleWorker(
        soulsDao = soulsDbModule.soulsDao,
        dispatchers = coreModule.dispatchers,
        soulsConfigKrate = coreModule.soulsConfigKrate,
        showArmorStandUseCase = ShowArmorStandUseCaseFactory(coreModule).create()
    )

    private val pickUpWorker = PickUpWorker(
        pickUpSoulUseCase = PickUpSoulUseCase(
            dispatchers = coreModule.dispatchers,
            pickUpExpUseCase = PickUpExpUseCase(
                collectXpSoundProvider = { coreModule.soulsConfigKrate.cachedValue.sounds.collectXp },
                soulsDao = soulsDbModule.soulsDao
            ),
            pickUpItemsUseCase = PickUpItemsUseCase(
                collectItemSoundProvider = { coreModule.soulsConfigKrate.cachedValue.sounds.collectItem },
                soulsDao = soulsDbModule.soulsDao
            ),
            soulsDao = soulsDbModule.soulsDao,
            soulGoneParticleProvider = { coreModule.soulsConfigKrate.cachedValue.particles.soulGone },
            soulDisappearSoundProvider = { coreModule.soulsConfigKrate.cachedValue.sounds.soulDisappear },
            soulContentLeftParticleProvider = {
                coreModule.soulsConfigKrate.cachedValue.particles.soulContentLeft
            },
            soulContentLeftSoundProvider = {
                coreModule.soulsConfigKrate.cachedValue.sounds.soulContentLeft
            }
        ),
        getNearestSoulUseCase = GetNearestSoulUseCase(
            soulsDao = soulsDbModule.soulsDao
        ),
        onPickUp = {
            particleWorker.onReload()
        }
    )

    val lifecycle: Lifecycle = Lifecycle.Lambda(
        onEnable = {
            coreModule.lifecycle.onEnable()
            soulsDbModule.lifecycle.onEnable()
            particleWorker.onEnable()
            pickUpWorker.onEnable()
            deleteSoulWorker.onEnable()
            freeSoulWorker.onEnable()
        },
        onReload = {
            coreModule.lifecycle.onReload()
            soulsDbModule.lifecycle.onReload()
        },
        onDisable = {
            coreModule.lifecycle.onDisable()
            soulsDbModule.lifecycle.onDisable()
            particleWorker.onDisable()
            pickUpWorker.onDisable()
            deleteSoulWorker.onDisable()
            freeSoulWorker.onDisable()
            HandlerList.unregisterAll(coreModule.plugin)
        }
    )
}
