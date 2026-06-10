package io.github.facticee.dpsgm.manager

import io.github.facticee.textlib.cmp
import io.github.facticee.textlib.plus
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

object ClanManager {
    val mm = MiniMessage.miniMessage()

    // Vordefinierte Clans und deren Gradient-Tags
     val clans: Map<String, String> = mapOf(
        "blue" to "<gradient:#0000aa:#005AFF>",
        "red" to "<gradient:#aa0000:#FF5555>",
        "white" to "<gradient:#aaaaaa:#ffffff>",
        "black" to "<gradient:#131313:#282828>",
        "pink" to "<gradient:#FF00D5:#FFA5DB>",
        "violet" to "<gradient:#E86AFC:#E0BFFC>",
        "green" to "<gradient:#FFA500:#FFE300>"
    )

    // Spieler(UUID) -> ClanName Cache (optional, für schnelle Zugriffe)
    private val playerClans: MutableMap<UUID, String> = mutableMapOf()

    init {
        val config = ConfigManager.getConfig("config.yml")
        config.getKeys(false).forEach { uuidStr ->
            val clan = config.getString(uuidStr)?.lowercase() // lowercase für Safety
            try {
                if (clan != null && clans.containsKey(clan)) {
                    playerClans[UUID.fromString(uuidStr)] = clan
                }
            } catch (e: IllegalArgumentException) {
                // Ungültige UUID überspringen
            }
        }
    }

    /** Speichert alle Spieler-Clans in config.yml */
    private fun saveClans() {
        val config = ConfigManager.getConfig("clans.yml")
        playerClans.forEach { (uuid, clan) ->
            config.set(uuid.toString(), clan)
        }
        ConfigManager.save("clans.yml")
    }

    // --- Spieler- und Clan-Funktionen ---

    /** Liefert den Clan eines Spielers */
    fun getClan(playerUuid: UUID): String? = playerClans[playerUuid]

    /** Spieler einem Clan zuweisen und Name sofort updaten */
    fun joinClan(player: Player, clanName: String) {
        val real = clans.keys.find { it.equals(clanName, ignoreCase = true) } ?: return
        playerClans[player.uniqueId] = real

        val gradient = clans[real] ?: ""
        val clanComponent = mm.deserialize("$gradient<b>$real</b>")

        val message = mm.deserialize("<gray>Du bist dem Clan ")
            .append(clanComponent)
            .append(mm.deserialize("<gray> beigetreten!"))
        player.sendMessage(message)
// oben das für persönliche nachricht
// Das ist nur der name für die nachricht die kommt wenn man /clan join usw macht...
// unten das für broadcast nachricht
        val playerClanMessageName = mm.deserialize("$gradient<b>${player.name}</b>")

        val broadcastMessage = mm.deserialize("<b><gradient:#7700ff:#9d94ff>[Broadcast]<white>: ") +
                playerClanMessageName +
                cmp(" ist dem Clan ") +
                cmp("\"") +
                clanComponent +
                cmp("\"") +
                cmp(" beigetreten!")

        Bukkit.broadcast(broadcastMessage)

        updatePlayerName(player)
        saveClans()
    }

    /** Liefert alle online Spieler eines Clans */
    fun onlinePlayersInClan(clanName: String): List<Player> =
        Bukkit.getOnlinePlayers().filter { playerClans[it.uniqueId]?.equals(clanName, ignoreCase = true) == true }

    fun getAllPlayersInClan(clanName: String): List<String> {
        val realClanName = clans.keys.find { it.equals(clanName, ignoreCase = true) } ?: return emptyList()
        return playerClans.filter { it.value == realClanName }
            .keys
            .mapNotNull { Bukkit.getOfflinePlayer(it).name } // Konvertiert UUID zu Name
    }


    /** Setzt den Spielernamen im Chat und Tab-Liste auf den Clan-Gradienten */
    fun updatePlayerName(player: Player) {
        val clanName = getClan(player.uniqueId)
        val gradient = if (clanName != null) clans[clanName] ?: "" else ""

        val prefix = PrefixSuffixManager.getPrefix(player)  // Holt gespeichertes Prefix
        val suffix = PrefixSuffixManager.getSuffix(player)  // Holt gespeichertes Suffix

        val nameComponent = mm.deserialize(
            "$prefix$gradient<b>${player.name}</b>$suffix"
        )

        player.displayName(nameComponent)
        player.playerListName(nameComponent)
    }
}