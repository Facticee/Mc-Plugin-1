package io.github.facticee.dpsgm.superpower

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.persistence.PersistentDataType

/**
 * Definiert das Item für die Chaos-Superkraft.
 */
object ChaosItem {

    // Eindeutiger Schlüssel, um unser Item zu erkennen
    val CHAOS_ITEM_KEY = NamespacedKey("chaositem", "key")
    val CHAOS_PROJECTILE_KEY = NamespacedKey("chaositem", "projectile_key")

    fun createItem(): ItemStack {
        // Ersetze kpaper itemStack
        val item = ItemStack(Material.NETHER_STAR)

        item.itemMeta = item.itemMeta.apply {
            // Ersetze name = cmp()
            displayName(LegacyComponentSerializer.legacySection().deserialize("§c§lChaos-Fragment"))

            // Ersetze lore { ... }
            lore(
                listOf(
                    LegacyComponentSerializer.legacySection().deserialize("§7Wirf mich, um die Welt neu zu formen...")
                )
            )

            // Wir "markieren" das Item mit einem PersistentDataContainer-Tag
            persistentDataContainer.set(CHAOS_ITEM_KEY, PersistentDataType.BYTE, 1)
        }
        return item
    }

    /**
     * Prüft, ob ein ItemStack unser Chaos-Item ist.
     */
    fun isChaosItem(item: ItemStack): Boolean {
        return item.itemMeta?.persistentDataContainer?.has(CHAOS_ITEM_KEY) ?: false
    }
}