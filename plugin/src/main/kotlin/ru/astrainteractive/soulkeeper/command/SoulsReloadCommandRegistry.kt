package ru.astrainteractive.soulkeeper.command

import org.bukkit.command.CommandSender
import ru.astrainteractive.astralibs.command.api.context.BukkitCommandContext
import ru.astrainteractive.astralibs.command.api.context.BukkitCommandContextExt.requirePermission
import ru.astrainteractive.astralibs.command.api.executor.CommandExecutor
import ru.astrainteractive.astralibs.command.api.parser.CommandParser
import ru.astrainteractive.astralibs.command.api.util.PluginExt.registerCommand
import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.klibs.kstorage.api.Krate
import ru.astrainteractive.soulkeeper.core.plugin.LifecyclePlugin
import ru.astrainteractive.soulkeeper.core.plugin.PluginPermission
import ru.astrainteractive.soulkeeper.core.plugin.PluginTranslation
import ru.astrainteractive.soulkeeper.core.util.getValue

internal class SoulsReloadCommandRegistry(
    private val plugin: LifecyclePlugin,
    translationKrate: Krate<PluginTranslation>,
    kyoriKrate: Krate<KyoriComponentSerializer>
) {
    private val kyori by kyoriKrate
    private val translation by translationKrate

    private inner class CommandParserImpl : CommandParser<CommandSender, BukkitCommandContext> {
        override fun parse(commandContext: BukkitCommandContext): CommandSender {
            return when (commandContext.args.getOrNull(0)) {
                else -> {
                    commandContext.requirePermission(PluginPermission.Reload)
                    commandContext.sender
                }
            }
        }
    }

    private inner class CommandExecutorImpl : CommandExecutor<CommandSender> {

        override fun execute(input: CommandSender) {
            with(kyori) {
                input.sendMessage(translation.general.reload.component)
                plugin.onReload()
                input.sendMessage(translation.general.reloadComplete.component)
            }
        }
    }

    fun register() {
        plugin.registerCommand(
            alias = "skreload",
            commandParser = CommandParserImpl(),
            commandExecutor = CommandExecutorImpl(),
            errorHandler = { context, throwable ->
                throwable.printStackTrace()
            }
        )
    }
}
