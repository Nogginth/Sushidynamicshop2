package mcsushi.dynamicshop.sushidynamicshop.premium.database;

import mcsushi.dynamicshop.sushidynamicshop.Sushidynamicshop;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.logging.Logger;
import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

public class DatabaseConfig {

    private final String type;
    private final String host;
    private final int port;
    private final String database;
    private final String user;
    private final String password;
    private final String file;

    public DatabaseConfig(FileConfiguration config, Logger logger) {
        this.type = config.getString("database.type", "litesql").toLowerCase();
        this.host = config.getString("database.host", "localhost");
        this.port = config.getInt("database.port", 3306);
        this.database = config.getString("database.database", "my_custom_currency_db");
        this.user = config.getString("database.user", "root");
        this.password = config.getString("database.password", "");
        this.file = new File(JavaPlugin.getProvidingPlugin(Sushidynamicshop.class).getDataFolder(), config.getString("database.file", "currency.db")).getAbsolutePath();

        // Debug Logging
        logger.info("[DatabaseConfig] Loaded configuration:");
        logger.info("Type: " + type);
        logger.info("Host: " + host);
        logger.info("Port: " + port);
        logger.info("Database: " + database);
        logger.info("User: " + user);
        logger.info("File: " + file);
    }

    public String getType() {
        return type;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getFile() {
        return file;
    }

    /**
     * สร้าง JDBC URL ตามประเภทของ Database
     */
    public String getJdbcUrl() {
        String typeLower = type.toLowerCase();

        if (typeLower.equals("mysql")) {
            return "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
        } else if (typeLower.equals("litesql")) {
            return "jdbc:sqlite:" + file;
        }

        throw new IllegalArgumentException("Unsupported database type: " + type);
    }
}
