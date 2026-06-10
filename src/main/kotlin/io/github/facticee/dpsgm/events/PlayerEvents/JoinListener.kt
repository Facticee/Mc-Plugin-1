package io.github.facticee.dpsgm.events.PlayerEvents

import io.github.facticee.dpsgm.manager.ClanManager
import io.github.facticee.dpsgm.manager.PrefixSuffixManager
import io.github.facticee.dpsgm.manager.TablistManager
import io.github.facticee.textlib.cmp
import io.github.facticee.textlib.plus
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

object JoinListener : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {

        val player = event.player

        val prefix = PrefixSuffixManager.getPrefix(player)
        val suffix = PrefixSuffixManager.getSuffix(player)

        // Clan-Name und Name-Component
        val clanName = ClanManager.getClan(player.uniqueId)
        val nameComponent: Component = if (clanName != null) {
            val gradient = ClanManager.clans[clanName] ?: ""
            ClanManager.mm.deserialize("$prefix$gradient<b>${player.name}</b>$suffix")
        } else {
            Component.text(player.name, NamedTextColor.GRAY)
        }

        // Join Message
        event.joinMessage(
            cmp(">> ", NamedTextColor.GREEN).decorate(TextDecoration.BOLD) + nameComponent
        )

        // Sound beim Join
        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.375f)

        // ActionBar-Willkommensnachricht
        val mm = MiniMessage.miniMessage()
        val welcomeMsg: Component = mm.deserialize("<gradient:#7700ff:#ffff00><b>Willkommen auf dem Server!")
        player.sendActionBar(welcomeMsg)

        // Spieler-Name in Tab-Liste updaten, falls Clan vorhanden
        if (clanName != null) {
            ClanManager.updatePlayerName(player)
        }

        // Tablist setzen
        TablistManager.setPlayerList(player)
    }
}



