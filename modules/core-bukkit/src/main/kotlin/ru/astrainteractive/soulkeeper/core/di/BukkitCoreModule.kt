package ru.astrainteractive.soulkeeper.core.di

import org.bstats.bukkit.Metrics
import ru.astrainteractive.astralibs.event.EventListener
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.lifecycle.LifecyclePlugin
import ru.astrainteractive.astralibs.menu.event.DefaultInventoryClickEvent

class BukkitCoreModule(val plugin: LifecyclePlugin) {

    val eventListener = EventListener.Default()
    val inventoryClickEventListener = DefaultInventoryClickEvent()

    val lifecycle: Lifecycle = Lifecycle.Lambda(
        onEnable = {
            inventoryClickEventListener.onEnable(plugin)
            eventListener.onEnable(plugin)
            Metrics(plugin, 23714)
        },
        onReload = {
        },
        onDisable = {
            inventoryClickEventListener.onDisable()
            eventListener.onDisable()
        }
    )
}
