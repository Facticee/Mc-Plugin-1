package io.github.facticee.dpsgm.bank

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object BankListener : Listener {

    // Beim Join: Lade die Bilanz des Spielers in den RAM-Cache
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        BankManager.loadBalance(event.player.uniqueId)
    }

    // Beim Quit: Speichere die Bilanz des Spielers in die DB und entferne sie aus dem RAM
    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        BankManager.saveBalance(event.player.uniqueId)
    }
}