package ru.astrainteractive.soulkeeper.command.soul.util

import ru.astrainteractive.astralibs.command.api.brigadier.sender.ConsoleKCommandSender
import ru.astrainteractive.astralibs.command.api.brigadier.sender.KCommandSender
import ru.astrainteractive.astralibs.command.api.brigadier.sender.KPlayerKCommandSender
import ru.astrainteractive.astralibs.server.permission.Permission
import ru.astrainteractive.klibs.mikro.core.util.tryCast
import ru.astrainteractive.soulkeeper.core.plugin.model.PluginPermission
import ru.astrainteractive.soulkeeper.data.dao.soul.model.DatabaseSoul

private fun KCommandSender.hasPermission(permission: Permission): Boolean {
    return when (val sender = this) {
        is ConsoleKCommandSender -> sender.hasPermission(permission)
        is KPlayerKCommandSender -> sender.hasPermission(permission)
    }
}

private fun KCommandSender.isSoulOwner(soul: DatabaseSoul): Boolean {
    return tryCast<KPlayerKCommandSender>()?.instance?.uuid == soul.ownerUUID
}

internal fun KCommandSender.canFreeSoul(soul: DatabaseSoul): Boolean {
    return hasPermission(PluginPermission.FreeAllSouls) || isSoulOwner(soul)
}

internal fun KPlayerKCommandSender.canTeleportToSoul(): Boolean {
    return hasPermission(PluginPermission.TeleportToSouls)
}
