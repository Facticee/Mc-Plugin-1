package io.github.facticee.dpsgm.commands.EventCommand

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import io.github.facticee.dpsgm.manager.DamageManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

class DamageCommand {

    init {
        commandTree("damagelistener") {
            withPermission("damagelistener.use")

            // /damagelistener enable
            literalArgument("enable") {
                literalArgument("chat") {
                    playerExecutor { player: Player, _ ->
                        DamageManager.setMessageMode(player, DamageManager.MessageMode.CHAT)
                        DamageManager.setEnabled(player, true)
                        player.sendMessage(Component.text("✅ Damage-Listener aktiviert (Chat)", NamedTextColor.GREEN))
                    }
                }

                literalArgument("actionbar") {
                    playerExecutor { player: Player, _ ->
                        DamageManager.setMessageMode(player, DamageManager.MessageMode.ACTIONBAR)
                        DamageManager.setEnabled(player, true)
                        player.sendMessage(Component.text("✅ Damage-Listener aktiviert (Actionbar)", NamedTextColor.GREEN))
                    }
                }
            }

            // /damagelistener disable
            literalArgument("disable") {
                playerExecutor { player: Player, _ ->
                    DamageManager.setEnabled(player, false)
                    player.sendMessage(Component.text("❌ Damage-Listener deaktiviert", NamedTextColor.RED))
                }
            }

            // /damagelistener status
            literalArgument("status") {
                playerExecutor { player: Player, _ ->
                    val enabled = DamageManager.isEnabled(player)
                    player.sendMessage(Component.text("§eDamage-Listener Status:", NamedTextColor.YELLOW))
                    player.sendMessage(Component.text(" §7enabled: §f$enabled", NamedTextColor.GRAY))
                }
            }

            // /damagelistener help
            literalArgument("help") {
                playerExecutor { player: Player, _ ->
                    player.sendMessage(Component.text("§eDamage-Listener Help:", NamedTextColor.YELLOW))
                    player.sendMessage(Component.text(" §7enable §f= Nachrichten bei Schaden erhalten", NamedTextColor.GRAY))
                    player.sendMessage(Component.text(" §7disable §f= Nachrichten bei Schaden nicht erhalten", NamedTextColor.GRAY))
                    player.sendMessage(Component.text(" §7status §f= zeigt aktuellen Status", NamedTextColor.GRAY))
                }
            }
        }
    }
}
