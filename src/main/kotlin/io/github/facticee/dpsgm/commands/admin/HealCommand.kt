package io.github.facticee.dpsgm.commands.admin

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import io.github.facticee.textlib.cmp
import io.github.facticee.textlib.plus
import net.kyori.adventure.text.format.NamedTextColor

class HealCommand {
    val command = commandTree("heal") {
        playerExecutor { sender, arguments ->
            if (!sender.isOp) {
                sender.sendMessage("§cNur Admins dürfen diesen Command nutzen!")
                return@playerExecutor
            }

            sender.health = 20.0
            sender.sendMessage(cmp("Du wurdest ") + cmp("geheilt!", NamedTextColor.GREEN))
        }
    }

}