package ru.astrainteractive.aspekt.module.souls.di

import ru.astrainteractive.aspekt.di.CoreModule
import ru.astrainteractive.aspekt.di.factory.ConfigKrateFactory
import ru.astrainteractive.aspekt.module.souls.model.SoulsConfig
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.klibs.kstorage.api.Krate
import java.io.File

internal interface SoulsConfigModule {
    val lifecycle: Lifecycle
    val soulsConfigKrate: Krate<SoulsConfig>

    class Default(
        coreModule: CoreModule,
        dataFolder: File
    ) : SoulsConfigModule {
        override val soulsConfigKrate = ConfigKrateFactory.create<SoulsConfig>(
            fileNameWithoutExtension = "db",
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
