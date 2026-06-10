package io.github.facticee.dpsgm.sortinv

import io.github.facticee.dpsgm.gui.MyItemsGUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack

object SortListener : Listener {
        // Löst die Sortierung bei Doppelklick aus
        @EventHandler
        fun onDoubleClickToSort(event: InventoryClickEvent) {
            if (event.click != ClickType.DOUBLE_CLICK || event.clickedInventory !== event.whoClicked.inventory) {
                return
            }

            event.isCancelled = true // Verhindert das Standard-Verhalten (Items einsammeln)
            val player = event.whoClicked as Player
            SortManager.sortInventory(player)
            player.sendMessage(Component.text("Inventar sortiert!", NamedTextColor.GREEN))
        }

        // Behandelt Klicks innerhalb der Template-GUI
        @EventHandler
        fun onGuiClick(event: InventoryClickEvent) {
            if (event.view.title != MyItemsGUI.TITLE) return

            val player = event.whoClicked as Player
            val clickedSlot = event.rawSlot

            // Klick in der GUI
            if (event.inventory == event.view.topInventory) {
                event.isCancelled = true // Grundsätzlich alle Klicks in der GUI erstmal abbrechen

                // Template-Bereich (Slot 0-35)
                if (clickedSlot in 0..35) {
                    // Erlaubt das Setzen/Tauschen von Items, aber nicht das direkte Herausnehmen
                    event.inventory.setItem(clickedSlot, event.cursor)
                    player.inventory.setItem(event.slot, null) // Simuliert das "Setzen" des Items
                }

                // Speicher-Button (Slot 49)
                if (clickedSlot == 49) {
                    val newTemplate = Array<ItemStack?>(36) { event.inventory.getItem(it) }
                    SortManager.setPlayerTemplate(player.uniqueId, newTemplate)
                    player.closeInventory()
                    player.sendMessage(Component.text("Vorlage erfolgreich gespeichert!", NamedTextColor.GREEN))
                }
            }
            // Klick im Spielerinventar (unten) ist erlaubt, damit Items aufgenommen werden können
            else {
                event.isCancelled = false
            }
        }
    }