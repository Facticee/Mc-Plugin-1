package io.github.facticee.dpsgm.superpower

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import kotlin.random.Random

class ChaosListener(private val plugin: JavaPlugin) : Listener {

    // Konfigurierbar: Wie viele Blöcke von der Oberfläche entfernt werden sollen.
    // Standardwert: 1 (entfernt nur den obersten Block, z.B. Gras oder Erde)
    private val surfaceBlockCount = 1

    // NEU: Steuerung der Wurfhöhe (Vertikale Kraft)
    private val launchHeightMin = 1.0
    private val launchHeightMax = 1.9

    // NEU: Steuerung der horizontalen Streuung (Spread)
    // Ein höherer Wert führt dazu, dass Blöcke weiter weggeschleudert werden.
    private val horizontalSpread = 1.0 //0.35 auch gut

    // Materialien, die ignoriert werden sollen (wie vom Benutzer gewünscht)
    private val forbiddenMaterials = setOf(
        Material.WATER,
        Material.LAVA,
        Material.BEDROCK,
        Material.REINFORCED_DEEPSLATE,
        Material.END_PORTAL_FRAME,
        Material.BARRIER
    )

    // Der Radius des Effekts wurde auf 15 Blöcke reduziert
    private val radius = 15

    // 1. Event: Spieler wirft das Item
    @EventHandler
    fun onPlayerInteract(it: PlayerInteractEvent) {
        val item = it.item ?: return

        // Prüfe auf Rechtsklick (kann Action.RIGHT_CLICK_AIR oder Action.RIGHT_CLICK_BLOCK sein)
        if (it.action != Action.RIGHT_CLICK_AIR && it.action != Action.RIGHT_CLICK_BLOCK) return

        // Prüfe, ob es unser markiertes Item ist
        if (!ChaosItem.isChaosItem(item)) return

        val player = it.player

        it.isCancelled = true

        // Item-Menge reduzieren
        item.subtract(1)

        // Wir werfen einen "Schneeball" als Projektil
        val projectile = player.launchProjectile(Snowball::class.java).apply {
            // Wir "markieren" das Projektil
            persistentDataContainer.set(ChaosItem.CHAOS_PROJECTILE_KEY, PersistentDataType.BYTE, 1)
            velocity = player.location.direction.multiply(1.8) // Etwas schneller als normal
        }
    }

    // 2. Event: Projektil trifft auf
    @EventHandler
    fun onProjectileHit(it: ProjectileHitEvent) {
        val projectile = it.entity
        // Prüfen, ob es unser markierter Schneeball ist
        if (projectile !is Snowball || !projectile.persistentDataContainer.has(ChaosItem.CHAOS_PROJECTILE_KEY)) {
            return
        }

        // Sicherstellen, dass der Werfer ein Spieler war
        val player = projectile.shooter as? Player ?: return

        // Den Aufprallort holen (zentraler Punkt des Effekts)
        val impactLocation = it.hitBlock?.location ?: it.hitEntity?.location ?: projectile.location
        val world = impactLocation.world

        // --- DER EFFEKT BEGINNT ---

        // 1. Sound abspielen (Dumpfes Knacken) und Blitz hinzufügen
        world.strikeLightningEffect(impactLocation) // Blitzschlag (bleibt, für visuelles Feedback)

        // SOUNDS: Epische und krachende Sounds für den Aufprall (Wiederhergestellt!)

        // Tiefer, immersiver "KRACK" (Warden Death Sound, sehr laut und tief)
        world.playSound(impactLocation, Sound.ENTITY_WARDEN_DEATH, 8.5f, 0.4f)
        world.playSound(impactLocation, Sound.BLOCK_DEEPSLATE_BREAK, 3.5f, 0.4f)
        world.playSound(impactLocation, Sound.ENTITY_WARDEN_SONIC_CHARGE, 10f, 0.4f)
        world.playSound(impactLocation, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 5.0f, 0.1f)

        val blocksToLaunch = mutableMapOf<Block, Vector>()

        val startX = impactLocation.blockX
        val startZ = impactLocation.blockZ
        val impactY = impactLocation.blockY

        // Die maximale Höhe für die Suche (vermeidet unnötige Iterationen in hohen Welten)
        val worldMaxHeight = world.maxHeight

        // Iteriere nur über die X- und Z-Achse (Zylinderform)
        for (x in -radius..radius) {
            for (z in -radius..radius) {

                // Horizontaler Radius-Check (Kreisform)
                if (x * x + z * z > radius * radius) continue

                val targetX = startX + x
                val targetZ = startZ + z

                // --- NEUE LOGIK: FINDE DEN OBERFLÄCHENBLOCK ---
                var surfaceBlock: Block? = null

                // Wir suchen von der Welt-Max-Höhe nach unten (für Oberflächeneffekte)
                // ODER von 2 Blöcken über dem Aufschlagpunkt (für Höhleneffekte)
                val startY = if (world.getBlockAt(targetX, worldMaxHeight - 1, targetZ).type.isAir) {
                    worldMaxHeight - 1
                } else {
                    impactY + 2 // Falls der Aufschlag in einem geschlossenen Raum ist
                }

                // Scanne von oben nach unten (oder vom Aufschlagpunkt nach oben, falls unterirdisch)
                for (y in startY downTo world.minHeight) {
                    val block = world.getBlockAt(targetX, y, targetZ)

                    // Wenn wir einen Block finden, der NICHT Luft/verboten/flüssig ist, ist das unser Oberflächenblock
                    if (!block.type.isAir && block.type !in forbiddenMaterials && !block.isLiquid) {
                        surfaceBlock = block
                        break // Wir haben die Oberfläche gefunden, beende die y-Suche
                    }
                }

                // Wenn kein gültiger Oberflächenblock gefunden wurde, weiter zur nächsten X/Z-Koordinate
                if (surfaceBlock == null) continue

                // --- OBERFLÄCHENSCHICHT ABHEBEN (DYNAMISCHE ANZAHL) ---

                // Die Schleife geht von 0 (Oberfläche) bis zur negativen Anzahl der Blöcke
                for (yOffset in 0 downTo -(surfaceBlockCount - 1)) {
                    val block = surfaceBlock.getRelative(0, yOffset, 0)

                    if (block.type.isAir || block.type in forbiddenMaterials || block.isLiquid) continue

                    // --- VEKTOR-BERECHNUNG ---

                    // 1. Horizontale Richtung: Völlig zufällig, verwendet horizontalSpread
                    val randomChaos = Vector(
                        Random.nextDouble(-horizontalSpread, horizontalSpread),
                        0.0,
                        Random.nextDouble(-horizontalSpread, horizontalSpread)
                    )

                    var vector = randomChaos

                    // 2. Uniforme Höhe (Starker vertikaler Lift, verwendet launchHeightMin/Max)
                    vector.y = Random.nextDouble(launchHeightMin, launchHeightMax)

                    blocksToLaunch[block] = vector
                }
            }
        }

        // 3. Blöcke starten (als FallingBlock-Entitäten)
        blocksToLaunch.forEach { (block, vector) ->
            val blockData = block.blockData
            val blockLocation = block.location.toCenterLocation() // Wichtig: Zentriert starten

            // Originalblock entfernen
            block.type = Material.AIR

            // Fallenden Block spawnen
            val fallingBlock = world.spawnFallingBlock(blockLocation, blockData)

            // Konfiguration des fallenden Blocks
            fallingBlock.velocity = vector // Hier wird die berechnete Kraft angewendet!
            fallingBlock.dropItem = false // Soll sich selbst platzieren, nicht als Item droppen
            fallingBlock.setHurtEntities(false) // Soll niemanden verletzen

            // WICHTIG: Markiere den FallingBlock, damit wir ihn beim Landen erkennen können
            fallingBlock.persistentDataContainer.set(ChaosItem.CHAOS_PROJECTILE_KEY, PersistentDataType.BYTE, 1)

            // Sound beim Start des Blocks (Wiederhergestellt!)
            world.playSound(blockLocation, Sound.BLOCK_STONE_BREAK, 0.5f, 0.8f) // Leises Krack für jeden Block
        }
    }
}