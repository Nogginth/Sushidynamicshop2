package mcsushi.dynamicshop.sushidynamicshop.premium.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;
import mcsushi.dynamicshop.sushidynamicshop.Sushidynamicshop;

public class CurrencyDAO {

    private static final Logger logger = Sushidynamicshop.getInstance().getLogger();

    /**
     * เพิ่มจำนวนเงินให้ผู้เล่น
     */
    public static void addCurrency(UUID uuid, String currency, double amount) {
        String sql = "INSERT INTO player_currency (uuid, currency_name, amount) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT(uuid, currency_name) DO UPDATE SET amount = amount + excluded.amount";

        executeUpdate(sql, uuid.toString(), currency, amount);
    }

    /**
     * ดึงข้อมูลจำนวนเงินของผู้เล่น
     */
    public static double getCurrency(UUID uuid, String currency) {
        String sql = "SELECT amount FROM player_currency WHERE uuid = ? AND currency_name = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uuid.toString());
            stmt.setString(2, currency);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("amount");
                }
            }

        } catch (SQLException e) {
            logger.warning("[CurrencyDAO] Failed to get currency: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * อัปเดตจำนวนเงินของผู้เล่น
     */
    public static void updateCurrency(UUID uuid, String currency, double amount) {
        String sql = "UPDATE player_currency SET amount = ? WHERE uuid = ? AND currency_name = ?";
        executeUpdate(sql, amount, uuid.toString(), currency);
    }

    /**
     * ลบจำนวนเงินของผู้เล่น
     */
    public static void removeCurrency(UUID uuid, String currency, double amount) {
        String sql = "UPDATE player_currency SET amount = amount - ? WHERE uuid = ? AND currency_name = ?";
        executeUpdate(sql, amount, uuid.toString(), currency);
    }

    /**
     * โอนเงินระหว่างผู้เล่นสองคน (Transaction-based)
     */
    public static void transferCurrency(UUID from, UUID to, String currency, double amount) {
        String withdrawSQL = "UPDATE player_currency SET amount = amount - ? WHERE uuid = ? AND currency_name = ?";
        String depositSQL = "UPDATE player_currency SET amount = amount + ? WHERE uuid = ? AND currency_name = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            DatabaseManager.beginTransaction(conn);

            try (PreparedStatement withdrawStmt = conn.prepareStatement(withdrawSQL);
                 PreparedStatement depositStmt = conn.prepareStatement(depositSQL)) {

                // หักเงินจากผู้เล่นต้นทาง
                withdrawStmt.setDouble(1, amount);
                withdrawStmt.setString(2, from.toString());
                withdrawStmt.setString(3, currency);
                withdrawStmt.executeUpdate();

                // เพิ่มเงินให้ผู้เล่นปลายทาง
                depositStmt.setDouble(1, amount);
                depositStmt.setString(2, to.toString());
                depositStmt.setString(3, currency);
                depositStmt.executeUpdate();

                // ยืนยัน Transaction
                DatabaseManager.commitTransaction(conn);

            } catch (SQLException e) {
                // ยกเลิก Transaction หากเกิดข้อผิดพลาด
                DatabaseManager.rollbackTransaction(conn);
                logger.warning("[CurrencyDAO] Transfer failed: " + e.getMessage());
            }

        } catch (SQLException e) {
            logger.warning("[CurrencyDAO] Database error during transfer: " + e.getMessage());
        }
    }


    /**
     * ลบข้อมูลผู้เล่นทั้งหมด (ใช้ในกรณี Reset หรือ Account Deletion)
     */
    public static void deleteCurrency(UUID uuid) {
        String sql = "DELETE FROM player_currency WHERE uuid = ?";
        executeUpdate(sql, uuid.toString());
    }

    /**
     * ลบข้อมูลสกุลเงินทั้งหมด (ใช้ในกรณีลบ Currency Type ออกจากระบบ)
     */
    public static void deleteCurrencyType(String currency) {
        String sql = "DELETE FROM player_currency WHERE currency_name = ?";
        executeUpdate(sql, currency);
    }

    /**
     * ฟังก์ชันสำหรับ Execute Update (INSERT, UPDATE, DELETE)
     */
    private static void executeUpdate(String sql, Object... params) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.warning("[CurrencyDAO] SQL Error: " + e.getMessage());
        }
    }
}
