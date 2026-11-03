package ru.astrainteractive.soulkeeper.command.soul.fake

import net.kyori.adventure.text.Component
import ru.astrainteractive.astralibs.command.api.brigadier.sender.ConsoleKCommandSender
import ru.astrainteractive.astralibs.server.permission.Permission
import java.util.Locale

internal class FakeConsoleKCommandSender(
    private val grantedPermissions: Set<Permission>
) : ConsoleKCommandSender {
    override val locale: Locale = Locale.ROOT

    private fun notUsed(): Nothing = error("The tests do not call this")

    override fun sendMessage(component: Component) = notUsed()

    override fun hasPermission(permission: Permission): Boolean = permission in grantedPermissions

    override fun maxPermissionSize(permission: Permission): Int = notUsed()

    override fun minPermissionSize(permission: Permission): Int = notUsed()

    override fun permissionSizes(permission: Permission): List<Int> = notUsed()

    override fun dispatchCommand(command: String) = notUsed()
}
