package io.github.facticee.dpsgm.commands.EventCommand

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.greedyStringArgument
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import io.github.facticee.dpsgm.manager.ClanManager
import io.github.facticee.textlib.plus
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class ClanCommand {
    private val mm = MiniMessage.miniMessage()

    val command = commandTree("clan") {
        literalArgument("join") {
            // Literal für jeden Clan
            literalArgument("blue") { playerExecutor { player, _ -> ClanManager.joinClan(player, "blue") } }
            literalArgument("red") { playerExecutor { player, _ -> ClanManager.joinClan(player, "red") } }
            literalArgument("white") { playerExecutor { player, _ -> ClanManager.joinClan(player, "white") } }
            literalArgument("black") { playerExecutor { player, _ -> ClanManager.joinClan(player, "black") } }
            literalArgument("pink") { playerExecutor { player, _ -> ClanManager.joinClan(player, "pink") } }
            literalArgument("violet") { playerExecutor { player, _ -> ClanManager.joinClan(player, "violet") } }
            literalArgument("green") { playerExecutor { player, _ -> ClanManager.joinClan(player, "green") } }
        }

        literalArgument("list") {
            literalArgument("blue") { playerExecutor { player, _ -> sendClanList(player, "blue") } }
            literalArgument("red") { playerExecutor { player, _ -> sendClanList(player, "red") } }
            literalArgument("white") { playerExecutor { player, _ -> sendClanList(player, "white") } }
            literalArgument("black") { playerExecutor { player, _ -> sendClanList(player, "black") } }
            literalArgument("pink") { playerExecutor { player, _ -> sendClanList(player, "pink") } }
            literalArgument("violet") { playerExecutor { player, _ -> sendClanList(player, "violet") } }
            literalArgument("green") { playerExecutor { player, _ -> sendClanList(player, "green") } }
        }

        literalArgument("chat") {
            greedyStringArgument("message") {
                playerExecutor { player, args ->
                    val clanName = ClanManager.getClan(player.uniqueId)
                        ?: run {
                            player.sendMessage(mm.deserialize("<red>Du bist in keinem Clan!"))
                            return@playerExecutor
                        }

                    val message = args["message"] as String
                    val prefix = ClanManager.clans[clanName] ?: ""

                    // Name in Gradient/Fett
                    val nameComponent = mm.deserialize("$prefix<b>${player.name}</b>")

                    // Nachricht normal
                    val messageComponent = Component.text(": $message")

                    // [Clan Chat] Tag
                    val clanTag = Component.text("[ClanChat] ", NamedTextColor.DARK_GRAY)

                    val fullMessage = clanTag.append(nameComponent).append(messageComponent)

                    ClanManager.onlinePlayersInClan(clanName).forEach { it.sendMessage(fullMessage) }
                }
            }
        }
    }

    val ccCommand = commandTree("cc") {
        greedyStringArgument("message") {
            playerExecutor { player, args ->
                // Den Befehl einfach an /clan chat weitergeben
                val message = args["message"] as String
                val clanName = ClanManager.getClan(player.uniqueId)
                if (clanName == null) {
                    player.sendMessage(mm.deserialize("<red>Du bist in keinem Clan!"))
                    return@playerExecutor
                }

                val prefix = ClanManager.clans[clanName] ?: ""
                val nameComponent = mm.deserialize("$prefix<b>${player.name}</b>")
                val messageComponent = Component.text(": ") + Component.text("$message")
                val clanTag = Component.text("[Clan Chat] ", NamedTextColor.GRAY)
                val fullMessage = clanTag.append(nameComponent).append(messageComponent)

                ClanManager.onlinePlayersInClan(clanName).forEach { it.sendMessage(fullMessage) }
            }
        }
    }
    private fun sendClanList(sender: Player, clanName: String) {
        // Greift jetzt auf die neue Funktion für alle Mitglieder zu
        val allMembers = ClanManager.getAllPlayersInClan(clanName)
        val gradient = ClanManager.clans[clanName] ?: ""
        val clanComponent = mm.deserialize("$gradient<b>$clanName</b>")

        val header = mm.deserialize("<dark_gray>---</dark_gray> Mitglieder im Clan ")
            .append(clanComponent)
            .append(mm.deserialize(" <dark_gray>---"))
        sender.sendMessage(header)

        if (allMembers.isEmpty()) {
            sender.sendMessage(mm.deserialize("<gray><i>Dieser Clan hat keine Mitglieder.</i>"))
        } else {
            // Erstellt eine Liste von Component-Namen, die mit Kommas verbunden werden
            val playerNames = allMembers.map { playerName ->
                // Prüft, ob der Spieler online ist, um ihn ggf. hervorzuheben (optionaler Bonus)
                val isOnline = Bukkit.getPlayer(playerName) != null
                if (isOnline) {
                    mm.deserialize("$gradient<b>$playerName</b>") // Online-Spieler in Fett
                } else {
                    mm.deserialize("<gray>$playerName</gray>") // Offline-Spieler in Grau
                }
            }
            val listComponent = Component.join(JoinConfiguration.separator(mm.deserialize("<dark_gray>, ")), playerNames)
            sender.sendMessage(listComponent)
        }
    }
}

