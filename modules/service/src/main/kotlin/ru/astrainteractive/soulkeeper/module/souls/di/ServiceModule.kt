package ru.astrainteractive.soulkeeper.module.souls.di

import kotlinx.coroutines.flow.flowOf
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.server.MinecraftNativeBridge
import ru.astrainteractive.astralibs.server.PlatformServer
import ru.astrainteractive.astralibs.service.TickFlowService
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.souls.domain.GetNearestSoulUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpExpUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpSoulUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.armorstand.ShowArmorStandUseCase
import ru.astrainteractive.soulkeeper.module.souls.renderer.ArmorStandRenderer
import ru.astrainteractive.soulkeeper.module.souls.renderer.SoulParticleRenderer
import ru.astrainteractive.soulkeeper.module.souls.renderer.SoulSoundRenderer
import ru.astrainteractive.soulkeeper.module.souls.server.EffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.server.IsDeadPlayerProvider
import ru.astrainteractive.soulkeeper.module.souls.server.event.EventProvider
import ru.astrainteractive.soulkeeper.module.souls.service.DeleteSoulWorker
import ru.astrainteractive.soulkeeper.module.souls.service.FreeSoulWorker
import ru.astrainteractive.soulkeeper.module.souls.service.PickUpWorker
import ru.astrainteractive.soulkeeper.module.souls.service.SoulCallWorker
import kotlin.time.Duration.Companion.seconds

class ServiceModule(
    coreModule: CoreModule,
    soulsDaoModule: SoulsDaoModule,
    showArmorStandUseCase: ShowArmorStandUseCase,
    pickUpExpUseCase: PickUpExpUseCase,
    pickUpItemsUseCase: PickUpItemsUseCase,
    isDeadPlayerProvider: IsDeadPlayerProvider,
    platformServer: PlatformServer,
    effectEmitter: EffectEmitter,
    minecraftNativeBridge: MinecraftNativeBridge,
    eventProvider: EventProvider

) {

    private val armorStandRenderer = ArmorStandRenderer(
        soulsConfigKrate = coreModule.soulsConfigKrate,
        showArmorStandUseCase = showArmorStandUseCase,
        platformServer = platformServer
    )
    private val soulParticleRenderer = SoulParticleRenderer(
        soulsConfigKrate = coreModule.soulsConfigKrate,
        dispatchers = coreModule.dispatchers,
        effectEmitter = effectEmitter
    )
    private val soulSoundRenderer = SoulSoundRenderer(
        dispatchers = coreModule.dispatchers,
        soulsConfigKrate = coreModule.soulsConfigKrate,
        effectEmitter = effectEmitter
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
        config = coreModule.soulsConfigKrate.cachedValue,
        soulParticleRenderer = soulParticleRenderer,
        soulSoundRenderer = soulSoundRenderer,
        soulArmorStandRenderer = armorStandRenderer,
        eventProvider = eventProvider,
        minecraftNativeBridge = minecraftNativeBridge
    )

    private val pickUpSoulService = TickFlowService(
        coroutineContext = coreModule.dispatchers.IO,
        delay = flowOf(3.seconds),
        executor = PickUpWorker(
            pickUpSoulUseCase = PickUpSoulUseCase(
                dispatchers = coreModule.dispatchers,
                pickUpExpUseCase = pickUpExpUseCase,
                pickUpItemsUseCase = pickUpItemsUseCase,
                soulsDao = soulsDaoModule.soulsDao,
                soulGoneParticleProvider = { coreModule.soulsConfigKrate.cachedValue.particles.soulGone },
                soulDisappearSoundProvider = { coreModule.soulsConfigKrate.cachedValue.sounds.soulDisappear },
                soulContentLeftParticleProvider = {
                    coreModule.soulsConfigKrate.cachedValue.particles.soulContentLeft
                },
                soulContentLeftSoundProvider = {
                    coreModule.soulsConfigKrate.cachedValue.sounds.soulContentLeft
                },
                effectEmitter = effectEmitter
            ),
            getNearestSoulUseCase = GetNearestSoulUseCase(
                soulsDao = soulsDaoModule.soulsDao,
                minecraftNativeBridge = minecraftNativeBridge
            ),
            soulsDao = soulsDaoModule.soulsDao,
            platformServer = platformServer,
            isDeadPlayerProvider = isDeadPlayerProvider,
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
