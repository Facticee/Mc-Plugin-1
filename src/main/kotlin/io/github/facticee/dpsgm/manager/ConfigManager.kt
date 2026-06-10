package io.github.facticee.dpsgm.manager

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

object ConfigManager {
    private lateinit var plugin: JavaPlugin
    private val configs = ConcurrentHashMap<String, Pair<File, YamlConfiguration>>()

    /** setup in onEnable: Plugin einmal setzen und config.yml preloaden */
    fun setup(plugin: JavaPlugin) {
        this.plugin = plugin
        if (!plugin.dataFolder.exists()) plugin.dataFolder.mkdirs()
        // Standard-config beim Start laden/erstellen
        getConfig("config.yml")
    }

    /** Holt eine Config (lädt/erstellt automatisch) */
    fun getConfig(fileName: String): YamlConfiguration {
        return configs.computeIfAbsent(fileName) {
            val file = File(plugin.dataFolder, fileName).apply {
                if (!exists()) {
                    val resource = plugin.getResource(fileName)
                    if (resource != null) {
                        plugin.saveResource(fileName, false)
                    } else {
                        createNewFile()
                    }
                }
            }
            val yaml = YamlConfiguration.loadConfiguration(file)
            file to yaml
        }.second
    }

    /** Speichert eine bestimmte Datei */
    fun save(fileName: String) {
        configs[fileName]?.let { (file, yaml) ->
            try {
                yaml.save(file)
            } catch (e: IOException) {
                plugin.logger.severe("Konnte ${file.name} nicht speichern: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /** Lädt eine bestimmte Datei neu */
    fun reload(fileName: String) {
        configs[fileName]?.let { (file, _) ->
            val yaml = YamlConfiguration.loadConfiguration(file)
            configs[fileName] = file to yaml
        }
    }
}