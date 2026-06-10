package io.github.facticee.dpsgm.sortinv

import io.github.facticee.dpsgm.manager.ConfigManager
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.ConcurrentHashMap

enum class SortMode {
    MY_ITEMS, ALL_ITEMS
}

object SortManager {

    private const val DATA_FILE = "sortdata.yml"

    // Thread-sichere Maps zur Speicherung der Spielerdaten im RAM
    private val playerModes = ConcurrentHashMap<UUID, SortMode>()
    private val playerTemplates = ConcurrentHashMap<UUID, Array<ItemStack?>>()

    // Hier wird die Priorität für den allitems-Modus definiert (höherer Wert = höhere Prio)
    private val materialPriority = mapOf(
        // Werkzeuge/Waffen/Rüstung
        Material.NETHERITE_SWORD to 100, Material.NETHERITE_PICKAXE to 100, Material.NETHERITE_AXE to 100, /* ... alle Netherite Items */
        Material.DIAMOND_SWORD to 90, Material.DIAMOND_PICKAXE to 90, /* ... alle Diamond Items */
        Material.IRON_SWORD to 80, /* ... */
        // Erze & Blöcke
        Material.NETHERITE_BLOCK to 95, Material.DIAMOND_BLOCK to 85,
        // Sonstiges
        Material.TOTEM_OF_UNDYING to 110, Material.ENCHANTED_GOLDEN_APPLE to 105
        // Fülle diese Liste nach Belieben auf. Items, die nicht hier sind, erhalten Prio 0.
    )

    fun setup() {
        loadAllData()
    }

    // SETTER & GETTER
    fun getPlayerMode(uuid: UUID): SortMode = playerModes.getOrDefault(uuid, SortMode.ALL_ITEMS)
    fun setPlayerMode(uuid: UUID, mode: SortMode) {
        playerModes[uuid] = mode
    }
    fun getPlayerTemplate(uuid: UUID): Array<ItemStack?> = playerTemplates.getOrDefault(uuid, Array(36) { null })
    fun setPlayerTemplate(uuid: UUID, template: Array<ItemStack?>) {
        playerTemplates[uuid] = template
    }


    // Haupt-Sortierfunktion, wird vom Listener aufgerufen
    fun sortInventory(player: Player) {
        when (getPlayerMode(player.uniqueId)) {
            SortMode.MY_ITEMS -> sortMyItems(player)
            SortMode.ALL_ITEMS -> sortAllItems(player)
        }
    }

    // Logik für "myitems"
    private fun sortMyItems(player: Player) {
        val template = getPlayerTemplate(player.uniqueId)
        val playerInv = player.inventory
        val contents = (0..35).map { playerInv.getItem(it) }.toMutableList()
        val newContents = Array<ItemStack?>(36) { null }

        // Template anwenden
        for (i in template.indices) {
            val templateItem = template[i] ?: continue
            val matchIndex = contents.indexOfFirst { it != null && it.isSimilar(templateItem) }

            if (matchIndex != -1) {
                newContents[i] = contents.removeAt(matchIndex)
            }
        }

        // Verbleibende Items in leere Slots füllen
        contents.filterNotNull().forEach { remainingItem ->
            val emptySlot = newContents.indexOfFirst { it == null }
            if (emptySlot != -1) newContents[emptySlot] = remainingItem
        }

        (0..35).forEach { playerInv.setItem(it, newContents[it]) }
    }

    // Logik für "allitems"
    private fun sortAllItems(player: Player) {
        val playerInv = player.inventory
        val items = (0..35).mapNotNull { playerInv.getItem(it) }

        // Der performante Comparator
        val sortedItems = items.sortedWith(
            compareByDescending<ItemStack> { materialPriority.getOrDefault(it.type, 0) } // 1. Nach Material-Priorität
                .thenByDescending { it.enchantments.isNotEmpty() } // 2. Verzauberte Items zuerst
                .thenByDescending { it.amount } // 3. Nach Stack-Größe
                .thenBy { it.type.name } // 4. Alphabetisch als Fallback
        )

        val newContents = Array<ItemStack?>(36) { null }
        sortedItems.forEachIndexed { index, item -> newContents[index] = item }

        (0..35).forEach { playerInv.setItem(it, newContents[it]) }
    }

    // DATENPERSISTENZ
    fun loadAllData() {
        val config = ConfigManager.getConfig(DATA_FILE)
        config.getKeys(false).forEach { uuidString ->
            val uuid = UUID.fromString(uuidString)
            val mode = SortMode.valueOf(config.getString("$uuidString.mode", "ALL_ITEMS")!!)
            val templateItems = config.getList("$uuidString.template") as? List<ItemStack>
            val template = Array<ItemStack?>(36) { null }
            templateItems?.forEachIndexed { index, itemStack ->
                if (index < 36) template[index] = itemStack
            }
            playerModes[uuid] = mode
            playerTemplates[uuid] = template
        }
    }

    fun saveAllData() {
        val config = ConfigManager.getConfig(DATA_FILE)
        // Bestehende Daten löschen, um alte Spielerprofile zu entfernen (optional)
        config.getKeys(false).forEach { config.set(it, null) }

        playerModes.forEach { (uuid, mode) ->
            config.set("$uuid.mode", mode.name)
        }
        playerTemplates.forEach { (uuid, template) ->
            config.set("$uuid.template", template.toList())
        }
        ConfigManager.save(DATA_FILE)
    }
}