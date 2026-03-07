package ru.astrainteractive.soulkeeper.command.soulkrate

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.StringFormat
import ru.astrainteractive.astralibs.command.api.registrar.PaperCommandRegistrarContext
import ru.astrainteractive.astralibs.command.api.util.argument
import ru.astrainteractive.astralibs.command.api.util.command
import ru.astrainteractive.astralibs.command.api.util.requireArgument
import ru.astrainteractive.astralibs.command.api.util.requirePermission
import ru.astrainteractive.astralibs.command.api.util.requirePlayer
import ru.astrainteractive.astralibs.command.api.util.runs
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.plugin.PluginPermission
import ru.astrainteractive.soulkeeper.core.serialization.ItemStackSerializer
import ru.astrainteractive.soulkeeper.module.souls.database.model.StringFormatObject
import ru.astrainteractive.soulkeeper.module.souls.krate.PlayerSoulKrate
import java.io.File
import java.time.Instant
import java.util.UUID

internal class SoulKrateCommandRegistrar(
    private val registrarContext: PaperCommandRegistrarContext,
    private val stringFormat: StringFormat,
    private val dataFolder: File,
    private val ioScope: CoroutineScope
) : Logger by JUtiltLogger("SoulKrateCommandRegistrar") {
    private fun createNode(): LiteralCommandNode<CommandSourceStack> {
        return command("soulkrate") {
            argument("uuid", StringArgumentType.string()) { uuidArg ->
                argument("instant", LongArgumentType.longArg()) { instantArg ->
                    argument("index", IntegerArgumentType.integer()) { indexArg ->
                        runs { ctx ->
                            ctx.requirePermission(PluginPermission.LoadSouls)
                            val player = ctx.requirePlayer()
                            val instant = ctx.requireArgument(instantArg).let(Instant::ofEpochSecond)
                            val index = ctx.requireArgument(indexArg)
                            val uuid = ctx.requireArgument(uuidArg).let(UUID::fromString)
                            ioScope.launch {
                                val soul = PlayerSoulKrate(
                                    stringFormat = stringFormat,
                                    dataFolder = dataFolder,
                                    createdAt = instant,
                                    ownerUUID = uuid,
                                    readIndex = index
                                ).getValue()
                                val items = soul?.items
                                    .orEmpty()
                                    .map(StringFormatObject::raw)
                                    .map(ItemStackSerializer::decodeFromString)
                                    .mapNotNull { itemStackResult ->
                                        itemStackResult
                                            .onFailure { error(it) { "Failed to deserialize item stack" } }
                                            .getOrNull()
                                    }
                                player.inventory.addItem(*items.toTypedArray())
                            }
                        }
                    }
                }
            }
        }.build()
    }

    fun register() {
        registrarContext.registerWhenReady(createNode())
    }
}
