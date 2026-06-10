package io.github.facticee.dpsgm.superpower

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

class ChaosCommand {

    val command = commandTree("chaositem") {
        playerExecutor { player, arguments ->
            player.inventory.addItem(ChaosItem.createItem())
            player.sendMessage(LegacyComponentSerializer.legacySection().deserialize("§aDu hast ein §cChaos-Fragment §aerhalten."))
        }
    }
}