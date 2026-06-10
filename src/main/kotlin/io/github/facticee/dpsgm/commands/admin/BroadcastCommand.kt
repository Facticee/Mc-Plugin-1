package io.github.facticee.dpsgm.commands.admin

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.greedyStringArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit

class BroadcastCommand {

    val mm = MiniMessage.miniMessage()

    val command = commandTree("broadcast") {
        greedyStringArgument("message") {
            withPermission("admin")
            withAliases("shout")
            playerExecutor { player, arguments ->

                var message = arguments["message"] as String
                val broadcastmsg = mm.deserialize("<b><gradient:#7700ff:#9d94ff>[Broadcast]<white>: ")
                val broadcastchatcolormessage = mm.deserialize("<gradient:#AA0000:#ffa200>$message")
                val fullMessage = broadcastmsg.append(broadcastchatcolormessage)

                Bukkit.broadcast(fullMessage)
                //broadcast(broadcastmsg + cmp(" $message", NamedTextColor.AQUA, true))



            }
        }
    }
}