package io.github.facticee.dpsgm.events.PlayerEvents

import io.github.facticee.dpsgm.manager.ClanManager
import io.github.facticee.textlib.cmp
import io.github.facticee.textlib.plus
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

object LeaveListener : Listener {
    @EventHandler
    fun onJoin(event: PlayerQuitEvent) {



        val player = event.player

        val clanName = ClanManager.getClan(player.uniqueId)
        val nameComponent = if (clanName != null) {
            ClanManager.mm.deserialize("${ClanManager.clans[clanName]}<b>${player.name}</b>")
        } else {
            Component.text(player.name, NamedTextColor.GRAY)
        }

        event.quitMessage(
            cmp("<< ", NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD) + nameComponent)
    }
}