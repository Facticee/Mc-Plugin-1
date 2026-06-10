package io.github.facticee.dpsgm.events

import io.github.facticee.dpsgm.manager.ClanManager
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object ClanChatListener : Listener {
    private val mm = MiniMessage.miniMessage()

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val player = event.player
        val clanName = ClanManager.getClan(player.uniqueId)

        // Nachricht so rendern, wie du willst
        event.renderer { _, _, message, _ ->
            val nameComponent = if (clanName != null) {
                // Clan-Gradient-Name
                ClanManager.mm.deserialize("${ClanManager.clans[clanName]}<b>${player.name}</b>")
            } else {
                // Fallback normaler Name
                Component.text(player.name, NamedTextColor.GRAY)
            }

            Component.text()
                .append(nameComponent)
                .append(Component.text(": ", NamedTextColor.WHITE))
                .append(message)
                .build()
        }
    }
}