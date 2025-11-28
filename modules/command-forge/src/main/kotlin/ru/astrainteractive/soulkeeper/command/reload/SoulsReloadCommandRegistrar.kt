package ru.astrainteractive.soulkeeper.command.reload

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.commands.CommandSourceStack
import ru.astrainteractive.astralibs.command.registrar.ForgeCommandRegistrarContext
import ru.astrainteractive.astralibs.command.util.command
import ru.astrainteractive.astralibs.command.util.requirePermission
import ru.astrainteractive.astralibs.command.util.runs
import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.astralibs.kyori.unwrap
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.server.util.toNative
import ru.astrainteractive.klibs.kstorage.api.CachedKrate
import ru.astrainteractive.klibs.kstorage.util.getValue
import ru.astrainteractive.soulkeeper.core.plugin.PluginPermission
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation

internal class SoulsReloadCommandRegistrar(
    private val plugin: Lifecycle,
    private val registrarContext: ForgeCommandRegistrarContext,
    translationKrate: CachedKrate<PluginTranslation>,
    kyoriKrate: CachedKrate<KyoriComponentSerializer>
) : KyoriComponentSerializer by kyoriKrate.unwrap() {
    private val translation by translationKrate

    private fun createNode(): LiteralArgumentBuilder<CommandSourceStack> {
        return command("skreload") {
            runs { ctx ->
                ctx.requirePermission(PluginPermission.Reload)
                ctx.source.sendSystemMessage(translation.general.reload.component.toNative())
                plugin.onReload()
                ctx.source.sendSystemMessage(translation.general.reloadComplete.component.toNative())
            }
        }
    }

    fun register() {
        registrarContext.registerWhenReady(createNode())
    }
}
