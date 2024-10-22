package ru.astrainteractive.soulkeeper.di

import org.bukkit.plugin.java.JavaPlugin
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsModule

interface RootModule {
    val lifecycle: Lifecycle

    val coreModule: CoreModule
    val soulsModule: SoulsModule

    class RootModuleImpl(plugin: JavaPlugin) : RootModule {
        override val coreModule: CoreModule by lazy {
            CoreModule.Default(plugin)
        }

        override val soulsModule: SoulsModule by lazy {
            SoulsModule.Default(coreModule)
        }

        private val lifecycles: List<Lifecycle>
            get() = listOfNotNull(
                coreModule.lifecycle,
                soulsModule.lifecycle
            )

        override val lifecycle = Lifecycle.Lambda(
            onEnable = { lifecycles.forEach(Lifecycle::onEnable) },
            onDisable = { lifecycles.forEach(Lifecycle::onDisable) },
            onReload = { lifecycles.forEach(Lifecycle::onReload) }
        )
    }
}
