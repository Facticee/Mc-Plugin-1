package io.github.facticee.dpsgm.general

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

object Vanish {

    fun toglleVanish(player: Player, plugin: Plugin) {
        // vor allen anderen verstecken
        for (p in Bukkit.getServer().onlinePlayers) {
            if (p.uniqueId != player.uniqueId) {
                p.hidePlayer(plugin, player)
            }
        }


    }
}