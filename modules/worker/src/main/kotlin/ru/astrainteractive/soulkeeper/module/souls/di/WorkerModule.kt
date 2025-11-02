package ru.astrainteractive.soulkeeper.module.souls.di

import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.core.di.BukkitCoreModule
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.souls.domain.GetNearestSoulUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpExpUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpSoulUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.di.factory.ShowArmorStandUseCaseFactory
import ru.astrainteractive.soulkeeper.module.souls.renderer.ArmorStandRenderer
import ru.astrainteractive.soulkeeper.module.souls.renderer.SoulParticleRenderer
import ru.astrainteractive.soulkeeper.module.souls.renderer.SoulSoundRenderer
import ru.astrainteractive.soulkeeper.module.souls.worker.DeleteSoulWorker
import ru.astrainteractive.soulkeeper.module.souls.worker.FreeSoulWorker
import ru.astrainteractive.soulkeeper.module.souls.worker.PickUpWorker
import ru.astrainteractive.soulkeeper.module.souls.worker.SoulCallWorker

class WorkerModule(
    coreModule: CoreModule,
    bukkitCoreModule: BukkitCoreModule,
    soulsDaoModule: SoulsDaoModule
) {

    private val showArmorStandUseCase = ShowArmorStandUseCaseFactory(coreModule).create()

    private val armorStandRenderer = ArmorStandRenderer(
        soulsConfigKrate = coreModule.soulsConfigKrate,
        showArmorStandUseCase = showArmorStandUseCase
    )
    private val soulParticleRenderer = SoulParticleRenderer(
        soulsConfigKrate = coreModule.soulsConfigKrate,
        dispatchers = coreModule.dispatchers
    )
    private val soulSoundRenderer = SoulSoundRenderer(
        dispatchers = coreModule.dispatchers,
        soulsConfigKrate = coreModule.soulsConfigKrate
    )

    private val deleteSoulWorker = DeleteSoulWorker(
        soulsDao = soulsDaoModule.soulsDao,
        configKrate = coreModule.soulsConfigKrate,
    )

    private val freeSoulWorker = FreeSoulWorker(
        soulsDao = soulsDaoModule.soulsDao,
        configKrate = coreModule.soulsConfigKrate,
    )

    private val soulCallWorker = SoulCallWorker(
        soulsDao = soulsDaoModule.soulsDao,
        plugin = bukkitCoreModule.plugin,
        config = coreModule.soulsConfigKrate.cachedValue,
        soulParticleRenderer = soulParticleRenderer,
        soulSoundRenderer = soulSoundRenderer,
        soulArmorStandRenderer = armorStandRenderer
    )

    private val pickUpWorker = PickUpWorker(
        pickUpSoulUseCase = PickUpSoulUseCase(
            dispatchers = coreModule.dispatchers,
            pickUpExpUseCase = PickUpExpUseCase(
                collectXpSoundProvider = { coreModule.soulsConfigKrate.cachedValue.sounds.collectXp },
                soulsDao = soulsDaoModule.soulsDao
            ),
            pickUpItemsUseCase = PickUpItemsUseCase(
                collectItemSoundProvider = { coreModule.soulsConfigKrate.cachedValue.sounds.collectItem },
                soulsDao = soulsDaoModule.soulsDao
            ),
            soulsDao = soulsDaoModule.soulsDao,
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
            soulsDao = soulsDaoModule.soulsDao
        ),
        soulsDao = soulsDaoModule.soulsDao
    )

    val lifecycle: Lifecycle = Lifecycle.Lambda(
        onEnable = {
            soulCallWorker.onEnable()
            pickUpWorker.onEnable()
            deleteSoulWorker.onEnable()
            freeSoulWorker.onEnable()
        },
        onDisable = {
            soulCallWorker.onDisable()
            pickUpWorker.onDisable()
            deleteSoulWorker.onDisable()
            freeSoulWorker.onDisable()
        }
    )
}
