package ru.astrainteractive.soulkeeper.event.forge.di

import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.soulkeeper.core.di.CoreModule
import ru.astrainteractive.soulkeeper.core.platform.api.EffectEmitter
import ru.astrainteractive.soulkeeper.data.dao.soul.api.SoulRepository
import ru.astrainteractive.soulkeeper.event.forge.event.ForgeSoulEvents

class ForgeEventModule(
    coreModule: CoreModule,
    soulRepository: SoulRepository,
    effectEmitter: EffectEmitter
) {
    @Suppress("UnusedPrivateProperty")
    private val event = ForgeSoulEvents(
        soulRepository = soulRepository,
        clock = coreModule.clock,
        soulsConfigKrate = coreModule.soulsConfigKrate,
        effectEmitter = effectEmitter,
        mainScope = coreModule.mainScope,
        dispatchers = coreModule.dispatchers,
        dataFolder = coreModule.dataFolder,
        stringFormat = coreModule.yamlFormat,
        ioScope = coreModule.ioScope
    )

    val lifecycle = Lifecycle.Lambda(
        onEnable = {
        },
        onDisable = {
        }
    )
}
