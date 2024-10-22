package ru.astrainteractive.aspekt.module.souls.di

import ru.astrainteractive.aspekt.di.CoreModule
import ru.astrainteractive.aspekt.module.souls.database.SoulsDbModule
import ru.astrainteractive.aspekt.module.souls.event.SoulEvents
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

        private val particleWorker = ParticleWorker(
            soulsDao = soulsDbModule.soulsDao,
            dispatchers = coreModule.dispatchers,
            soulsConfigKrate = soulsConfigModule.soulsConfigKrate
        )

        private val pickUpWorker = PickUpWorker(
            soulsDao = soulsDbModule.soulsDao,
            dispatchers = coreModule.dispatchers,
            soulsConfigKrate = soulsConfigModule.soulsConfigKrate,
            onPickUp = {
                particleWorker.onDisable()
                particleWorker.onEnable()
            }
        )

        private val event = SoulEvents(
            soulsDao = soulsDbModule.soulsDao,
            soulsConfigKrate = soulsConfigModule.soulsConfigKrate
        )

        override val lifecycle: Lifecycle = Lifecycle.Lambda(
            onEnable = {
                soulsConfigModule.lifecycle.onEnable()
                soulsDbModule.lifecycle.onEnable()
                event.onEnable(coreModule.plugin)
                particleWorker.onEnable()
                pickUpWorker.onEnable()
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
            }
        )
    }
}
