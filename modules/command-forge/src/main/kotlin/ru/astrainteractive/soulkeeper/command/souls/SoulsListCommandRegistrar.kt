package ru.astrainteractive.soulkeeper.command.souls

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.commands.CommandSourceStack
import ru.astrainteractive.astralibs.command.registrar.ForgeCommandRegistrarContext
import ru.astrainteractive.astralibs.command.util.argument
import ru.astrainteractive.astralibs.command.util.command
import ru.astrainteractive.astralibs.command.util.literal
import ru.astrainteractive.astralibs.command.util.requireArgument
import ru.astrainteractive.astralibs.command.util.runs
import ru.astrainteractive.astralibs.kyori.KyoriComponentSerializer
import ru.astrainteractive.astralibs.kyori.unwrap
import ru.astrainteractive.klibs.kstorage.api.CachedKrate

internal class SoulsListCommandRegistrar(
    kyoriKrate: CachedKrate<KyoriComponentSerializer>,
    private val registrarContext: ForgeCommandRegistrarContext,
    private val soulsCommandExecutor: SoulsCommandExecutor
) : KyoriComponentSerializer by kyoriKrate.unwrap() {
    private fun createNode(): LiteralArgumentBuilder<CommandSourceStack> {
        return command("souls") {
            literal("page") {
                argument("page", IntegerArgumentType.integer(0)) { pageArg ->
                    runs { ctx ->
                        val page = ctx.requireArgument(pageArg)
                        SoulsCommand.Intent.List(
                            sender = ctx.source,
                            page = page
                        ).run(soulsCommandExecutor::execute)
                    }
                }
            }
            literal("free") {
                argument("soul_id", LongArgumentType.longArg(0)) { idArg ->
                    runs { ctx ->
                        SoulsCommand.Intent.Free(
                            sender = ctx.source,
                            soulId = ctx.requireArgument(idArg)
                        ).run(soulsCommandExecutor::execute)
                    }
                }
            }
            literal("teleport") {
                argument("soul_id", LongArgumentType.longArg(0)) { idArg ->
                    runs { ctx ->
                        SoulsCommand.Intent.TeleportToSoul(
                            sender = ctx.source,
                            soulId = ctx.requireArgument(idArg)
                        ).run(soulsCommandExecutor::execute)
                    }
                }
            }
            runs { ctx ->
                SoulsCommand.Intent.List(
                    sender = ctx.source,
                    page = 0
                ).run(soulsCommandExecutor::execute)
            }
        }
    }

    fun register() {
        registrarContext.registerWhenReady(createNode())
    }
}
