package io.github.facticee.dpsgm.commands.EventCommand

import dev.jorel.commandapi.kotlindsl.booleanArgument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import io.github.facticee.dpsgm.manager.FireballManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

class FireballCommand {
    init {
        commandTree("fireball") {
            withPermission("admin")
            withAliases("fb")

            // /fireball enable [destroyEnvironment] [consumeInSurvival]
            literalArgument("enable") {

                // Ohne Argumente → Standardwerte
                playerExecutor { player: Player, _ ->
                    FireballManager.isEnabled = true
                    FireballManager.destroyEnvironment = false
                    FireballManager.consumeInSurvival = true

                    player.sendMessage(
                        Component.text("✅ Fireball aktiviert!", NamedTextColor.GREEN)
                            .append(Component.text(" (destroyEnvironment=false, consumeInSurvival=true)", NamedTextColor.GRAY))
                    )
                }

                // Mit 1 Boolean → destroyEnvironment
                booleanArgument("destroyEnvironment") {
                    playerExecutor { player: Player, args ->
                        val destroy = args[0] as Boolean
                        FireballManager.isEnabled = true
                        FireballManager.destroyEnvironment = destroy
                        FireballManager.consumeInSurvival = true

                        player.sendMessage(
                            Component.text("✅ Fireball aktiviert!", NamedTextColor.GREEN)
                                .append(Component.text(" (destroyEnvironment=$destroy, consumeInSurvival=true)", NamedTextColor.GRAY))
                        )
                    }

                    // Mit 2 Booleans → destroyEnvironment + consumeInSurvival
                    booleanArgument("consumeInSurvival") {
                        playerExecutor { player: Player, args ->
                            val destroy = args[0] as Boolean
                            val consume = args[1] as Boolean
                            FireballManager.isEnabled = true
                            FireballManager.destroyEnvironment = destroy
                            FireballManager.consumeInSurvival = consume

                            player.sendMessage(
                                Component.text("✅ Fireball aktiviert!", NamedTextColor.GREEN)
                                    .append(Component.text(" (destroyEnvironment=$destroy, consumeInSurvival=$consume)", NamedTextColor.GRAY))
                            )
                        }
                    }
                }
            }

            // /fireball disable
            literalArgument("disable") {
                playerExecutor { player: Player, _ ->
                    FireballManager.isEnabled = false
                    player.sendMessage(Component.text("❌ Fireball deaktiviert!", NamedTextColor.RED))
                }
            }

            // /fireball status
            literalArgument("status") {
                playerExecutor { player: Player, _ ->
                    player.sendMessage(Component.text("§eFireball Status:", NamedTextColor.YELLOW))
                    player.sendMessage(Component.text(" §7enabled: §f${FireballManager.isEnabled}", NamedTextColor.GRAY))
                    player.sendMessage(Component.text(" §7destroyEnvironment: §f${FireballManager.destroyEnvironment}", NamedTextColor.GRAY))
                    player.sendMessage(Component.text(" §7consumeInSurvival: §f${FireballManager.consumeInSurvival}", NamedTextColor.GRAY))
                    player.sendMessage(Component.text(" §7debug: §f${FireballManager.debug}", NamedTextColor.GRAY))
                }
            }

            // /fireball debug → toggelt Debug-Nachrichten
            literalArgument("debug") {
                playerExecutor { player: Player, _ ->
                    FireballManager.debug = !FireballManager.debug
                    val state = if (FireballManager.debug) "§aAN" else "§cAUS"
                    player.sendMessage(Component.text("§eFireball Debug ist jetzt $state"))
                }
            }

            literalArgument("velocityinfo") {
                playerExecutor { player: Player, _ ->
                    FireballManager.velocitydebug = !FireballManager.velocitydebug
                    val state = if (FireballManager.velocitydebug) "§aAN" else "§cAUS"
                    player.sendMessage(Component.text("§eFireball Debug ist jetzt $state"))
                }
            }

            // /fireball help
            literalArgument("help") {
                playerExecutor { player: Player, _ ->
                    player.sendMessage(Component.text("§eFireball Help:", NamedTextColor.YELLOW))
                    player.sendMessage(Component.text(" §7destroyEnvironment §f= true ⇒ Explosionen zerstören Blöcke & Feuer", NamedTextColor.GRAY))
                    player.sendMessage(Component.text(" §7consumeInSurvival §f= true ⇒ Fire Charges werden in SURVIVAL/ADVENTURE verbraucht", NamedTextColor.GRAY))
                    player.sendMessage(Component.text(" §7debug §f= true ⇒ Debug-Nachrichten ein/aus", NamedTextColor.GRAY))
                    player.sendMessage(Component.text(" §7Hinweis: false deaktiviert die jeweilige Funktion", NamedTextColor.GRAY))
                }
            }
        }
    }
}