package io.github.facticee.dpsgm.general

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object Timer {

    private lateinit var plugin: JavaPlugin
    private val miniMessages = MiniMessage.miniMessage()

    var time: Duration = Duration.ZERO
        private set
    var paused: Boolean = true
    var visible: Boolean = false
    private var countdown: Boolean = false
    private var idle: Boolean = false
    private var offset = 0.0

    fun init(plugin: JavaPlugin) {
        this.plugin = plugin
        schedule()
    }

    fun start() {
        paused = false
        idle = false
    }

    fun pause() { paused = true }

    fun reset() {
        time = Duration.ZERO
        paused = true
        idle = false
    }

    fun confirm() {
        if (idle) {
            paused = false
            idle = false
        }
    }

    fun startCountdown(d: Duration) {
        time = d
        countdown = true
        paused = false
        idle = false
    }

    fun setTimeIdle(d: Duration, isCountdown: Boolean) {
        time = d
        countdown = isCountdown
        paused = true
        idle = true
    }

    private fun displayTimer() {
        if (!visible) return
        val suffix = when {
            idle -> "Idle (${formatDuration(time)})"
            paused -> "Timer pausiert"
            else -> formatDuration(time)
        }
        val display = miniMessages.deserialize("<gradient:#5555ff:#55ffff:$offset><b>$suffix")
        Bukkit.getOnlinePlayers().forEach { it.sendActionBar(display) }
    }

    private fun schedule() {
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            offset -= 0.05
            if (offset < -1.0) offset += 2.0
            displayTimer()
        }, 0L, 1L)

        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            if (paused || idle) return@Runnable
            if (countdown) {
                time -= 1.seconds
                if (time.inWholeSeconds <= 0) {
                    time = Duration.ZERO
                    paused = true
                }
            } else {
                time += 1.seconds
            }
        }, 0L, 20L)
    }

    fun parseDuration(input: String): Duration? {
        val regex = """(\d+d)?(\d+h)?(\d+m)?(\d+s)?""".toRegex()
        val cleaned = input.replace("\\s+".toRegex(), "")
        val match = regex.matchEntire(cleaned) ?: return null
        val (d, h, m, s) = match.destructured
        return Duration.ZERO +
                (d.removeSuffix("d").toLongOrNull() ?: 0).days +
                (h.removeSuffix("h").toLongOrNull() ?: 0).hours +
                (m.removeSuffix("m").toLongOrNull() ?: 0).minutes +
                (s.removeSuffix("s").toLongOrNull() ?: 0).seconds
    }

    fun formatDuration(d: Duration): String {
        var secs = d.inWholeSeconds
        val sign = if (secs < 0) "-" else ""
        secs = abs(secs)
        val hrs = secs / 3600
        val mins = (secs % 3600) / 60
        val s = secs % 60
        return if (hrs > 0) "%s%d:%02d:%02d".format(sign, hrs, mins, s)
        else "%s%d:%02d".format(sign, mins, s)
    }
}