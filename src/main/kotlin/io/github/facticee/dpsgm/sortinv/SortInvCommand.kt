package io.github.facticee.dpsgm.sortinv

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.stringArgument
import io.github.facticee.dpsgm.gui.MyItemsGUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player

class SortInvCommand {

    init {
        commandTree("sortinv") {
            withPermission("inventorysorter.use")

            literalArgument("myitems") {
                playerExecutor { player, _ ->
                    SortManager.setPlayerMode(player.uniqueId, SortMode.MY_ITEMS)
                    player.sendMessage(
                        Component.text("Sortier-Modus auf ", NamedTextColor.GRAY)
                            .append(Component.text("MyItems", NamedTextColor.AQUA, TextDecoration.BOLD))
                            .append(Component.text(" gesetzt. Öffne das Template...", NamedTextColor.GRAY))
                    )
                    MyItemsGUI.open(player)
                }
            }

            literalArgument("allitems") {
                playerExecutor { player, _ ->
                    SortManager.setPlayerMode(player.uniqueId, SortMode.ALL_ITEMS)
                    player.sendMessage(
                        Component.text("Sortier-Modus auf ", NamedTextColor.GRAY)
                            .append(Component.text("AllItems", NamedTextColor.GREEN, TextDecoration.BOLD))
                            .append(Component.text(" gesetzt. Doppelklicke in deinem Inventar zum Sortieren!", NamedTextColor.GRAY))
                    )
                }
            }
        }
    }
}
