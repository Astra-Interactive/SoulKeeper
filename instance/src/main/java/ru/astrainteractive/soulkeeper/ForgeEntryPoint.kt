package ru.astrainteractive.soulkeeper

import net.neoforged.fml.common.Mod
import ru.astrainteractive.astralibs.lifecycle.ForgeLifecycleServer
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.module.souls.platform.NeoForgeIsDeadPlayerProvider
import javax.annotation.ParametersAreNonnullByDefault

@Mod("soulkeeper")
@ParametersAreNonnullByDefault
class ForgeEntryPoint :
    ForgeLifecycleServer(),
    Logger by JUtiltLogger("SoulKeeper-ForgeEntryPoint"),
    Lifecycle {

    override fun onEnable() {
        NeoForgeIsDeadPlayerProvider.printSomeExample()
    }

    override fun onDisable() = Unit

    override fun onReload() = Unit
}
