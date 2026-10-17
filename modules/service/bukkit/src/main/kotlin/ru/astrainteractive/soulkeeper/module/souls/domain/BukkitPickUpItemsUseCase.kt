package ru.astrainteractive.soulkeeper.module.souls.domain

import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import ru.astrainteractive.astralibs.server.player.OnlineKPlayer
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.platform.EffectEmitter
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.serialization.ItemStackSerializer
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemDatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.StringFormatObject
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpItemsUseCase.Output

internal class BukkitPickUpItemsUseCase(
    private val collectItemSoundProvider: () -> SoulsConfig.Sounds.SoundConfig,
    private val soulsDao: SoulsDao,
    private val dispatchers: KotlinDispatchers,
    private val effectEmitter: EffectEmitter
) : PickUpItemsUseCase,
    Logger by JUtiltLogger("SoulKeeper-PickUpItemsUseCase") {

    override suspend fun invoke(player: OnlineKPlayer, soul: ItemDatabaseSoul): Output {
        if (soul.items.isEmpty()) {
            info { "Soul ${soul.id} has no items to collect" }
            return Output.NoItemsPresent
        }
        val bukkitPlayer = Bukkit.getPlayer(player.uuid)
        if (bukkitPlayer == null) {
            info { "Player ${player.uuid} is not online" }
            return Output.SomeItemsRemain
        }

        val items = soul.items
            .map(StringFormatObject::raw)
            .map(ItemStackSerializer::decodeFromString)
            .mapNotNull { itemStackResult ->
                itemStackResult
                    .onFailure { error(it) { "Failed to deserialize item stack" } }
                    .getOrNull()
            }
        val requestedAmount = items.sumOf(ItemStack::getAmount)
        val notAddedItems = withContext(dispatchers.Main) {
            bukkitPlayer.inventory
                .addItem(*items.toTypedArray())
                .values
                .toList()
        }
        if (notAddedItems.sumOf(ItemStack::getAmount) < requestedAmount) {
            withContext(dispatchers.Main) {
                effectEmitter.playSoundForPlayer(
                    location = soul.location,
                    player = player,
                    sound = collectItemSoundProvider.invoke()
                )
            }
        }

        soulsDao.updateSoul(
            soul.copy(
                items = notAddedItems
                    .map(ItemStackSerializer::encodeToString)
                    .map(::StringFormatObject)
            )
        )
        return when {
            notAddedItems.isEmpty() -> Output.ItemsCollected
            else -> Output.SomeItemsRemain
        }
    }
}
