package mcsushi.dynamicshop.sushidynamicshop.premium.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import mcsushi.dynamicshop.sushidynamicshop.Sushidynamicshop;
import mcsushi.dynamicshop.sushidynamicshop.util.PremiumChecker;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * DatabaseManager - จัดการการเชื่อมต่อกับฐานข้อมูลสำหรับฟีเจอร์พรีเมี่ยม
 * Phase 1: Initialization (init, shutdown)
 */
public class DatabaseManager {

    private static HikariDataSource dataSource;
    private static DatabaseConfig dbConfig;
    private static String jdbcUrl;
    private static final Logger logger = Sushidynamicshop.getInstance().getLogger();
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY = 2000;
    private static boolean debug;

    /**
     * เริ่มต้นการเชื่อมต่อกับฐานข้อมูล
     * - โหลด DatabaseConfig
     * - สร้าง HikariDataSource
     * - ตรวจสอบ Premium Status ก่อนการเชื่อมต่อ
     */
    public static void init() {
        if (!PremiumChecker.isPremium()) {
            logger.info("[DatabaseManager] Premium features are not enabled. Skipping database initialization.");
            return;
        }

        try {
            // โหลด DatabaseConfig
            dbConfig = new DatabaseConfig(Sushidynamicshop.getInstance().getConfig(), logger);
            jdbcUrl = dbConfig.getJdbcUrl();

            logger.info("[DatabaseManager] Initializing database connection with URL: " + jdbcUrl);

            // ตั้งค่า HikariConfig
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(jdbcUrl);
            hikariConfig.setUsername(dbConfig.getUser());
            hikariConfig.setPassword(dbConfig.getPassword());
            hikariConfig.setMaximumPoolSize(10);
            hikariConfig.setConnectionTimeout(30000);
            hikariConfig.setLeakDetectionThreshold(15000);
            hikariConfig.setAutoCommit(true);

            // สร้าง HikariDataSource
            dataSource = new HikariDataSource(hikariConfig);
            logger.info("[DatabaseManager] Database connection pool initialized successfully.");

            createTables();

        } catch (Exception e) {
            logger.warning("[DatabaseManager] Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
        debug = Sushidynamicshop.getInstance().getConfig().getBoolean("debug", false);
    }

    public static void createTables() {
        String playerCurrencySQL;
        String currencyListSQL;

        // ✅ MySQL Configuration
        if ("mysql".equalsIgnoreCase(dbConfig.getType())) {
            playerCurrencySQL = """
        CREATE TABLE IF NOT EXISTS player_currency (
            id INT AUTO_INCREMENT PRIMARY KEY,
            uuid VARCHAR(255) NOT NULL,
            currency_name VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL,
            amount DECIMAL(18, 2) DEFAULT 0.00,
            UNIQUE KEY (uuid, currency_name)
        );
        """;

            currencyListSQL = """
        CREATE TABLE IF NOT EXISTS currency_list (
            id INT AUTO_INCREMENT PRIMARY KEY,
            currency_id VARCHAR(255) COLLATE utf8mb4_unicode_ci NOT NULL UNIQUE,
            pronoun VARCHAR(255) NOT NULL
        );
        """;

        }
        // ✅ SQLite Configuration
        else {
            playerCurrencySQL = """
        CREATE TABLE IF NOT EXISTS player_currency (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            uuid TEXT NOT NULL,
            currency_name TEXT NOT NULL COLLATE NOCASE,
            amount REAL DEFAULT 0.0,
            UNIQUE(uuid, currency_name)
        );
        """;

            currencyListSQL = """
        CREATE TABLE IF NOT EXISTS currency_list (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            currency_id TEXT NOT NULL COLLATE NOCASE UNIQUE, -- ✅ ใช้ COLLATE NOCASE ใน currency_id
            pronoun TEXT NOT NULL
        );
        """;
        }

        try (Connection conn = getConnection()) {
            try (PreparedStatement stmt1 = conn.prepareStatement(playerCurrencySQL)) {
                stmt1.executeUpdate();
            }

            try (PreparedStatement stmt2 = conn.prepareStatement(currencyListSQL)) {
                stmt2.executeUpdate();
            }

            logger.info("[DatabaseManager] Tables 'player_currency' and 'currency_list' created/verified successfully.");

        } catch (SQLException e) {
            logger.warning("[DatabaseManager] Failed to create tables: " + e.getMessage());
        }
    }

    /**
     * ปิดการเชื่อมต่อกับฐานข้อมูล
     * - ตรวจสอบว่ามี Connection Pool หรือไม่
     * - หากมี ให้ปิด Connection Pool
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                dataSource.close();
                logDebug("[DatabaseManager] Database connection pool closed.");
            } catch (Exception e) {
                logDebug("[DatabaseManager] Error closing connection pool: " + e.getMessage());
            }
        }
    }


    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("[DatabaseManager] Database is not initialized or connection pool is closed.");
        }

        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            try {
                Connection conn = dataSource.getConnection();
                return conn;
            } catch (SQLException e) {
                attempt++;
                logger.warning("[DatabaseManager] Connection attempt " + attempt + " failed: " + e.getMessage());

                if (attempt >= MAX_RETRIES) {
                    throw new SQLException("[DatabaseManager] Failed to connect after " + MAX_RETRIES + " attempts.");
                }

                try {
                    Thread.sleep(RETRY_DELAY);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("[DatabaseManager] Connection retry interrupted.");
                }
            }
        }

        throw new SQLException("[DatabaseManager] Unable to establish a connection.");
    }

    /**
     * ตรวจสอบการเชื่อมต่อ
     *
     * @return true หากเชื่อมต่อได้, false หากไม่สามารถเชื่อมต่อได้
     */
    public static boolean isConnected() {
        try (Connection conn = getConnection()) {
            return !conn.isClosed();
        } catch (SQLException e) {
            logger.warning("[DatabaseManager] Connection check failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * เริ่ม Transaction
     *
     * @param conn Connection ที่ต้องการเริ่ม Transaction
     * @throws SQLException หากเกิดข้อผิดพลาด
     */
    public static void beginTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            try {
                conn.setAutoCommit(false);
                logger.info("[DatabaseManager] Transaction started.");
            } catch (SQLException e) {
                logger.warning("[DatabaseManager] Failed to begin transaction: " + e.getMessage());
                throw e;
            }
        }
    }

    /**
     * ยืนยัน Transaction
     *
     * @param conn Connection ที่ต้องการ Commit
     * @throws SQLException หากเกิดข้อผิดพลาด
     */
    public static void commitTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            try {
                conn.commit();
                conn.setAutoCommit(true);
                logger.info("[DatabaseManager] Transaction committed.");
            } catch (SQLException e) {
                logger.warning("[DatabaseManager] Failed to commit transaction: " + e.getMessage());
                rollbackTransaction(conn);
                throw e;
            }
        }
    }

    /**
     * ยกเลิก Transaction
     *
     * @param conn Connection ที่ต้องการ Rollback
     */
    public static void rollbackTransaction(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
                logger.info("[DatabaseManager] Transaction rolled back.");
            } catch (SQLException e) {
                logger.warning("[DatabaseManager] Failed to rollback transaction: " + e.getMessage());
            }
        }
    }

    /**
     * Execute Query (SELECT)
     *
     * @param sql      SQL Query ที่ต้องการรัน
     * @param consumer Lambda สำหรับเตรียมพารามิเตอร์
     * @param handler  Lambda สำหรับจัดการผลลัพธ์
     */
    public static void executeQuery(String sql, SQLConsumer<PreparedStatement> consumer, SQLConsumer<ResultSet> handler) {
        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                consumer.accept(stmt);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        handler.accept(rs);
                    }
                }

                return;

            } catch (SQLException e) {
                attempt++;
                logger.warning("[DatabaseManager] Query attempt " + attempt + " failed: " + e.getMessage());
                if (attempt >= MAX_RETRIES) break;

                try {
                    Thread.sleep(RETRY_DELAY);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    /**
     * Execute Update (INSERT, UPDATE, DELETE)
     *
     * @param sql      SQL Query ที่ต้องการรัน
     * @param consumer Lambda สำหรับเตรียมพารามิเตอร์
     */
    public static void executeUpdate(String sql, SQLConsumer<PreparedStatement> consumer) {
        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                consumer.accept(stmt);
                stmt.executeUpdate();
                logger.info("[DatabaseManager] Update executed successfully.");
                return;

            } catch (SQLException e) {
                attempt++;
                logger.warning("[DatabaseManager] Update attempt " + attempt + " failed: " + e.getMessage());
                if (attempt >= MAX_RETRIES) break;

                try {
                    Thread.sleep(RETRY_DELAY);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    /**
     * Logging สำหรับ Debug
     *
     * @param message ข้อความที่ต้องการแสดง
     */
    public static void logDebug(String message) {
        if (debug) {
            logger.info("[DatabaseManager] " + message);
        }
    }

    /**
     * ตรวจสอบฐานข้อมูล (เฉพาะ MySQL)
     * - ตรวจสอบว่า Database ที่กำหนดใน config.yml มีอยู่จริงหรือไม่
     */
    public static void checkDatabase() {
        if (dbConfig == null) {
            logger.warning("[DatabaseManager] DatabaseConfig is not initialized.");
            return;
        }

        String type = dbConfig.getType();
        if (type == null || !type.equalsIgnoreCase("mysql")) {
            logDebug("Skipping database check for non-MySQL databases.");
            return;
        }

        String query = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?";
        executeQuery(query, stmt -> stmt.setString(1, dbConfig.getDatabase()), rs -> {
            if (!rs.next()) {
                logger.warning("[DatabaseManager] Database '" + dbConfig.getDatabase() + "' not found.");
            } else {
                logDebug("Database '" + dbConfig.getDatabase() + "' exists.");
            }
        });
    }

    public interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }

    public static String getDbType() {
        return dbConfig != null ? dbConfig.getType() : "litesql";
    }
}
