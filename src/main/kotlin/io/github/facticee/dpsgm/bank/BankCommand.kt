package io.github.facticee.dpsgm.bank

import dev.jorel.commandapi.kotlindsl.*
import io.github.facticee.textlib.cmp
import io.github.facticee.textlib.plus
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.OfflinePlayer

class BankCommand {


    val command = commandTree("bank") { // KORREKTUR: val command = commandTree(...)

        // /bank balance [spieler]
        literalArgument("balance") {
            playerExecutor { player, _ ->
                val balance = BankManager.getBalance(player.uniqueId)
                player.sendMessage(
                    cmp("Dein Kontostand: ", NamedTextColor.GREEN) +
                            cmp("$balance Dias", NamedTextColor.GOLD)
                )
            }

            literalArgument("ziel") {
                withPermission("bank.balance.others")
                playerExecutor { player, args ->
                    val targetOffline = args["ziel"] as OfflinePlayer
                    val targetUuid = targetOffline.uniqueId
                    val targetName = targetOffline.name ?: targetOffline.uniqueId.toString().substring(0, 8)

                    val balance = BankManager.getBalance(targetUuid)

                    player.sendMessage(
                        cmp("Kontostand von $targetName: ", NamedTextColor.GREEN) +
                                cmp("$balance Dias", NamedTextColor.GOLD)
                    )
                }
            }
        }

        // /bank pay <spieler> <amount>
        literalArgument("pay") {
            playerProfileArgument("ziel") {
                longArgument("amount", 1L) {
                    playerExecutor { player, args ->
                        val targetOffline = args["ziel"] as OfflinePlayer
                        val targetUuid = targetOffline.uniqueId
                        val targetName = targetOffline.name ?: targetOffline.uniqueId.toString().substring(0, 8)
                        val amount = args["amount"] as Long

                        if (targetUuid == player.uniqueId) {
                            player.sendMessage(cmp("Du kannst dir nicht selbst Geld überweisen.", NamedTextColor.RED))
                            return@playerExecutor
                        }

                        if (!BankManager.withdraw(player.uniqueId, amount)) {
                            player.sendMessage(cmp("Du hast nicht genügend Dias auf der Bank.", NamedTextColor.RED))
                            return@playerExecutor
                        }

                        if (!BankManager.deposit(targetUuid, amount)) {
                            // Selten, aber falls DB-Fehler beim Empfänger: Geld zurückbuchen
                            BankManager.deposit(player.uniqueId, amount)
                            player.sendMessage(cmp("Überweisung fehlgeschlagen. Geld wurde zurückgebucht.", NamedTextColor.RED))
                            return@playerExecutor
                        }

                        player.sendMessage(
                            cmp("Du hast ", NamedTextColor.GREEN) +
                                    cmp("$amount Dias", NamedTextColor.GOLD) +
                                    cmp(" an $targetName gesendet.", NamedTextColor.GREEN)
                        )

                        val targetPlayer = targetOffline.player
                        targetPlayer?.sendMessage(
                            cmp("Du hast ", NamedTextColor.GREEN) +
                                    cmp("$amount Dias", NamedTextColor.GOLD) +
                                    cmp(" von ${player.name} erhalten.", NamedTextColor.GREEN)
                        )
                    }
                }
            }
        }

        literalArgument("deposit") {
            playerExecutor { player, _ ->
                GuiManager.openDepositMenu(player)
            }
        }

        literalArgument("withdraw") {
            playerExecutor { player, _ ->
                GuiManager.openWithdrawMenu(player)
            }
        }
    }
}