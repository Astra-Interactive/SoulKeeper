package ru.astrainteractive.soulkeeper

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.event.server.ServerStoppingEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.fml.common.Mod
import ru.astrainteractive.astralibs.event.flowEvent
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.server.util.ForgeUtil
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.di.RootModule
import javax.annotation.ParametersAreNonnullByDefault

@Mod("soulkeeper")
@ParametersAreNonnullByDefault
class ForgeEntryPoint :
    Logger by JUtiltLogger("SoulKeeper-ForgeEntryPoint"),
    Lifecycle {
    private val rootModule by lazy { RootModule() }

    override fun onEnable() {
        info { "#onEnable" }
        rootModule.lifecycle.onEnable()
    }

    override fun onDisable() {
        info { "#onDisable" }
        rootModule.lifecycle.onDisable()
    }

    override fun onReload() {
        info { "#onReload" }
        rootModule.lifecycle.onReload()
    }

    val serverStartedEvent = flowEvent<ServerStartedEvent>(EventPriority.HIGHEST)
        .onEach {
            info { "#serverStartedEvent" }
            withContext(rootModule.coreModule.dispatchers.Main) {
                onEnable()
            }
        }.launchIn(rootModule.coreModule.ioScope)

    val serverStoppingEvent = flowEvent<ServerStoppingEvent>(EventPriority.HIGHEST)
        .onEach {
            info { "#serverStoppingEvent" }
            withContext(rootModule.coreModule.dispatchers.Main) {
                onDisable()
            }
        }.launchIn(rootModule.coreModule.ioScope)

    init {
        ForgeUtil.bootstrap()
    }
}
