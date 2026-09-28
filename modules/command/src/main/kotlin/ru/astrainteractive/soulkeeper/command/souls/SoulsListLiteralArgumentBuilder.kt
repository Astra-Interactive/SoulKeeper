package ru.astrainteractive.soulkeeper.command.souls

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import ru.astrainteractive.astralibs.command.api.brigadier.command.MultiplatformCommand
import ru.astrainteractive.soulkeeper.core.command.CommandExceptionHandler
import ru.astrainteractive.soulkeeper.core.plugin.PluginPermission

internal class SoulsListLiteralArgumentBuilder(
    private val multiplatformCommand: MultiplatformCommand,
    private val soulsCommandExecutor: SoulsCommandExecutor,
    private val commandExceptionHandler: CommandExceptionHandler,
) {
    fun create(): LiteralArgumentBuilder<*> {
        return with(multiplatformCommand) {
            command("souls") {
                literal("page") {
                    argument("page", IntegerArgumentType.integer(0)) { pageArg ->
                        runs(commandExceptionHandler::handle) { ctx ->
                            val page = ctx.requireArgument(pageArg)
                            SoulsCommand.Intent.List(
                                sender = ctx.getSender(),
                                page = page
                            ).run(soulsCommandExecutor::execute)
                        }
                    }
                }
                literal("free") {
                    argument("soul_id", LongArgumentType.longArg(0)) { idArg ->
                        runs(commandExceptionHandler::handle) { ctx ->
                            SoulsCommand.Intent.Free(
                                sender = ctx.getSender(),
                                soulId = ctx.requireArgument(idArg)
                            ).run(soulsCommandExecutor::execute)
                        }
                    }
                }
                literal("teleport") {
                    argument("soul_id", LongArgumentType.longArg(0)) { idArg ->
                        runs(commandExceptionHandler::handle) { ctx ->
                            ctx.requirePermission(PluginPermission.TeleportToSouls)
                            SoulsCommand.Intent.TeleportToSoul(
                                player = ctx.requirePlayer(),
                                soulId = ctx.requireArgument(idArg)
                            ).run(soulsCommandExecutor::execute)
                        }
                    }
                }
                runs(commandExceptionHandler::handle) { ctx ->
                    SoulsCommand.Intent.List(
                        sender = ctx.getSender(),
                        page = 0
                    ).run(soulsCommandExecutor::execute)
                }
            }
        }
    }
}
