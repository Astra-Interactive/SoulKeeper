package ru.astrainteractive.soulkeeper

import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import ru.astrainteractive.soulkeeper.di.RootModule
import javax.annotation.ParametersAreNonnullByDefault

@Mod("soulkeeper")
@ParametersAreNonnullByDefault
class ForgeEntryPoint {
    private val rootModule = RootModule(this)

   init {
       println(NeoForge.EVENT_BUS)
   }
}
