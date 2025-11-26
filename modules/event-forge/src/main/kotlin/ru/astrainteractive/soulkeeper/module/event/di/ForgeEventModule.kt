package ru.astrainteractive.soulkeeper.module.event.di

import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.module.event.event.ForgeSoulEvents
import ru.astrainteractive.soulkeeper.module.souls.di.SoulsDaoModule
import ru.astrainteractive.soulkeeper.module.souls.platform.EffectEmitter

class ForgeEventModule(
    coreModule: CoreModule,
    soulsDaoModule: SoulsDaoModule,
    effectEmitter: EffectEmitter
) {
    @Suppress("UnusedPrivateProperty")
    private val event = ForgeSoulEvents(
        soulsDao = soulsDaoModule.soulsDao,
        soulsConfigKrate = coreModule.soulsConfigKrate,
        effectEmitter = effectEmitter,
        mainScope = coreModule.mainScope,
        ioScope = coreModule.ioScope,
        dispatchers = coreModule.dispatchers
    )

    val lifecycle = Lifecycle.Lambda(
        onEnable = {
        },
        onDisable = {
        }
    )
}
