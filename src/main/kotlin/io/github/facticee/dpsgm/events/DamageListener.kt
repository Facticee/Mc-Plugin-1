package io.github.facticee.dpsgm.events

import io.github.facticee.dpsgm.manager.DamageManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.entity.Player

object DamageListener : Listener {

    @EventHandler
    fun onPlayerDamage(event: EntityDamageEvent) {
        val player = event.entity
        if (player !is Player) return

        // nur anzeigen, wenn Listener für diesen Spieler aktiviert ist
        if (!DamageManager.isEnabled(player)) return

        val hearts = event.finalDamage / 2.0
        val heartsFormatted = String.format("%.1f", hearts)

        val DamageMessage: Component = Component.text("Du hast ", NamedTextColor.GRAY)
            .append(Component.text("$heartsFormatted ❤", NamedTextColor.RED))
            .append(Component.text(" Schaden bekommen", NamedTextColor.GRAY))

        when (DamageManager.getMessageMode(player)) {
            DamageManager.MessageMode.CHAT -> player.sendMessage(DamageMessage)
            DamageManager.MessageMode.ACTIONBAR -> player.sendActionBar(DamageMessage)
        }
    }
}
