package mcsushi.dynamicshop.sushidynamicshop.premium.customcurrency;

import mcsushi.dynamicshop.sushidynamicshop.premium.database.DatabaseManager;
import mcsushi.dynamicshop.sushidynamicshop.util.CurrencyRegistry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import java.util.UUID;

public class CurrencyRegistryImpl implements CurrencyRegistry {

    private final Logger logger = Bukkit.getLogger();

    @Override
    public boolean addCurrency(String currencyId, String pronoun) {
        currencyId = currencyId.toLowerCase();
        String dbType = DatabaseManager.getDbType();
        String sql;

        if (dbType.equalsIgnoreCase("mysql")) {
            sql = """
        INSERT INTO currency_list (currency_id, pronoun) 
        VALUES (?, ?)
        ON DUPLICATE KEY UPDATE pronoun = VALUES(pronoun);
        """;
        } else {
            sql = """
        INSERT INTO currency_list (currency_id, pronoun) 
        VALUES (?, ?)
        ON CONFLICT(currency_id) DO UPDATE SET pronoun = excluded.pronoun;
        """;
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, currencyId);
            stmt.setString(2, pronoun);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            logger.warning("[CurrencyRegistry] Failed to add currency: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean removeCurrency(String currencyId) {
        currencyId = currencyId.toLowerCase();
        String sqlDeleteCurrency = "DELETE FROM currency_list WHERE LOWER(currency_id) = ?";
        String sqlDeletePlayerData = "DELETE FROM player_currency WHERE LOWER(currency_name) = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            // ลบข้อมูลใน player_currency
            try (PreparedStatement stmt = conn.prepareStatement(sqlDeletePlayerData)) {
                stmt.setString(1, currencyId);
                int affectedRows = stmt.executeUpdate();
                org.bukkit.Bukkit.getLogger().info("[DEBUG] Removed " + affectedRows + " records from player_currency for '" + currencyId + "'");
            }

            // ลบข้อมูลใน currency_list
            try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteCurrency)) {
                stmt.setString(1, currencyId);
                int affectedRows = stmt.executeUpdate();
                org.bukkit.Bukkit.getLogger().info("[DEBUG] Removed currency '" + currencyId + "' from currency_list with " + affectedRows + " affected rows.");
                return affectedRows > 0;
            }

        } catch (SQLException e) {
            org.bukkit.Bukkit.getLogger().warning("[CurrencyRegistry] Failed to remove currency '" + currencyId + "': " + e.getMessage());
        }

        return false;
    }

    @Override
    public boolean updateCurrency(String currencyId, String newPronoun) {
        currencyId = currencyId.toLowerCase();
        String sql = "UPDATE currency_list SET pronoun = ? WHERE LOWER(currency_id) = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPronoun);
            stmt.setString(2, currencyId);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            logger.warning("[CurrencyRegistry] Failed to update currency: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean adjustPlayerBalance(UUID playerUUID, String currencyId, double amount, String action) {
        currencyId = currencyId.toLowerCase();
        String sqlSelect = "SELECT amount FROM player_currency WHERE uuid = ? AND LOWER(currency_name) = ?";
        String sqlInsert = "INSERT INTO player_currency (uuid, currency_name, amount) VALUES (?, ?, ?)";
        String sqlUpdate = "UPDATE player_currency SET amount = ? WHERE uuid = ? AND LOWER(currency_name) = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(sqlSelect)) {

            selectStmt.setString(1, playerUUID.toString());
            selectStmt.setString(2, currencyId);

            try (ResultSet rs = selectStmt.executeQuery()) {
                double currentBalance = 0.0;
                boolean isNewRecord = false;

                if (rs.next()) {
                    currentBalance = rs.getDouble("amount");
                    org.bukkit.Bukkit.getLogger().info("[DEBUG] Current balance for '" + currencyId + "' is: " + currentBalance);
                } else {
                    org.bukkit.Bukkit.getLogger().info("[DEBUG] No balance record found for '" + currencyId + "'. Creating new record...");
                    isNewRecord = true;
                }

                double newBalance;
                if ("set".equalsIgnoreCase(action)) {
                    newBalance = amount;
                    org.bukkit.Bukkit.getLogger().info("[DEBUG] Setting new balance for '" + currencyId + "' to: " + newBalance);
                } else {
                    newBalance = action.equalsIgnoreCase("give") ? currentBalance + amount : currentBalance - amount;
                    newBalance = Math.max(newBalance, 0.0); // Prevent negative balance
                }

                if (isNewRecord) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(sqlInsert)) {
                        insertStmt.setString(1, playerUUID.toString());
                        insertStmt.setString(2, currencyId);
                        insertStmt.setDouble(3, newBalance);
                        insertStmt.executeUpdate();
                    }
                    org.bukkit.Bukkit.getLogger().info("[DEBUG] New record created with balance: " + newBalance);
                } else {
                    try (PreparedStatement updateStmt = conn.prepareStatement(sqlUpdate)) {
                        updateStmt.setDouble(1, newBalance);
                        updateStmt.setString(2, playerUUID.toString());
                        updateStmt.setString(3, currencyId);
                        updateStmt.executeUpdate();
                    }
                    org.bukkit.Bukkit.getLogger().info("[DEBUG] Updated balance for '" + currencyId + "' to: " + newBalance);
                }

                return true;

            }

        } catch (SQLException e) {
            org.bukkit.Bukkit.getLogger().warning("[CurrencyRegistry] Failed to adjust balance for '" + currencyId + "': " + e.getMessage());
        }

        return false;
    }

    @Override
    public String getPlayerBalance(UUID playerUUID, String currencyId, String defaultValue) {
        currencyId = currencyId.toLowerCase();
        String sql = "SELECT amount FROM player_currency WHERE uuid = ? AND LOWER(currency_name) = ?";
        String balanceStr = "0.0"; // หากไม่พบ record จะถือว่า balance เป็น 0.0

        org.bukkit.Bukkit.getLogger().info("[DEBUG] Retrieving balance for UUID: " + playerUUID + ", Currency: " + currencyId);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, currencyId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    balanceStr = rs.getString("amount");
                    org.bukkit.Bukkit.getLogger().info("[DEBUG] Raw database balance for '" + currencyId + "': " + balanceStr);
                } else {
                    org.bukkit.Bukkit.getLogger().info("[DEBUG] No balance record found for '" + currencyId + "'. Defaulting to 0.0");
                }
            }

        } catch (SQLException e) {
            org.bukkit.Bukkit.getLogger().warning("[CurrencyRegistry] Failed to retrieve balance for '" + currencyId + "': " + e.getMessage());
        }

        return balanceStr;
    }

    @Override
    public List<String> getAllCurrencies() {
        String sql = "SELECT LOWER(currency_id) AS currency_id FROM currency_list";
        List<String> currencies = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                currencies.add(rs.getString("currency_id"));
            }

        } catch (SQLException e) {
            logger.warning("[CurrencyRegistry] Failed to get all currencies: " + e.getMessage());
        }
        return currencies;
    }

    @Override
    public String getPronoun(String currencyId) {
        currencyId = currencyId.toLowerCase();
        String sql = "SELECT pronoun FROM currency_list WHERE LOWER(currency_id) = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, currencyId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String pronoun = rs.getString("pronoun");
                    return pronoun != null ? pronoun : currencyId;
                }
            }

        } catch (SQLException e) {
            logger.warning("[CurrencyRegistry] Failed to get pronoun: " + e.getMessage());
        }
        return currencyId;
    }

    @Override
    public boolean currencyExists(String currencyId) {
        currencyId = currencyId.toLowerCase();
        String sql = "SELECT 1 FROM currency_list WHERE LOWER(currency_id) = ? LIMIT 1";

        org.bukkit.Bukkit.getLogger().info("[DEBUG] Checking currency existence in database: " + currencyId);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, currencyId);

            try (ResultSet rs = stmt.executeQuery()) {
                boolean found = rs.next();
                org.bukkit.Bukkit.getLogger().info("[DEBUG] Currency found: " + found);
                return found;
            }

        } catch (SQLException e) {
            logger.warning("[CurrencyRegistry] Failed to check currency existence: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<String> getTop10Players(String currencyId, String pronoun) {
        currencyId = currencyId.toLowerCase(); // ✅ แปลงเป็น lowercase
        String sql = "SELECT uuid, amount FROM player_currency WHERE LOWER(currency_name) = ? ORDER BY amount DESC LIMIT 10";
        List<String> topPlayers = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, currencyId);

            try (ResultSet rs = stmt.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    UUID playerUUID = UUID.fromString(rs.getString("uuid"));
                    double amount = rs.getDouble("amount");

                    OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
                    String playerName = player.getName() != null ? player.getName() : "Unknown";

                    topPlayers.add("§7" + rank + ". §e" + playerName + " §7- §a" + amount + " §6" + pronoun);
                    rank++;
                }
            }

        } catch (SQLException e) {
            logger.warning("[CurrencyRegistry] Failed to get top players: " + e.getMessage());
        }

        if (topPlayers.isEmpty()) {
            topPlayers.add("§7No players found for this currency.");
        }

        return topPlayers;
    }

}
