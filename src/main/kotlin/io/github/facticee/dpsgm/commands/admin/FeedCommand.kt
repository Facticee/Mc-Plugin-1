package io.github.facticee.dpsgm.commands.admin

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.playerProfileArgument
import io.github.facticee.textlib.cmp
import io.github.facticee.textlib.plus
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

class FeedCommand {
    val command = commandTree("feed") {
        playerProfileArgument("ziel") {
            playerExecutor { sender, args ->
                if (!sender.isOp) {
                    sender.sendMessage("§cNur Admins dürfen diesen Command nutzen!")
                    return@playerExecutor
                }

                val target = args["ziel"] as Player

                target.foodLevel = 20
                target.saturation = 20f

                target.sendMessage(cmp("Du wurdest ") + cmp("gesättigt", NamedTextColor.GREEN))
            }
        }
    }
}