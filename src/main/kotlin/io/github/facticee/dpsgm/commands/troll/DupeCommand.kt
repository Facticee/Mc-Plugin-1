package io.github.facticee.dpsgm.commands.troll

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor

class DupeCommand {
     val command = commandTree("dupe") {
         playerExecutor { player, args ->
             player.sendMessage("nein :)")
         }
     }
}