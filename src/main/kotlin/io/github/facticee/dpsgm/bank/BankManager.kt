package io.github.facticee.dpsgm.bank

import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object BankManager {

    private lateinit var plugin: JavaPlugin
    // CACHE: Speichert Balancen nur für ONLINE-Spieler im RAM (sehr schnell)
    private val balances = ConcurrentHashMap<UUID, Long>()

    private var connection: Connection? = null
    private const val DB_FILE_NAME = "economy.db"

    fun setup(plugin: JavaPlugin) {
        this.plugin = plugin
        connect()
        createTable()
    }

    fun shutdown() {
        // Speichert alle verbleibenden Balancen in die DB
        balances.keys.forEach { saveBalance(it) }
        connection?.close()
        plugin.logger.info("SQLite-Verbindung geschlossen und Balancen gespeichert.")
    }

    private fun connect() {
        try {
            // WICHTIG: Korrekter Import ist java.sql.Connection
            Class.forName("org.sqlite.JDBC")
            val dataFolder = plugin.dataFolder.path
            val dbFile = File(dataFolder, DB_FILE_NAME)
            connection = DriverManager.getConnection("jdbc:sqlite:$dbFile")
        } catch (e: Exception) {
            plugin.logger.severe("Konnte keine Verbindung zur Datenbank herstellen: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun createTable() {
        val sql = "CREATE TABLE IF NOT EXISTS player_balances (" +
                "uuid TEXT PRIMARY KEY," +
                "balance INTEGER NOT NULL DEFAULT 0);"
        try {
            connection?.createStatement()?.use { it.execute(sql) }
        } catch (e: Exception) {
            plugin.logger.severe("Fehler beim Erstellen der Tabelle: ${e.message}")
            e.printStackTrace()
        }
    }

    // --- Performance-Verbesserung: Lazy Loading / On-Demand Loading ---

    fun loadBalance(uuid: UUID) {
        if (balances.containsKey(uuid)) return

        val sql = "SELECT balance FROM player_balances WHERE uuid = ?;"
        try {
            connection?.prepareStatement(sql)?.use { stmt ->
                stmt.setString(1, uuid.toString())
                val rs = stmt.executeQuery()
                val balance = if (rs.next()) rs.getLong("balance") else 0L
                balances[uuid] = balance
            }
        } catch (e: Exception) {
            plugin.logger.severe("Fehler beim Laden der Bilanz für $uuid: ${e.message}")
            balances[uuid] = 0L
        }
    }

    fun saveBalance(uuid: UUID) {
        val balance = balances.remove(uuid) ?: return

        val sql = "INSERT INTO player_balances (uuid, balance) VALUES (?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET balance = excluded.balance;"
        try {
            connection?.prepareStatement(sql)?.use { stmt ->
                stmt.setString(1, uuid.toString())
                stmt.setLong(2, balance)
                stmt.executeUpdate()
            }
        } catch (e: Exception) {
            plugin.logger.severe("Fehler beim Speichern der Bilanz für $uuid: ${e.message}")
        }
    }

    // --- Bank-Operationen ---

    fun getBalance(uuid: UUID): Long {
        // Zuerst aus dem schnellen RAM-Cache lesen
        val cachedBalance = balances[uuid]
        if (cachedBalance != null) return cachedBalance

        // Wenn offline, direkt aus der DB lesen
        return getOfflineBalance(uuid)
    }

    fun deposit(uuid: UUID, amount: Long): Boolean {
        if (amount <= 0) return false

        // Für ONLINE-Spieler: Ändere den Wert im RAM
        if (balances.containsKey(uuid)) {
            val newBalance = balances.computeIfPresent(uuid) { _, current -> current + amount }
            return newBalance != null
        }

        // Für OFFLINE-Spieler: Direkte DB-Operation
        return updateBalanceInDb(uuid, amount)
    }

    fun withdraw(uuid: UUID, amount: Long): Boolean {
        if (amount <= 0) return false

        // Für ONLINE-Spieler: Prüfe/Ändere den Wert im RAM
        if (balances.containsKey(uuid)) {
            val currentBalance = balances[uuid] ?: 0L
            if (currentBalance < amount) return false
            balances.computeIfPresent(uuid) { _, current -> current - amount }
            return true
        }

        // Für OFFLINE-Spieler: Direkte DB-Operation
        val currentBalance = getOfflineBalance(uuid)
        if (currentBalance < amount) return false
        return updateBalanceInDb(uuid, -amount)
    }

    // --- Interne DB-Helfer ---

    private fun getOfflineBalance(uuid: UUID): Long {
        val sql = "SELECT balance FROM player_balances WHERE uuid = ?;"
        return try {
            connection?.prepareStatement(sql)?.use { stmt ->
                stmt.setString(1, uuid.toString())
                val rs = stmt.executeQuery()
                if (rs.next()) rs.getLong("balance") else 0L
            } ?: 0L
        } catch (e: Exception) {
            plugin.logger.severe("Fehler beim OFFLINE-Laden der Bilanz für $uuid: ${e.message}")
            0L
        }
    }

    private fun updateBalanceInDb(uuid: UUID, delta: Long): Boolean {
        // Transaktion, um sicherzustellen, dass die Operation atomar ist
        val updateSql = "UPDATE player_balances SET balance = balance + ? WHERE uuid = ?;"
        val insertSql = "INSERT OR IGNORE INTO player_balances (uuid, balance) VALUES (?, ?);"

        return try {
            connection?.autoCommit = false

            connection?.prepareStatement(updateSql)?.use { stmt ->
                stmt.setLong(1, delta)
                stmt.setString(2, uuid.toString())
                if (stmt.executeUpdate() == 0) {
                    connection?.prepareStatement(insertSql)?.use { insertStmt ->
                        insertStmt.setString(1, uuid.toString())
                        insertStmt.setLong(2, delta)
                        insertStmt.executeUpdate()
                    }
                }
            }
            connection?.commit()
            connection?.autoCommit = true
            true
        } catch (e: Exception) {
            plugin.logger.severe("Fehler bei der DB-Transaktion für $uuid: ${e.message}")
            connection?.rollback()
            connection?.autoCommit = true
            false
        }
    }
}