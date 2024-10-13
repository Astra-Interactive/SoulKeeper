package ru.astrainteractive.aspekt.di.impl

import org.bukkit.plugin.java.JavaPlugin
import ru.astrainteractive.aspekt.command.di.CommandManagerModule
import ru.astrainteractive.aspekt.di.CoreModule
import ru.astrainteractive.aspekt.di.RootModule
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

class RootModuleImpl(plugin: JavaPlugin) : RootModule {
    override val coreModule: CoreModule by lazy {
        CoreModule.Default(plugin)
    }
    override val adminPrivateModule: AdminPrivateModule by lazy {
        AdminPrivateModule.Default(coreModule)
    }
    override val eventsModule: EventsModule by lazy {
        EventsModule.Default(coreModule)
    }
    override val menuModule: MenuModule by lazy {
        MenuModule.Default(coreModule)
    }
    override val sitModule: SitModule by lazy {
        SitModule.Default(coreModule)
    }
    override val guiModule: GuiModule by lazy {
        GuiModule.Default(coreModule)
    }
    override val autoBroadcastModule by lazy {
        AutoBroadcastModule.Default(coreModule)
    }
    override val discordLinkModule: DiscordLinkModule by lazy {
        DiscordLinkModule.Default(coreModule)
    }

    override val commandManagerModule: CommandManagerModule by lazy {
        CommandManagerModule.Default(
            coreModule = coreModule,
            guiModule = guiModule,
            sitModule = sitModule
        )
    }
    override val townyDiscordModule: TownyDiscordModule by lazy {
        TownyDiscordModule.Default(coreModule, discordLinkModule)
    }
    override val moneyDropModule: MoneyDropModule by lazy {
        MoneyDropModule.Default(coreModule)
    }
    override val autoCropModule: AutoCropModule by lazy {
        AutoCropModule.Default(coreModule)
    }
    override val newBeeModule: NewBeeModule by lazy {
        NewBeeModule.Default(coreModule = coreModule)
    }
    override val antiSwearModule: AntiSwearModule by lazy {
        AntiSwearModule.Default(coreModule = coreModule)
    }
    override val moneyAdvancementModule: MoneyAdvancementModule by lazy {
        MoneyAdvancementModule.Default(coreModule)
    }
    override val chatGameModule: ChatGameModule by lazy {
        ChatGameModule.Default(coreModule = coreModule)
    }
    override val economyModule: EconomyModule by lazy {
        EconomyModule.Default(coreModule)
    }
    override val soulsModule: SoulsModule by lazy {
        SoulsModule.Default(coreModule)
    }
}
