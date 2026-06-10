package io.github.facticee.dpsgm.gui

import io.github.facticee.dpsgm.sortinv.SortManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object MyItemsGUI {

    const val TITLE = "§8Lege deine Sortier-Vorlage fest"

    fun open(player: Player) {
        val gui = Bukkit.createInventory(null, 54, Component.text("Lege deine Sortier-Vorlage fest"))

        // Gespeicherte Vorlage laden und in die oberen 36 Slots einfügen
        val template = SortManager.getPlayerTemplate(player.uniqueId)
        for (i in 0..35) {
            gui.setItem(i, template[i])
        }

        // GUI-Steuerungselemente
        val filler = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta.apply { displayName(Component.empty()) }
        }
        for (i in 36..44) gui.setItem(i, filler)

        val saveButton = ItemStack(Material.LIME_DYE).apply {
            itemMeta = itemMeta.apply {
                displayName(Component.text("Speichern & Schließen", NamedTextColor.GREEN, TextDecoration.BOLD))
                lore(listOf(Component.text("Klicke, um diese Vorlage zu speichern.", NamedTextColor.GRAY)))
            }
        }
        gui.setItem(49, saveButton)

        player.openInventory(gui)
    }
}