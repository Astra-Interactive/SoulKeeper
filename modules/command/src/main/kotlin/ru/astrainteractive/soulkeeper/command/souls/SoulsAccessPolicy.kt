package ru.astrainteractive.soulkeeper.command.souls

import ru.astrainteractive.astralibs.command.api.brigadier.sender.ConsoleKCommandSender
import ru.astrainteractive.astralibs.command.api.brigadier.sender.KCommandSender
import ru.astrainteractive.astralibs.command.api.brigadier.sender.KPlayerKCommandSender
import ru.astrainteractive.astralibs.server.permission.Permission
import ru.astrainteractive.klibs.mikro.core.util.tryCast
import ru.astrainteractive.soulkeeper.core.plugin.PluginPermission
import ru.astrainteractive.soulkeeper.module.souls.database.model.DatabaseSoul

internal class SoulsAccessPolicy {

    private fun KCommandSender.hasPermission(permission: Permission): Boolean {
        return when (val sender = this) {
            is ConsoleKCommandSender -> sender.hasPermission(permission)
            is KPlayerKCommandSender -> sender.hasPermission(permission)
        }
    }

    private fun KCommandSender.isSoulOwner(soul: DatabaseSoul): Boolean {
        return tryCast<KPlayerKCommandSender>()?.instance?.uuid == soul.ownerUUID
    }

    /**
     * Owners may free their own soul without any permission;
     * [PluginPermission.FreeAllSouls] is required to free someone else's soul.
     */
    fun canFreeSoul(sender: KCommandSender, soul: DatabaseSoul): Boolean {
        return sender.hasPermission(PluginPermission.FreeAllSouls) || sender.isSoulOwner(soul)
    }

    fun canTeleportToSoul(player: KPlayerKCommandSender): Boolean {
        return player.hasPermission(PluginPermission.TeleportToSouls)
    }
}
