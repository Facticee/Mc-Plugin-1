package io.github.facticee.dpsgm

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import io.github.facticee.dpsgm.bank.BankCommand
import io.github.facticee.dpsgm.bank.BankListener
import io.github.facticee.dpsgm.bank.BankManager
import io.github.facticee.dpsgm.bank.GuiManager
import io.github.facticee.dpsgm.commands.EventCommand.ClanCommand
import io.github.facticee.dpsgm.commands.EventCommand.DamageCommand
import io.github.facticee.dpsgm.commands.EventCommand.FireballCommand
import io.github.facticee.dpsgm.commands.EventCommand.PrefixSuffixCommand
import io.github.facticee.dpsgm.commands.TimerCommand
import io.github.facticee.dpsgm.commands.admin.*
import io.github.facticee.dpsgm.commands.troll.DupeCommand
import io.github.facticee.dpsgm.commands.troll.PrankCommand
import io.github.facticee.dpsgm.events.ClanChatListener
import io.github.facticee.dpsgm.events.DamageListener
import io.github.facticee.dpsgm.events.Fireball
import io.github.facticee.dpsgm.events.PlayerEvents.JoinListener
import io.github.facticee.dpsgm.events.PlayerEvents.LeaveListener
import io.github.facticee.dpsgm.general.Timer
import io.github.facticee.dpsgm.manager.ConfigManager
import io.github.facticee.dpsgm.manager.PrefixSuffixManager
import io.github.facticee.dpsgm.superpower.ChaosCommand
import io.github.facticee.dpsgm.superpower.ChaosListener
import io.github.facticee.textlib.cmp
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {



    override fun onLoad() {
        CommandAPI.onLoad(CommandAPIPaperConfig(this).silentLogs(true))
        server.consoleSender.sendMessage(cmp("Server gestartet!"))

        FireballCommand()
        DamageCommand()
        ActionBarTest()
        TimerCommand()
        ClanCommand()
        DupeCommand()
        HealCommand()
        FeedCommand()
        PrankCommand()
        BroadcastCommand()
        SudoCommand()
        PrefixSuffixCommand()
        BankCommand()
        ChaosCommand()

    }

    override fun onEnable() {
        CommandAPI.onEnable()
        server.pluginManager.registerEvents(Fireball, this)
        server.pluginManager.registerEvents(DamageListener, this)
        server.pluginManager.registerEvents(JoinListener, this)
        server.pluginManager.registerEvents(LeaveListener, this)
        server.pluginManager.registerEvents(ClanChatListener, this)
        server.pluginManager.registerEvents(BankListener, this)
        server.pluginManager.registerEvents(ChaosListener(this), this)

        ConfigManager.setup(this)
        PrefixSuffixManager.setup(this)

        BankManager.setup(this)
        GuiManager.setup(this)

        Timer.init(this)
    }

    override fun onDisable() {
        CommandAPI.onDisable()
        BankManager.shutdown()
        server.consoleSender.sendMessage(cmp("Server gestoppt!"))
    }
}