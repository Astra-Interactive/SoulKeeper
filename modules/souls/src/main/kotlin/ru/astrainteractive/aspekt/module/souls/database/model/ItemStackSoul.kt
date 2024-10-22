package ru.astrainteractive.aspekt.module.souls.database.model

import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.util.UUID

internal data class ItemStackSoul(
    val exp: Int,
    val items: List<ItemStack>,
    override val ownerUUID: UUID,
    override val ownerLastName: String,
    override val createdAt: Instant,
    override val isFree: Boolean,
    override val location: Location,
) : Soul {
    override val hasItems: Boolean = items.isNotEmpty()
    override val hasXp: Boolean = exp > 0
}