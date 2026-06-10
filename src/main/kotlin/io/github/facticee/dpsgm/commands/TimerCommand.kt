package io.github.facticee.dpsgm.commands

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.textArgument
import io.github.facticee.dpsgm.general.Timer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class TimerCommand {
    init {
        commandTree("timer") {

            literalArgument("show") {
                anyExecutor { sender, _ ->
                    Timer.visible = true
                    sender.sendMessage(cmp("Timer wird angezeigt", NamedTextColor.GRAY))
                }
            }

            literalArgument("hide") {
                anyExecutor { sender, _ ->
                    Timer.visible = false
                    sender.sendMessage(cmp("Timer wird versteckt", NamedTextColor.GRAY))
                }
            }

            literalArgument("start") {
                anyExecutor { sender, _ ->
                    Timer.start()
                    sender.sendMessage(cmp("Timer gestartet", NamedTextColor.GREEN, true))
                }
            }

            literalArgument("pause") {
                anyExecutor { sender, _ ->
                    Timer.pause()
                    sender.sendMessage(cmp("Timer pausiert", NamedTextColor.RED, true))
                }
            }

            literalArgument("reset") {
                anyExecutor { sender, _ ->
                    Timer.reset()
                    sender.sendMessage(cmp("Timer zurückgesetzt", NamedTextColor.GREEN, true))
                }
            }

            literalArgument("confirm") {
                anyExecutor { sender, _ ->
                    Timer.confirm()
                    sender.sendMessage(cmp("Timer gestartet", NamedTextColor.GREEN, true))
                }
            }

            literalArgument("countdown") {
                textArgument("time") {
                    anyExecutor { sender, args ->
                        val d = Timer.parseDuration(args[0] as String)
                        if (d == null || d.inWholeSeconds <= 0) {
                            sender.sendMessage(cmp("Ungültige Zeit!", NamedTextColor.RED))
                            return@anyExecutor
                        }
                        Timer.startCountdown(d)
                        sender.sendMessage(
                            cmp(
                                "Countdown gestartet: ${Timer.formatDuration(d)}",
                                NamedTextColor.GREEN,
                                true
                            )
                        )
                    }
                }
            }

            literalArgument("settime") {
                textArgument("time") {
                    textArgument("type") {
                        anyExecutor { sender, args ->
                            val d = Timer.parseDuration(args[0] as String)
                            val type = (args[1] as String).lowercase()
                            if (d == null || d.isNegative()) {
                                sender.sendMessage(cmp("Ungültige Zeit!", NamedTextColor.RED))
                                return@anyExecutor
                            }
                            val isCountdown = when (type) {
                                "countdown", "down" -> true
                                "timer", "up" -> false
                                else -> {
                                    sender.sendMessage(cmp("Gib 'countdown' oder 'timer' an.", NamedTextColor.RED))
                                    return@anyExecutor
                                }
                            }
                            Timer.setTimeIdle(d, isCountdown)
                            sender.sendMessage(
                                cmp(
                                    "Timer Idle gesetzt: ${Timer.formatDuration(d)} — /timer confirm zum Start",
                                    NamedTextColor.GRAY
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Hilfsfunktion cmp ---
    private fun cmp(text: String, color: NamedTextColor? = null, bold: Boolean = false): Component {
        var comp = Component.text(text)
        if (color != null) comp = comp.color(color)
        if (bold) comp = comp.decorate(TextDecoration.BOLD)
        return comp
    }
}