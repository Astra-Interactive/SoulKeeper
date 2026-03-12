package ru.astrainteractive.soulkeeper.module.souls.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.flowOf
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.service.ThrottleTickFlowService
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.di.qualifier.ServiceLifecycle
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpExpUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpItemsUseCase
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpSoulUseCase
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter
import ru.astrainteractive.soulkeeper.module.souls.platform.Experienced
import ru.astrainteractive.soulkeeper.module.souls.service.DeleteSoulWorker
import ru.astrainteractive.soulkeeper.module.souls.service.FreeSoulWorker
import ru.astrainteractive.soulkeeper.module.souls.service.PickUpWorker
import ru.astrainteractive.soulkeeper.module.souls.service.SoulCallWorker
import kotlin.time.Duration.Companion.seconds

object ServiceScope

@DependencyGraph(ServiceScope::class)
interface ServiceModule {

    @get:ServiceLifecycle
    val lifecycle: Lifecycle

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Includes coreModule: CoreModule,
            @Includes soulsDaoModule: SoulsDaoModule,
            @Includes platformServiceModule: PlatformServiceModule
        ): ServiceModule
    }

    @Provides
    fun provideSoulsConfig(
        soulsConfigKrate: CachedKrate<SoulsConfig>
    ): SoulsConfig = soulsConfigKrate.cachedValue

    @Provides
    fun providePickUpExpUseCase(
        soulsConfigKrate: CachedKrate<SoulsConfig>,
        soulsDao: SoulsDao,
        effectEmitter: EffectEmitter,
        dispatchers: KotlinDispatchers,
        experiencedFactory: Experienced.Factory<OnlineMinecraftPlayer>
    ): PickUpExpUseCase = PickUpExpUseCase(
        collectXpSoundProvider = { soulsConfigKrate.cachedValue.sounds.collectXp },
        soulsDao = soulsDao,
        effectEmitter = effectEmitter,
        dispatchers = dispatchers,
        experiencedFactory = experiencedFactory
    )

    @Provides
    fun providePickUpSoulUseCase(
        dispatchers: KotlinDispatchers,
        pickUpExpUseCase: PickUpExpUseCase,
        pickUpItemsUseCase: PickUpItemsUseCase,
        soulsDao: SoulsDao,
        soulsConfigKrate: CachedKrate<SoulsConfig>,
        effectEmitter: EffectEmitter
    ): PickUpSoulUseCase = PickUpSoulUseCase(
        dispatchers = dispatchers,
        pickUpExpUseCase = pickUpExpUseCase,
        pickUpItemsUseCase = pickUpItemsUseCase,
        soulsDao = soulsDao,
        soulDisappearSoundProvider = { soulsConfigKrate.cachedValue.sounds.soulDisappear },
        soulGoneParticleProvider = { soulsConfigKrate.cachedValue.particles.soulGone },
        soulContentLeftSoundProvider = { soulsConfigKrate.cachedValue.sounds.soulContentLeft },
        soulContentLeftParticleProvider = { soulsConfigKrate.cachedValue.particles.soulContentLeft },
        effectEmitter = effectEmitter
    )

    @SingleIn(ServiceScope::class)
    @ServiceLifecycle
    @Provides
    fun provideLifecycle(
        soulCallWorker: SoulCallWorker,
        deleteSoulWorker: DeleteSoulWorker,
        freeSoulWorker: FreeSoulWorker,
        pickUpWorker: PickUpWorker,
        dispatchers: KotlinDispatchers
    ): Lifecycle {
        val deleteSoulService = ThrottleTickFlowService(
            coroutineContext = SupervisorJob() + dispatchers.IO,
            delay = flowOf(60.seconds),
            executor = deleteSoulWorker
        )
        val freeSoulService = ThrottleTickFlowService(
            coroutineContext = SupervisorJob() + dispatchers.IO,
            delay = flowOf(60.seconds),
            executor = freeSoulWorker
        )
        val pickUpSoulService = ThrottleTickFlowService(
            coroutineContext = SupervisorJob() + dispatchers.IO,
            delay = flowOf(3.seconds),
            executor = pickUpWorker
        )
        return Lifecycle.Lambda(
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
}
