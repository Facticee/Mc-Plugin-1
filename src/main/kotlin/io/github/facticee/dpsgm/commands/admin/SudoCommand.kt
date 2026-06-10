package io.github.facticee.dpsgm.commands.admin

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.greedyStringArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.playerProfileArgument
import org.bukkit.entity.Player

class SudoCommand {
    val command = commandTree("sudo") {
        withPermission("admin")
        playerProfileArgument("target") {
            greedyStringArgument("message") {
                playerExecutor { player, args ->
                    val message = args["message"] as String
                    val target = args["target"] as Player

                    val command = message.removePrefix("/")
                    if (message.startsWith("/"))
                        target.performCommand(command)

                    else {
                        target.chat(message)
                    }

                }
            }
        }
    }
}
