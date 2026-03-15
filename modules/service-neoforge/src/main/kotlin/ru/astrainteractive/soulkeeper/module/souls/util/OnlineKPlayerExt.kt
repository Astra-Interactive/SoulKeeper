package ru.astrainteractive.soulkeeper.module.souls.util

import net.minecraft.world.item.ItemStack
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.astralibs.server.util.NeoForgeUtil
import ru.astrainteractive.astralibs.server.util.getOnlinePlayer

/**
 * @param items List of items to add to inventory
 * @return not fitted items into inventory
 */
internal fun OnlineKPlayer.addItems(
    items: List<ItemStack>,
    isDead: (OnlineKPlayer) -> Boolean
): List<ItemStack> {
    return items
        .mapNotNull { stack ->
            if (!isDead.invoke(this)) {
                val inventory = NeoForgeUtil.getOnlinePlayer(uuid)?.inventory
                inventory?.add(stack)
                inventory?.setChanged()
            }
            stack.copy()
        }
        .filterNot(ItemStack::isEmpty)
}
