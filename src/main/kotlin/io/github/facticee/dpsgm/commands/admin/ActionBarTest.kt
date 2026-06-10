package io.github.facticee.dpsgm.commands.admin

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

class ActionBarTest {
    init {
        commandTree("actionbartest") {
            playerExecutor { player: Player, _ ->
                val message = Component.text("✅ Erfolgreich", NamedTextColor.GREEN)
                player.sendActionBar(message)
            }
        }
    }
}