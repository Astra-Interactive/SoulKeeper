package ru.astrainteractive.aspekt

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import ru.astrainteractive.aspekt.di.impl.RootModuleImpl
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.logging.JUtiltLogger
import ru.astrainteractive.astralibs.logging.Logger

/**
 * Initial class for your plugin
 */
class AspeKt : JavaPlugin(), Logger by JUtiltLogger("AspeKt") {
    private val rootModule = RootModuleImpl(this)
    private val lifecycles: List<Lifecycle>
        get() = listOfNotNull(
            rootModule.economyModule.lifecycle,
            rootModule.autoBroadcastModule.lifecycle,
            rootModule.sitModule.lifecycle,
            rootModule.commandManagerModule,
            rootModule.coreModule.lifecycle,
            rootModule.menuModule.lifecycle,
            rootModule.discordLinkModule.lifecycle,
            rootModule.adminPrivateModule.lifecycle,
            rootModule.townyDiscordModule.lifecycle,
            rootModule.moneyDropModule.lifecycle,
            rootModule.autoCropModule.lifecycle,
            rootModule.newBeeModule.lifecycle,
            rootModule.antiSwearModule.lifecycle,
            rootModule.moneyAdvancementModule.lifecycle,
            rootModule.chatGameModule.lifecycle,
            rootModule.eventsModule.lifecycle,
            rootModule.soulsModule.lifecycle
        )

    /**
     * This method called when server starts or PlugMan load plugin.
     */
    override fun onEnable() {
        lifecycles.forEach(Lifecycle::onEnable)
    }

    /**
     * This method called when server is shutting down or when PlugMan disable plugin.
     */
    override fun onDisable() {
        lifecycles.forEach(Lifecycle::onDisable)
        HandlerList.unregisterAll(this)
        Bukkit.getOnlinePlayers().forEach(Player::closeInventory)
    }

    /**
     * As it says, function for plugin reload
     */
    fun reloadPlugin() {
        lifecycles.forEach(Lifecycle::onReload)
        Bukkit.getOnlinePlayers().forEach(Player::closeInventory)
    }
}
