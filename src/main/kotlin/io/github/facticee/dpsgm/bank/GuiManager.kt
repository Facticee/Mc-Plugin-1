package io.github.facticee.dpsgm.bank

import io.github.facticee.textlib.cmp
import io.github.facticee.textlib.plus
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import kotlin.math.min

// Deine Text-Lib-Funktion (Annahme)

private val DEPOSIT_TITLE = cmp(" Bank - Einzahlen", NamedTextColor.DARK_AQUA, true)
private val WITHDRAW_TITLE = cmp(" Bank - Abheben", NamedTextColor.DARK_AQUA, true)


object GuiManager : Listener {

    private lateinit var plugin: JavaPlugin

    // --- SETUP ---
    fun setup(plugin: JavaPlugin) {
        this.plugin = plugin
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    // --- 1. Deposit GUI ---

    fun openDepositMenu(player: Player) {
        val gui = Bukkit.createInventory(player, 27, DEPOSIT_TITLE) // 3 Zeilen

        val separator = createGuiItem(Material.BLUE_STAINED_GLASS_PANE, cmp(" ", NamedTextColor.GRAY))
        for (i in 18..26) gui.setItem(i, separator)

        gui.setItem(22, createGuiItem(
            Material.CHEST,
            cmp("Einzahlungs-Zone", NamedTextColor.YELLOW, true),
            listOf(
                cmp("Lege physische Diamanten ein.", NamedTextColor.GRAY),
                cmp("Schließen, um zu bestätigen.", NamedTextColor.GRAY)
            )
        ))

        player.openInventory(gui)
    }

    @EventHandler
    fun onDepositClick(event: InventoryClickEvent) {
        if (event.view.title() != DEPOSIT_TITLE) return

        val player = event.whoClicked as Player
        val topInventory = event.inventory // Das 27-Slots-GUI-Inventar
        val cursor = event.cursor

        // 1. Klicks auf Trennlinien/Chest-Symbol (Slots 18-26) IMMER blockieren
        if (event.slot in 18..26 && event.clickedInventory == topInventory) {
            event.isCancelled = true
            return
        }

        // 2. Klick im Top-Inventar (Einzahlungsbereich: Slots 0-17)
        if (event.clickedInventory == topInventory) {

            // Problem 1 Fix: Verhindere das Herausnehmen von Items per Shift-Klick
            if (event.isShiftClick) {
                event.isCancelled = true
                return
            }

            // Problem 1 Fix: Verhindere das Herausnehmen von Items per Linksklick
            if (event.slot in 0..17 && (cursor == null || cursor.type == Material.AIR)) {
                if (event.currentItem != null && event.currentItem!!.type != Material.AIR) {
                    event.isCancelled = true
                    player.sendMessage(cmp("Items können nur durch Schließen der GUI eingezahlt werden.", NamedTextColor.RED))
                    return
                }
            }
            // Normale Rechts-/Linksklicks, um Items in leere Top-Slots zu legen, werden hier nicht gecancelt (erlaubt Problem 2).
        }

        // 3. Klick im Spieler-Inventar (Bottom Inventory)
        if (event.clickedInventory != topInventory) {
            // Problem 2 Fix: Nur Diamanten dürfen per SHIFT-Klick nach oben transferiert werden.
            if (event.isShiftClick) {
                val clickedItem = event.currentItem
                if (clickedItem == null || clickedItem.type != Material.DIAMOND) {
                    event.isCancelled = true
                    player.sendMessage(cmp("Du kannst nur Diamanten einzahlen.", NamedTextColor.RED))
                }
                // Wenn es ein Diamant ist, wird NICHT gecancelt. Bukkit erledigt den Shift-Transfer.
            }
            // Normale Klicks (ohne Shift) im Spieler-Inventar werden IMMER erlaubt (wichtig für Problem 2).
        }
    }


    @EventHandler
    fun onDepositClose(event: InventoryCloseEvent) {
        if (event.view.title() != DEPOSIT_TITLE) return

        val player = event.player as Player
        val inventory = event.inventory
        var totalDeposited = 0L
        val leftoverItems = mutableListOf<ItemStack>()

        for (i in 0..17) {
            val item = inventory.getItem(i)
            if (item == null || item.type == Material.AIR) continue

            if (item.type == Material.DIAMOND) {
                totalDeposited += item.amount
            } else {
                leftoverItems.add(item)
            }
        }

        if (totalDeposited > 0) {
            BankManager.deposit(player.uniqueId, totalDeposited)
            player.sendMessage(
                cmp("Du hast ", NamedTextColor.GREEN) +
                        cmp("$totalDeposited Dias", NamedTextColor.GOLD) +
                        cmp(" eingezahlt.", NamedTextColor.GREEN)
            )
        }

        if (leftoverItems.isNotEmpty()) {
            val didNotFit = player.inventory.addItem(*leftoverItems.toTypedArray())
            for (item in didNotFit.values) {
                player.world.dropItemNaturally(player.location, item)
            }
            player.sendMessage(cmp("Andere Items wurden zurückgelegt, da nur Diamanten akzeptiert werden.", NamedTextColor.YELLOW))
        }
    }

    // --- 2. Withdraw GUI ---

    fun openWithdrawMenu(player: Player) {
        val balance = BankManager.getBalance(player.uniqueId)
        val gui = Bukkit.createInventory(player, 36, WITHDRAW_TITLE)

        val darkGlass = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, cmp(" "))
        for (i in 0..8) gui.setItem(i, darkGlass)
        for (i in 27..35) gui.setItem(i, darkGlass)
        gui.setItem(9, darkGlass); gui.setItem(17, darkGlass)
        gui.setItem(18, darkGlass); gui.setItem(26, darkGlass)

        val balanceItem = createGlowItem(
            Material.NETHER_STAR,
            cmp("Kontostand", NamedTextColor.AQUA, true),
            listOf(
                cmp("Aktuelles Guthaben:", NamedTextColor.GRAY),
                cmp("» $balance Dias", NamedTextColor.GOLD, true)
            )
        )
        gui.setItem(4, balanceItem)

        gui.setItem(11, createWithdrawItem(Material.DIAMOND, 1, balance))
        gui.setItem(12, createWithdrawItem(Material.DIAMOND, 8, balance))
        gui.setItem(13, createWithdrawItem(Material.DIAMOND, 32, balance))
        gui.setItem(14, createWithdrawItem(Material.DIAMOND, 64, balance))
        gui.setItem(15, createWithdrawItem(Material.RED_WOOL, -1, balance))

        player.openInventory(gui)
    }

    @EventHandler
    fun onWithdrawClick(event: InventoryClickEvent) {
        if (event.view.title() != WITHDRAW_TITLE) return

        event.isCancelled = true
        val player = event.whoClicked as Player

        val isAllWithdraw = event.slot == 15

        val amountToWithdraw: Long = when (event.slot) {
            11 -> 1
            12 -> 8
            13 -> 32
            14 -> 64
            15 -> BankManager.getBalance(player.uniqueId)
            else -> return
        }

        if (isAllWithdraw && amountToWithdraw <= 0) {
            player.sendMessage(cmp("Du hast keine Dias zum Abheben.", NamedTextColor.YELLOW))
            return
        }

        handleWithdraw(player, amountToWithdraw)
    }

    private fun handleWithdraw(player: Player, amount: Long) {
        if (amount <= 0) {
            player.sendMessage(cmp("Du kannst keinen ungültigen Betrag abheben.", NamedTextColor.RED))
            return
        }

        if (!BankManager.withdraw(player.uniqueId, amount)) {
            player.sendMessage(cmp("So viele Diamanten hast du nicht auf der Bank.", NamedTextColor.RED))
            return
        }

        val itemsToGive = mutableListOf<ItemStack>()
        var remainingAmount = amount
        while (remainingAmount >= 64) {
            itemsToGive.add(ItemStack(Material.DIAMOND, 64))
            remainingAmount -= 64
        }
        if (remainingAmount > 0) {
            itemsToGive.add(ItemStack(Material.DIAMOND, remainingAmount.toInt()))
        }

        val didNotFit = player.inventory.addItem(*itemsToGive.toTypedArray())

        if (didNotFit.isNotEmpty()) {
            var refundedAmount = 0L
            for (item in didNotFit.values) {
                refundedAmount += item.amount
            }

            BankManager.deposit(player.uniqueId, refundedAmount)
            player.sendMessage(
                cmp("Dein Inventar war voll! ", NamedTextColor.RED) +
                        cmp("$refundedAmount Dias", NamedTextColor.GOLD) +
                        cmp(" wurden zurückgebucht.", NamedTextColor.RED)
            )
        } else {
            player.sendMessage(
                cmp("Du hast erfolgreich ", NamedTextColor.GREEN) +
                        cmp("$amount Dias", NamedTextColor.GOLD) +
                        cmp(" abgehoben.", NamedTextColor.GREEN)
            )
        }

        openWithdrawMenu(player)
    }


    // --- Helper Funktionen (FIX für Unresolved reference 'createGuiItem') ---

    private fun createGlowItem(material: Material, name: Component, lore: List<Component>): ItemStack {
        val item = ItemStack(material)
        item.editMeta { meta ->
            meta.displayName(name)
            meta.lore(lore)

            // Enchantment für Glow (Protection 1)
            meta.addEnchant(Enchantment.PROTECTION, 1, true)
            // HIDE_ENCHANTS ist wichtig, um die Lore "Protection I" zu entfernen
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)

            meta.addItemFlags(
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ENCHANTS
            )
        }
        return item
    }

    private fun createGuiItem(material: Material, name: Component, lore: List<Component> = emptyList()): ItemStack {
        val item = ItemStack(material)

        item.editMeta { meta ->
            meta.displayName(name)
            meta.lore(lore)

            // Glow für Diamanten (mit Unbreaking 1)
            if (material == Material.DIAMOND || material == Material.DIAMOND_BLOCK) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true)
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            }

            meta.addItemFlags(
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ENCHANTS
            )
        }
        return item
    }

    private fun createWithdrawItem(material: Material, amount: Long, balance: Long): ItemStack {
        val isAllButton = amount == -1L
        val realAmount = if (isAllButton) balance else amount

        val name = if (isAllButton) cmp("Alles abheben", NamedTextColor.RED, true) else cmp("$realAmount Dias abheben", NamedTextColor.GREEN, true)
        val lore = mutableListOf<Component>()

        if (balance < realAmount) {
            lore.add(cmp("Nicht genügend Guthaben.", NamedTextColor.RED))
        } else {
            lore.add(cmp("Klicken zum Abheben.", NamedTextColor.GRAY))
        }

        if (isAllButton) {
            lore.add(0, cmp("Betrag: $realAmount Dias", NamedTextColor.YELLOW))
        }

        val item = createGuiItem(material, name, lore)

        if (!isAllButton && material != Material.RED_WOOL) {
            item.amount = min(realAmount, 64).toInt()
        }

        return item
    }
}