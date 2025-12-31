package ru.astrainteractive.soulkeeper.module.souls.di

import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.flowOf
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.core.service.ThrottleTickFlowService
import ru.astrainteractive.soulkeeper.module.souls.domain.GetNearestSoulUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpExpUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpSoulUseCase
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
    soulsDaoModule: SoulsDaoModule,
    platformServiceModule: PlatformServiceModule

) {

    private val armorStandRenderer = ArmorStandRenderer(
        soulsConfigKrate = coreModule.soulsConfigKrate,
        showArmorStandUseCase = platformServiceModule.showArmorStandUseCase,
        platformServer = platformServiceModule.platformServer
    )
    private val soulParticleRenderer = SoulParticleRenderer(
        soulsConfigKrate = coreModule.soulsConfigKrate,
        dispatchers = coreModule.dispatchers,
        effectEmitter = platformServiceModule.effectEmitter
    )
    private val soulSoundRenderer = SoulSoundRenderer(
        dispatchers = coreModule.dispatchers,
        soulsConfigKrate = coreModule.soulsConfigKrate,
        effectEmitter = platformServiceModule.effectEmitter
    )

    private val deleteSoulService = ThrottleTickFlowService(
        coroutineContext = SupervisorJob() + coreModule.dispatchers.IO,
        delay = flowOf(60.seconds),
        executor = DeleteSoulWorker(
            soulsDao = soulsDaoModule.soulsDao,
            configKrate = coreModule.soulsConfigKrate,
        )
    )

    private val freeSoulService = ThrottleTickFlowService(
        coroutineContext = SupervisorJob() + coreModule.dispatchers.IO,
        delay = flowOf(60.seconds),
        executor = FreeSoulWorker(
            soulsDao = soulsDaoModule.soulsDao,
            configKrate = coreModule.soulsConfigKrate,
        )
    )

    private val soulCallWorker = SoulCallWorker(
        soulsDao = soulsDaoModule.soulsDao,
        config = coreModule.soulsConfigKrate.cachedValue,
        soulParticleRenderer = soulParticleRenderer,
        soulSoundRenderer = soulSoundRenderer,
        soulArmorStandRenderer = armorStandRenderer,
        eventProvider = platformServiceModule.eventProvider,
        minecraftNativeBridge = platformServiceModule.minecraftNativeBridge
    )

    private val pickUpExpUseCase: PickUpExpUseCase = PickUpExpUseCase(
        collectXpSoundProvider = { coreModule.soulsConfigKrate.cachedValue.sounds.collectXp },
        soulsDao = soulsDaoModule.soulsDao,
        effectEmitter = platformServiceModule.effectEmitter,
        experiencedFactory = platformServiceModule.onlineMinecraftPlayerExperiencedFactory,
        dispatchers = coreModule.dispatchers
    )
    private val pickUpSoulService = ThrottleTickFlowService(
        coroutineContext = SupervisorJob() + coreModule.dispatchers.IO,
        delay = flowOf(3.seconds),
        executor = PickUpWorker(
            pickUpSoulUseCase = PickUpSoulUseCase(
                dispatchers = coreModule.dispatchers,
                pickUpExpUseCase = pickUpExpUseCase,
                pickUpItemsUseCase = platformServiceModule.pickUpItemsUseCase,
                soulsDao = soulsDaoModule.soulsDao,
                soulGoneParticleProvider = { coreModule.soulsConfigKrate.cachedValue.particles.soulGone },
                soulDisappearSoundProvider = { coreModule.soulsConfigKrate.cachedValue.sounds.soulDisappear },
                soulContentLeftParticleProvider = {
                    coreModule.soulsConfigKrate.cachedValue.particles.soulContentLeft
                },
                soulContentLeftSoundProvider = {
                    coreModule.soulsConfigKrate.cachedValue.sounds.soulContentLeft
                },
                effectEmitter = platformServiceModule.effectEmitter
            ),
            getNearestSoulUseCase = GetNearestSoulUseCase(
                soulsDao = soulsDaoModule.soulsDao,
                minecraftNativeBridge = platformServiceModule.minecraftNativeBridge
            ),
            soulsDao = soulsDaoModule.soulsDao,
            platformServer = platformServiceModule.platformServer,
            isDeadPlayerProvider = platformServiceModule.isDeadPlayerProvider,
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
