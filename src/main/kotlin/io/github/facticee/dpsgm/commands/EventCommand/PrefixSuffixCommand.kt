package io.github.facticee.dpsgm.commands.EventCommand

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.greedyStringArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import io.github.facticee.dpsgm.manager.PrefixSuffixManager
import io.github.facticee.textlib.cmp
import io.github.facticee.textlib.plus
import net.kyori.adventure.text.format.NamedTextColor

class PrefixSuffixCommand {

    val commandPrefix = commandTree("setprefix") {
        greedyStringArgument("prefix") {
            playerExecutor { player, args ->
                val prefix = args.get("prefix").toString()
                PrefixSuffixManager.setPrefix(player, prefix)
                player.sendMessage(cmp("✅ Prefix gesetzt: ", NamedTextColor.GREEN) + cmp(prefix))
            }
        }
    }

    val commandSuffix = commandTree("setsuffix") {
        greedyStringArgument("suffix") {
            playerExecutor { player, args ->
                val suffix = args.get("suffix").toString()
                PrefixSuffixManager.setSuffix(player, suffix)
                player.sendMessage(cmp("✅ Suffix gesetzt: ", NamedTextColor.GREEN) + cmp(suffix))
            }
        }
    }
}
