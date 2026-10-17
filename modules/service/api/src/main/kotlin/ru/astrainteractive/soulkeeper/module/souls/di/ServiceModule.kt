package ru.astrainteractive.soulkeeper.module.souls.di

import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.service.IntervalService
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.data.dao.soul.api.SoulRepository
import ru.astrainteractive.soulkeeper.module.souls.renderer.ArmorStandRenderer
import ru.astrainteractive.soulkeeper.module.souls.renderer.SoulParticleRenderer
import ru.astrainteractive.soulkeeper.module.souls.renderer.SoulSoundRenderer
import ru.astrainteractive.soulkeeper.module.souls.service.DeleteSoulWorker
import ru.astrainteractive.soulkeeper.module.souls.service.FreeSoulWorker
import ru.astrainteractive.soulkeeper.module.souls.service.PickUpWorker
import ru.astrainteractive.soulkeeper.module.souls.service.SoulCallWorker
import ru.astrainteractive.soulkeeper.module.souls.soul.usecase.GetNearestSoulUseCase
import ru.astrainteractive.soulkeeper.module.souls.soul.usecase.PickUpSoulUseCase
import kotlin.time.Duration.Companion.seconds

class ServiceModule(
    coreModule: CoreModule,
    soulRepository: SoulRepository,
    platformServiceModule: PlatformServiceModule

) {

    val playerInventory = platformServiceModule.playerInventory
    private val armorStandRenderer = ArmorStandRenderer(
        soulsConfigKrate = coreModule.soulsConfigKrate,
        showArmorStandUseCase = platformServiceModule.showArmorStandUseCase,
        platformServer = platformServiceModule.platformServer
    )
    private val soulParticleRenderer = SoulParticleRenderer(
        soulsConfigKrate = coreModule.soulsConfigKrate,
        dispatchers = coreModule.dispatchers,
        effectEmitter = coreModule.effectEmitter
    )
    private val soulSoundRenderer = SoulSoundRenderer(
        dispatchers = coreModule.dispatchers,
        soulsConfigKrate = coreModule.soulsConfigKrate,
        effectEmitter = coreModule.effectEmitter
    )

    private val deleteSoulService = IntervalService(
        interval = SOUL_CLEANUP_INTERVAL,
        scope = coreModule.ioScope,
        logger = JUtiltLogger("DeleteSoulService"),
        task = DeleteSoulWorker(
            soulRepository = soulRepository,
            configKrate = coreModule.soulsConfigKrate,
        )
    )

    private val freeSoulService = IntervalService(
        interval = SOUL_CLEANUP_INTERVAL,
        scope = coreModule.ioScope,
        logger = JUtiltLogger("FreeSoulService"),
        task = FreeSoulWorker(
            soulRepository = soulRepository,
            configKrate = coreModule.soulsConfigKrate,
        )
    )

    private val soulCallWorker = SoulCallWorker(
        soulRepository = soulRepository,
        config = coreModule.soulsConfigKrate.cachedValue,
        soulParticleRenderer = soulParticleRenderer,
        soulSoundRenderer = soulSoundRenderer,
        soulArmorStandRenderer = armorStandRenderer,
        eventProvider = platformServiceModule.eventProvider,
    )

    private val pickUpSoulService = IntervalService(
        interval = SOUL_PICK_UP_INTERVAL,
        scope = coreModule.ioScope,
        logger = JUtiltLogger("PickUpSoulService"),
        task = PickUpWorker(
            pickUpSoulUseCase = PickUpSoulUseCase(
                soulRepository = soulRepository,
                playerInventory = platformServiceModule.playerInventory,
                experiencedFactory = platformServiceModule.onlineMinecraftPlayerExperiencedFactory,
                effectEmitter = coreModule.effectEmitter,
                dispatchers = coreModule.dispatchers,
                soulsConfigKrate = coreModule.soulsConfigKrate
            ),
            getNearestSoulUseCase = GetNearestSoulUseCase(
                soulRepository = soulRepository,
            ),
            soulRepository = soulRepository,
            platformServer = platformServiceModule.platformServer,
            isDeadPlayerProvider = platformServiceModule.isDeadPlayerProvider,
        )
    )

    val lifecycle: Lifecycle = Lifecycle.Lambda(
        onEnable = {
            soulCallWorker.onEnable()
            pickUpSoulService.onEnable()
            deleteSoulService.onEnable()
            freeSoulService.onEnable()
        },
        onDisable = {
            soulCallWorker.onDisable()
            pickUpSoulService.onDisable()
            deleteSoulService.onDisable()
            freeSoulService.onDisable()
        }
    )

    companion object {
        private val SOUL_CLEANUP_INTERVAL = 60.seconds
        private val SOUL_PICK_UP_INTERVAL = 3.seconds
    }
}
