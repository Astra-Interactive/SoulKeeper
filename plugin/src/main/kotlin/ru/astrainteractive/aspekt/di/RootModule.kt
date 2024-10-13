package ru.astrainteractive.aspekt.di

import ru.astrainteractive.aspekt.command.di.CommandManagerModule
import ru.astrainteractive.aspekt.event.di.EventsModule
import ru.astrainteractive.aspekt.event.sit.di.SitModule
import ru.astrainteractive.aspekt.gui.di.GuiModule
import ru.astrainteractive.aspekt.module.adminprivate.command.discordlink.di.DiscordLinkModule
import ru.astrainteractive.aspekt.module.adminprivate.di.AdminPrivateModule
import ru.astrainteractive.aspekt.module.antiswear.di.AntiSwearModule
import ru.astrainteractive.aspekt.module.autobroadcast.di.AutoBroadcastModule
import ru.astrainteractive.aspekt.module.autocrop.di.AutoCropModule
import ru.astrainteractive.aspekt.module.chatgame.di.ChatGameModule
import ru.astrainteractive.aspekt.module.economy.di.EconomyModule
import ru.astrainteractive.aspekt.module.menu.di.MenuModule
import ru.astrainteractive.aspekt.module.moneyadvancement.di.MoneyAdvancementModule
import ru.astrainteractive.aspekt.module.moneydrop.di.MoneyDropModule
import ru.astrainteractive.aspekt.module.newbee.di.NewBeeModule
import ru.astrainteractive.aspekt.module.souls.di.SoulsModule
import ru.astrainteractive.aspekt.module.towny.discord.di.TownyDiscordModule

interface RootModule {
    val coreModule: CoreModule
    val adminPrivateModule: AdminPrivateModule
    val eventsModule: EventsModule
    val sitModule: SitModule
    val menuModule: MenuModule
    val guiModule: GuiModule
    val autoBroadcastModule: AutoBroadcastModule
    val discordLinkModule: DiscordLinkModule
    val commandManagerModule: CommandManagerModule
    val townyDiscordModule: TownyDiscordModule
    val moneyDropModule: MoneyDropModule
    val autoCropModule: AutoCropModule
    val newBeeModule: NewBeeModule
    val antiSwearModule: AntiSwearModule
    val moneyAdvancementModule: MoneyAdvancementModule
    val chatGameModule: ChatGameModule
    val economyModule: EconomyModule
    val soulsModule: SoulsModule
}
