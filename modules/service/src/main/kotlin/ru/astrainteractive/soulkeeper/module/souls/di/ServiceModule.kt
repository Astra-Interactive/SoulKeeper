package ru.astrainteractive.soulkeeper.module.souls.di

import kotlinx.coroutines.flow.flowOf
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.service.TickFlowService
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
import ru.astrainteractive.soulkeeper.module.souls.service.DeleteSoulWorker
import ru.astrainteractive.soulkeeper.module.souls.service.FreeSoulWorker
import ru.astrainteractive.soulkeeper.module.souls.service.PickUpWorker
import ru.astrainteractive.soulkeeper.module.souls.service.SoulCallWorker
import kotlin.time.Duration.Companion.seconds

class ServiceModule(
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

    private val deleteSoulService = TickFlowService(
        coroutineContext = coreModule.dispatchers.IO,
        delay = flowOf(60.seconds),
        executor = DeleteSoulWorker(
            soulsDao = soulsDaoModule.soulsDao,
            configKrate = coreModule.soulsConfigKrate,
        )
    )

    private val freeSoulService = TickFlowService(
        coroutineContext = coreModule.dispatchers.IO,
        delay = flowOf(60.seconds),
        executor = FreeSoulWorker(
            soulsDao = soulsDaoModule.soulsDao,
            configKrate = coreModule.soulsConfigKrate,
        )
    )

    private val soulCallWorker = SoulCallWorker(
        soulsDao = soulsDaoModule.soulsDao,
        plugin = bukkitCoreModule.plugin,
        config = coreModule.soulsConfigKrate.cachedValue,
        soulParticleRenderer = soulParticleRenderer,
        soulSoundRenderer = soulSoundRenderer,
        soulArmorStandRenderer = armorStandRenderer
    )

    private val pickUpSoulService = TickFlowService(
        coroutineContext = coreModule.dispatchers.IO,
        delay = flowOf(3.seconds),
        executor = PickUpWorker(
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
    )

    val lifecycle: Lifecycle = Lifecycle.Lambda(
        onEnable = {
            soulCallWorker.onEnable()
            pickUpSoulService.onCreate()
            deleteSoulService.onCreate()
            freeSoulService.onCreate()
        },
        onDisable = {
            soulCallWorker.onDisable()
            pickUpSoulService.onDestroy()
            deleteSoulService.onDestroy()
            freeSoulService.onDestroy()
        }
    )
}
