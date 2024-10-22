package ru.astrainteractive.soulkeeper.module.souls.di

import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.klibs.kstorage.api.Krate
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.core.di.factory.ConfigKrateFactory
import ru.astrainteractive.soulkeeper.module.souls.model.SoulsConfig
import java.io.File

internal interface SoulsConfigModule {
    val lifecycle: Lifecycle
    val soulsConfigKrate: Krate<SoulsConfig>

    class Default(
        coreModule: CoreModule,
        dataFolder: File
    ) : SoulsConfigModule {
        override val soulsConfigKrate = ConfigKrateFactory.create<SoulsConfig>(
            fileNameWithoutExtension = "souls_config",
            stringFormat = coreModule.yamlFormat,
            dataFolder = dataFolder,
            factory = ::SoulsConfig
        )

        override val lifecycle: Lifecycle = Lifecycle.Lambda(
            onReload = {
                soulsConfigKrate.loadAndGet()
            }
        )
    }
}
