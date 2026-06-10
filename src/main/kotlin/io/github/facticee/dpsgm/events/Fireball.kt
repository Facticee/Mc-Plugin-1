package io.github.facticee.dpsgm.events

import io.github.facticee.dpsgm.Main
import io.github.facticee.dpsgm.manager.FireballManager
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.LargeFireball
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import kotlin.math.max

object Fireball : Listener {

    // ---- Tuning (ändere diese Werte, wenn du andere Ergebnisse willst) ----
    private const val SPAWN_OFFSET = 0.5
    private const val FIREBALL_SPEED = 0.35
    private const val EXPLOSION_RADIUS = 4
    private const val HORIZONTAL_MULT = 0.9
    private const val VERTICAL_MULT = 0.6
    // --------------------------------------------------------------------

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!FireballManager.isEnabled) return
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.hand != EquipmentSlot.HAND) return

        val player = event.player
        val item = player.inventory.itemInMainHand
        if (item.type != Material.FIRE_CHARGE) return

        event.isCancelled = true

        if (player.gameMode != GameMode.CREATIVE && FireballManager.consumeInSurvival) {
            item.amount -= 1
        }

        launchGhastFireball(player)
    }

    private fun launchGhastFireball(player: Player) {
        val spawnLocation = player.eyeLocation.add(player.location.direction.multiply(SPAWN_OFFSET))
        val fireball = player.world.spawn(spawnLocation, LargeFireball::class.java)

        fireball.shooter = player
        fireball.velocity = player.location.direction.multiply(FIREBALL_SPEED)
        fireball.yield = 2.0f
        fireball.setIsIncendiary(FireballManager.destroyEnvironment)

        player.playSound(player.location, Sound.ENTITY_GHAST_SHOOT, 1.0f, 1.0f)

        val plugin = JavaPlugin.getPlugin(Main::class.java)
        val ticks = 20L * 13L // 20s

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            try {
                if (fireball.isValid && !fireball.isDead) {
                    fireball.remove()
                    Bukkit.getOnlinePlayers().filter { it.isOp }.forEach { op ->
                        if (FireballManager.debug) {
                            op.sendMessage("§7[Debug] Fireball destroyed after 20 seconds")
                        }
                    }
                }
            } catch (e: Exception) {
                Bukkit.getOnlinePlayers().filter { it.isOp }.forEach { op ->
                    if (FireballManager.debug) {
                        op.sendMessage("§7[Debug] Timeout task failed: ${e.message}")
                    }
                }
            }
        }, ticks)
    }

    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent) {
        val entity = event.entity
        if (entity !is LargeFireball || entity.shooter !is Player) return

        if (!FireballManager.destroyEnvironment) {
            event.blockList().clear()
        }

        entity.world.playSound(entity.location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f)

        val explosionLoc = entity.location
        val shooter = entity.shooter as Player

        // Lokale Variable für Blickrichtungs-Bias
        val DIRECTION_BIAS = 0.22 // <-- AUCH ZUM JUSTIEREN DAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA SAGT CAHTGPT

        for (p in entity.world.players) {
            val distance = p.location.distance(explosionLoc)
            if (distance > EXPLOSION_RADIUS) continue

            val factor = 1.0 - (distance / EXPLOSION_RADIUS)

            val rawDir = p.location.toVector().subtract(explosionLoc.toVector()).setY(0.0)
            val rawDirNorm = if (rawDir.lengthSquared() < 0.0001) Vector(0.0, 0.0, 0.0) else rawDir.normalize()

            val playerForward = p.location.direction.setY(0.0)
            val playerForwardNorm = if (playerForward.lengthSquared() < 0.0001) Vector(0.0, 0.0, 0.0) else playerForward.normalize()

            val mixed = rawDirNorm.multiply(1.0 - DIRECTION_BIAS).add(playerForwardNorm.multiply(DIRECTION_BIAS))
            val finalDir = if (mixed.lengthSquared() < 0.0001) rawDirNorm else mixed.normalize()

            val horizontalStrength = HORIZONTAL_MULT * factor
            val verticalStrength = VERTICAL_MULT * factor + 0.2

            val knockback = Vector(
                finalDir.x * horizontalStrength,
                verticalStrength,
                finalDir.z * horizontalStrength
            )

            val newVel = p.velocity.add(knockback)
            newVel.y = max(newVel.y, 0.0)
            p.velocity = newVel

            if (FireballManager.velocitydebug && p.uniqueId == shooter.uniqueId) {
                shooter.sendMessage(
                    "§7[Debug] dist=${"%.2f".format(distance)} factor=${"%.2f".format(factor)} vert=${
                        "%.2f".format(verticalStrength)
                    } horiz=${"%.2f".format(horizontalStrength)} bias=${"%.2f".format(DIRECTION_BIAS)}"
                )
            }
        }
    }
}