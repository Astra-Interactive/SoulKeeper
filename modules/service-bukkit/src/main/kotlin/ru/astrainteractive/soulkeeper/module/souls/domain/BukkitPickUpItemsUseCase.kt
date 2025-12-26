package ru.astrainteractive.soulkeeper.module.souls.domain

import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import ru.astrainteractive.astralibs.server.player.OnlineMinecraftPlayer
import ru.astrainteractive.klibs.mikro.core.dispatchers.KotlinDispatchers
import ru.astrainteractive.klibs.mikro.core.logging.JUtiltLogger
import ru.astrainteractive.klibs.mikro.core.logging.Logger
import ru.astrainteractive.soulkeeper.core.plugin.SoulsConfig
import ru.astrainteractive.soulkeeper.core.serialization.ItemStackSerializer
import ru.astrainteractive.soulkeeper.core.util.playSoundForPlayer
import ru.astrainteractive.soulkeeper.core.util.toBukkitLocation
import ru.astrainteractive.soulkeeper.module.souls.dao.SoulsDao
import ru.astrainteractive.soulkeeper.module.souls.database.model.ItemDatabaseSoul
import ru.astrainteractive.soulkeeper.module.souls.database.model.StringFormatObject
import ru.astrainteractive.soulkeeper.module.souls.domain.PickUpItemsUseCase.Output

internal class BukkitPickUpItemsUseCase(
    private val collectItemSoundProvider: () -> SoulsConfig.Sounds.SoundConfig,
    private val soulsDao: SoulsDao,
    private val dispatchers: KotlinDispatchers
) : PickUpItemsUseCase,
    Logger by JUtiltLogger("SoulKeeper-PickUpItemsUseCase") {

    override suspend fun invoke(player: OnlineMinecraftPlayer, soul: ItemDatabaseSoul): Output {
        if (soul.items.isEmpty()) return Output.NoItemsPresent
        val bukkitPlayer = Bukkit.getPlayer(player.uuid) ?: return Output.SomeItemsRemain

        val items = soul.items
            .map(StringFormatObject::raw)
            .map(ItemStackSerializer::decodeFromString)
            .mapNotNull { itemStackResult -> itemStackResult.getOrNull() }
        val notAddedItems = withContext(dispatchers.Main) {
            bukkitPlayer.inventory
                .addItem(*items.toTypedArray())
                .values
                .toList()
        }
        if (notAddedItems != soul.items) {
            withContext(dispatchers.Main) {
                soul.location
                    .toBukkitLocation()
                    .playSoundForPlayer(
                        player = bukkitPlayer,
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
