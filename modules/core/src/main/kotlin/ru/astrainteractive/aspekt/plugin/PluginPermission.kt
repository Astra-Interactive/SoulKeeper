package ru.astrainteractive.aspekt.plugin

import ru.astrainteractive.astralibs.permission.Permission

sealed class PluginPermission(override val value: String) : Permission {
    data object Reload : PluginPermission("aspekt.reload")
    data object TellChat : PluginPermission("aspekt.tellchat")
    data object MaxOnline : PluginPermission("aspekt.maxonline")
    data object AtemFrame : PluginPermission("aspekt.atemframe")
    data object Entities : PluginPermission("aspekt.entities")
    data object AdminClaim : PluginPermission("aspekt.admin_claim")
    data object ForcePlayerSwear : PluginPermission("aspekt.set_swear.admin")
    data object ViewAllSouls : PluginPermission("aspekt.souls.all")
    data object FreeAllSouls : PluginPermission("aspekt.souls.free.all")
    data object TeleportToSouls : PluginPermission("aspekt.souls.teleport")
    class CustomPermission(value: String) : PluginPermission(value)
    sealed class Economy(value: String) : PluginPermission(value) {
        data object SetBalance : Economy("aspekt.economy.set")
    }
}
