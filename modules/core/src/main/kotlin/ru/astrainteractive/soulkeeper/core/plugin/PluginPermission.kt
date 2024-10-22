package ru.astrainteractive.soulkeeper.core.plugin

import ru.astrainteractive.astralibs.permission.Permission

sealed class PluginPermission(override val value: String) : Permission {
    data object Reload : PluginPermission("soulkeeper.reload")
    data object ViewAllSouls : PluginPermission("soulkeeper.all")
    data object FreeAllSouls : PluginPermission("soulkeeper.free.all")
    data object TeleportToSouls : PluginPermission("soulkeeper.teleport")
}
