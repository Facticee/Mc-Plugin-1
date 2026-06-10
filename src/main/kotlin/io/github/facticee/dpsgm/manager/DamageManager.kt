package io.github.facticee.dpsgm.manager

import org.bukkit.entity.Player
import java.util.UUID

object DamageManager {
    // Map, ob ein Spieler seine Damage-Nachrichten sehen möchte
    private val enabledPlayers = mutableSetOf<Player>()

    private val messageMode = mutableMapOf<UUID, MessageMode>()

    enum class MessageMode {
        CHAT, ACTIONBAR
    }

    fun isEnabled(player: Player): Boolean = enabledPlayers.contains(player)
    fun setEnabled(player: Player, enabled: Boolean) {
        if (enabled) enabledPlayers.add(player)
        else enabledPlayers.remove(player)
    }

    fun setMessageMode(player: Player, mode: MessageMode) {
        messageMode[player.uniqueId] = mode
    }

    fun getMessageMode(player: Player): MessageMode {
        return messageMode[player.uniqueId] ?: MessageMode.CHAT // Standardwert: CHAT
    }
}