package ru.astrainteractive.soulkeeper.core.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import org.bstats.bukkit.Metrics
import ru.astrainteractive.astralibs.event.EventListener
import ru.astrainteractive.astralibs.lifecycle.Lifecycle
import ru.astrainteractive.astralibs.lifecycle.LifecyclePlugin
import ru.astrainteractive.astralibs.menu.event.DefaultInventoryClickEvent
import ru.astrainteractive.soulkeeper.core.di.qualifier.BukkitCoreLifecycle

object BukkitCoreScope

@DependencyGraph(BukkitCoreScope::class)
interface BukkitCoreModule {

    val plugin: LifecyclePlugin

    val eventListener: EventListener

    val inventoryClickEventListener: DefaultInventoryClickEvent

    @get:BukkitCoreLifecycle
    val lifecycle: Lifecycle

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides plugin: LifecyclePlugin): BukkitCoreModule
    }

    @SingleIn(BukkitCoreScope::class)
    @Provides
    fun provideEventListener(): EventListener = EventListener.Default()

    @SingleIn(BukkitCoreScope::class)
    @Provides
    fun provideInventoryClickEventListener(): DefaultInventoryClickEvent = DefaultInventoryClickEvent()

    @SingleIn(BukkitCoreScope::class)
    @BukkitCoreLifecycle
    @Provides
    fun provideLifecycle(
        plugin: LifecyclePlugin,
        inventoryClickEventListener: DefaultInventoryClickEvent,
        eventListener: EventListener
    ): Lifecycle = Lifecycle.Lambda(
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
