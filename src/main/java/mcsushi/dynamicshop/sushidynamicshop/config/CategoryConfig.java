package mcsushi.dynamicshop.sushidynamicshop.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class CategoryConfig {
    private static FileConfiguration config;
    private static File file;

    public static void setup(JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), "category.yml");
        if (!file.exists()) {
            plugin.saveResource("category.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get() {
        return config;
    }

    public static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static boolean hasCategory(String id) {
        return config.contains("categories." + id);
    }

    public static void createCategory(String id) {
        config.createSection("categories." + id);
    }

    public static Set<String> getCategoryIds() {
        ConfigurationSection section = config.getConfigurationSection("categories");
        return section != null ? section.getKeys(false) : java.util.Collections.emptySet();
    }

    public static String getCategoryIdBySlot(int slot) {
        ConfigurationSection categories = config.getConfigurationSection("categories");
        if (categories == null) return null;

        for (String key : categories.getKeys(false)) {
            ConfigurationSection section = categories.getConfigurationSection(key);
            if (section == null) continue;
            if (section.getInt("slot", -1) == slot) {
                return key;
            }
        }
        return null;
    }

    // ✅ เพิ่ม method เพื่อดูว่า slot นี้ชี้ไปยัง shop ไหน
    public static String getShopIdBySlot(int slot) {
        ConfigurationSection categories = config.getConfigurationSection("categories");
        if (categories == null) return null;

        for (String key : categories.getKeys(false)) {
            ConfigurationSection section = categories.getConfigurationSection(key);
            if (section == null) continue;
            if (section.getInt("slot", -1) == slot) {
                return section.getString("shopid", key); // fallback เป็น key เอง
            }
        }
        return null;
    }

    public static String getPermissionBySlot(int slot) {
        ConfigurationSection categories = config.getConfigurationSection("categories");

        if (categories == null) {
            Bukkit.getLogger().warning("[Sushidynamicshop] Category not found in category.yml");
            return "";
        }

        boolean categoryFound = false;

        for (String key : categories.getKeys(false)) {
            ConfigurationSection section = categories.getConfigurationSection(key);
            if (section == null) continue;

            categoryFound = true;

            if (!section.contains("slot")) {
                Bukkit.getLogger().warning("[Sushidynamicshop] Category slot not found in \"" + key + "\"");
                continue;
            }

            int configSlot = section.getInt("slot", -1);
            if (configSlot == slot) {
                String permission = section.getString("permission", "");

                // หาก permission เป็นค่าว่างเปล่า ให้ถือว่าไม่มีการเช็ค permission
                if (permission.isEmpty()) {
                    return "";
                }

                return permission;
            }
        }

        if (!categoryFound) {
            Bukkit.getLogger().warning("[Sushidynamicshop] No valid categories found in category.yml");
        }

        return "";
    }
}