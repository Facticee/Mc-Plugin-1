package io.github.facticee.dpsgm.commands.troll

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.playerProfileArgument
import io.github.facticee.dpsgm.manager.ClanManager
import io.github.facticee.textlib.cmp
import io.github.facticee.textlib.plus

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class PrankCommand {

    private val mm = MiniMessage.miniMessage()

    val command = commandTree("prank") {

        literalArgument("strike") {
            playerProfileArgument("ziel") {
                literalArgument("effect") {
                    playerExecutor { sender, args ->
                        if (!sender.isOp) {
                            sender.sendMessage("§cNur Admins dürfen diesen Command nutzen!")
                            return@playerExecutor
                        }

                        val target = args["ziel"] as Player
                        // Lightning Effect ohne Schaden
                        target.world.strikeLightningEffect(target.location)
                        sender.sendMessage("§aLightning Effect auf ${target.name}!")
                    }
                }

                // Normaler Strike ohne "effect"
                playerExecutor { sender, args ->
                    if (!sender.isOp) {
                        sender.sendMessage("§cNur Admins dürfen diesen Command nutzen!")
                        return@playerExecutor
                    }

                    val target = args["ziel"] as Player
                    // Normales Lightning
                    target.world.strikeLightning(target.location)
                    sender.sendMessage("§aBlitz auf ${target.name} eingeschlagen!")
                }
            }
        }

        literalArgument("cheats") {
            literalArgument("xray") {
                playerProfileArgument("ziel") {
                    withPermission("admin")
                    playerExecutor { player, arguments ->

                        val target = arguments["ziel"] as Player

                        val playerClan = ClanManager.getClan(player.uniqueId) // z. B. "blue"
                        val gradient = ClanManager.clans[playerClan] ?: ""
                        val targetComponent = ClanManager.mm.deserialize("$gradient<b>${target.name}</b>")

                        val xrayMessage = mm.deserialize("<b><gradient:#8D0000:#FF0000>[ANTI CHEAT]<Gray>: Player ") +
                                targetComponent +
                                cmp(" might use", NamedTextColor.GRAY, true) +
                                cmp(" X-Ray", NamedTextColor.DARK_RED, true)

                        Bukkit.broadcast(xrayMessage)

                    }
                }
            }
        }

        literalArgument("disconnect") {
            literalArgument("client") {
                playerProfileArgument("ziel") {
                    withPermission("admin")
                    playerExecutor { player, arguments ->
                        val target = arguments["ziel"]

                        if (target == null) {
                            player.sendMessage("Spieler nicht gefunden.")
                            return@playerExecutor
                        }

                        player.kick(cmp("Invalid Session (Try restarting your game)", NamedTextColor.WHITE))

                    }
                }
            }
        }

        literalArgument("disconnect") {
            literalArgument("server") {
                playerProfileArgument("ziel") {
                    withPermission("admin")
                    playerExecutor { player, arguments ->
                        val target = arguments["ziel"]

                        if (target == null) {
                            player.sendMessage("Spieler nicht gefunden.")
                            return@playerExecutor
                        }

                        player.kick(
                            cmp(
                                "Internal Exception java.net.SocketException: Connection Reset ",
                                NamedTextColor.WHITE
                            )
                        )

                    }
                }
            }
        }
    }
}