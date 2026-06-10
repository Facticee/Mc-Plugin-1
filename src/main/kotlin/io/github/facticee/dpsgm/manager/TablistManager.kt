package io.github.facticee.dpsgm.manager

import io.github.facticee.textlib.cmp
import io.github.facticee.textlib.plus
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player

object TablistManager {

    private val mm = MiniMessage.miniMessage()

    fun setPlayerList(player: Player) {


        val header: Component =

            cmp("        ", TextColor.color(0x151515)).decorate(TextDecoration.STRIKETHROUGH) +
            cmp("[", TextColor.color(0x151515)) +
            mm.deserialize("<gradient:#7700ff:#906bff><b> Server ").decoration(TextDecoration.STRIKETHROUGH, false) +
            cmp("]", TextColor.color(0x151515)) +
            cmp("        ", TextColor.color(0x151515)).decorate(TextDecoration.STRIKETHROUGH)


        val footer: Component = mm.deserialize("<gradient:#7700ff:#ffff00><b>Player online: ${player.server.onlinePlayers.size}</b>")

        player.sendPlayerListHeaderAndFooter(header, footer)

    }
}

