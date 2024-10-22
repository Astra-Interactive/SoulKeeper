package ru.astrainteractive.aspekt.module.souls.di

import ru.astrainteractive.aspekt.di.CoreModule
import ru.astrainteractive.aspekt.module.souls.command.SoulsCommandRegistry
import ru.astrainteractive.aspekt.module.souls.database.di.SoulsDbModule
import ru.astrainteractive.aspekt.module.souls.domain.GetNearestSoulUseCase
import ru.astrainteractive.aspekt.module.souls.domain.PickUpExpUseCase
import ru.astrainteractive.aspekt.module.souls.domain.PickUpItemsUseCase
import ru.astrainteractive.aspekt.module.souls.domain.PickUpSoulUseCase
import ru.astrainteractive.aspekt.module.souls.domain.di.factory.ShowArmorStandUseCaseFactory
import ru.astrainteractive.aspekt.module.souls.event.SoulEvents
import ru.astrainteractive.aspekt.module.souls.worker.DeleteSoulWorker
import ru.astrainteractive.aspekt.module.souls.worker.FreeSoulWorker
import ru.astrainteractive.aspekt.module.souls.worker.ParticleWorker
import ru.astrainteractive.aspekt.module.souls.worker.PickUpWorker
import ru.astrainteractive.astralibs.lifecycle.Lifecycle

interface SoulsModule {
    val lifecycle: Lifecycle

    class Default(coreModule: CoreModule) : SoulsModule {
        private val dataFolder = coreModule.plugin.dataFolder.resolve("souls")

        private val soulsDbModule = SoulsDbModule.Default(
            dataFolder = dataFolder,
            scope = coreModule.scope
        )

        private val soulsConfigModule = SoulsConfigModule.Default(
            coreModule = coreModule,
            dataFolder = dataFolder
        )

        private val deleteSoulWorker = DeleteSoulWorker(
            soulsDao = soulsDbModule.soulsDao,
            configKrate = soulsConfigModule.soulsConfigKrate
        )

        private val freeSoulWorker = FreeSoulWorker(
            soulsDao = soulsDbModule.soulsDao,
            configKrate = soulsConfigModule.soulsConfigKrate
        )

        private val particleWorker = ParticleWorker(
            soulsDao = soulsDbModule.soulsDao,
            dispatchers = coreModule.dispatchers,
            soulsConfigKrate = soulsConfigModule.soulsConfigKrate,
            showArmorStandUseCase = ShowArmorStandUseCaseFactory(coreModule).create()
        )

        private val pickUpWorker = PickUpWorker(
            pickUpSoulUseCase = PickUpSoulUseCase(
                dispatchers = coreModule.dispatchers,
                pickUpExpUseCase = PickUpExpUseCase(
                    collectXpSoundProvider = { soulsConfigModule.soulsConfigKrate.cachedValue.sounds.collectXp },
                    soulsDao = soulsDbModule.soulsDao
                ),
                pickUpItemsUseCase = PickUpItemsUseCase(
                    collectItemSoundProvider = { soulsConfigModule.soulsConfigKrate.cachedValue.sounds.collectItem },
                    soulsDao = soulsDbModule.soulsDao
                ),
                soulsDao = soulsDbModule.soulsDao,
                soulGoneParticleProvider = { soulsConfigModule.soulsConfigKrate.cachedValue.particles.soulGone },
                soulDisappearSoundProvider = { soulsConfigModule.soulsConfigKrate.cachedValue.sounds.soulDisappear },
                soulContentLeftParticleProvider = {
                    soulsConfigModule.soulsConfigKrate.cachedValue.particles.soulContentLeft
                },
                soulContentLeftSoundProvider = {
                    soulsConfigModule.soulsConfigKrate.cachedValue.sounds.soulContentLeft
                }
            ),
            getNearestSoulUseCase = GetNearestSoulUseCase(
                soulsDao = soulsDbModule.soulsDao
            ),
            onPickUp = {
                particleWorker.onReload()
            }
        )

        private val event = SoulEvents(
            soulsDao = soulsDbModule.soulsDao,
            soulsConfigKrate = soulsConfigModule.soulsConfigKrate
        )
        private val soulsCommandRegistry = SoulsCommandRegistry(
            plugin = coreModule.plugin,
            scope = coreModule.scope,
            soulsDao = soulsDbModule.soulsDao,
            kyoriKrate = coreModule.kyoriComponentSerializer,
            translationKrate = coreModule.translation
        )

        override val lifecycle: Lifecycle = Lifecycle.Lambda(
            onEnable = {
                soulsConfigModule.lifecycle.onEnable()
                soulsDbModule.lifecycle.onEnable()
                event.onEnable(coreModule.plugin)
                particleWorker.onEnable()
                pickUpWorker.onEnable()
                deleteSoulWorker.onEnable()
                freeSoulWorker.onEnable()
                soulsCommandRegistry.register()
            },
            onReload = {
                soulsConfigModule.lifecycle.onReload()
                soulsDbModule.lifecycle.onReload()
            },
            onDisable = {
                soulsConfigModule.lifecycle.onDisable()
                soulsDbModule.lifecycle.onDisable()
                event.onDisable()
                particleWorker.onDisable()
                pickUpWorker.onDisable()
                deleteSoulWorker.onDisable()
                freeSoulWorker.onDisable()
            }
        )
    }
}
