package io.github.facticee.dpsgm.manager

import io.github.facticee.dpsgm.manager.ClanManager.clans
import io.github.facticee.dpsgm.manager.ClanManager.getClan
import io.github.facticee.dpsgm.manager.ConfigManager.getConfig
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object PrefixSuffixManager {

    private lateinit var plugin: JavaPlugin
    private val mm = MiniMessage.miniMessage()

    // Cache für Online-Spieler
    private val prefixCache = ConcurrentHashMap<UUID, String>()
    private val suffixCache = ConcurrentHashMap<UUID, String>()

    fun setup(plugin: JavaPlugin) {
        this.plugin = plugin
        if (!plugin.dataFolder.exists()) plugin.dataFolder.mkdirs()
        // Standard-config beim Start laden/erstellen
        getConfig("prefixsuffix.yml")
    }

    /** Spieler ist gejoint, lade Prefix/Suffix in den Cache */
    fun onPlayerJoin(player: Player) {
        val config = ConfigManager.getConfig("prefixsuffix.yml")

        val prefix = config.getString("${player.uniqueId}-prefix") ?: ""
        val suffix = config.getString("${player.uniqueId}-suffix") ?: ""

        prefixCache[player.uniqueId] = prefix
        suffixCache[player.uniqueId] = suffix

        updatePlayerName(player)
    }

    /** Liefert den Prefix eines Spielers */
    fun getPrefix(player: Player): String = prefixCache[player.uniqueId] ?: ""

    /** Liefert den Suffix eines Spielers */
    fun getSuffix(player: Player): String = suffixCache[player.uniqueId] ?: ""

    /** Setzt den Prefix eines Spielers */
    fun setPrefix(player: Player, prefix: String) {
        prefixCache[player.uniqueId] = prefix
        updatePlayerName(player)
    }

    /** Setzt den Suffix eines Spielers */
    fun setSuffix(player: Player, suffix: String) {
        suffixCache[player.uniqueId] = suffix
        updatePlayerName(player)
    }

    /** Update DisplayName + PlayerListName auf Main-Thread */
    fun updatePlayerName(player: Player) {

        val clanName = getClan(player.uniqueId)
        val gradient = if (clanName != null) clans[clanName] ?: "" else ""

        val prefix = prefixCache[player.uniqueId] ?: ""
        val suffix = suffixCache[player.uniqueId] ?: ""

        val nameComponent = mm.deserialize("$prefix$gradient<b>${player.name}</b>$suffix")

    }
}


