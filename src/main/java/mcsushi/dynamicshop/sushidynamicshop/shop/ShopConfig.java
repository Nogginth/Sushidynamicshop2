package mcsushi.dynamicshop.sushidynamicshop.shop;

import mcsushi.dynamicshop.sushidynamicshop.pricehandler.PriceHandler;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import mcsushi.dynamicshop.sushidynamicshop.init.PremiumInitializer;
import mcsushi.dynamicshop.sushidynamicshop.util.CurrencyRegistry;

import java.io.File;
import java.util.*;
import java.io.IOException;

public class ShopConfig {
    private static final Map<String, FileConfiguration> shopMap = new HashMap<>();
    public static void clearCache() {
        shopMap.clear();
    }

    public static void ensureDefaultShop(JavaPlugin plugin) {
        File folder = new File(plugin.getDataFolder(), "shop");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File defaultShop = new File(folder, "default.yml");
        if (!defaultShop.exists()) {
            plugin.saveResource("shop/default.yml", false);
        }
    }

    public static void loadAll(JavaPlugin plugin) {
        shopMap.clear();
        File folder = new File(plugin.getDataFolder(), "shop");
        if (!folder.exists()) {
            org.bukkit.Bukkit.getLogger().warning("[ShopConfig] Shop folder not found.");
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            org.bukkit.Bukkit.getLogger().warning("[ShopConfig] No shop files found.");
            return;
        }

        for (File file : files) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            String fileName = file.getName().replace(".yml", "");

            shopMap.put(fileName, config);

            ConfigurationSection shopcurrencySection = config.getConfigurationSection("shopcurrency");
            if (shopcurrencySection == null) {
                org.bukkit.Bukkit.getLogger().warning("[ShopConfig] 'shopcurrency' section is missing in shop '" + fileName + "'.");
            }
        }
    }

    public static FileConfiguration getShopConfig(String shopId) {
        FileConfiguration config = shopMap.get(shopId);

        if (config == null) {
            org.bukkit.Bukkit.getLogger().warning("[ShopConfig] Shop '" + shopId + "' configuration not found in shopMap.");
        }

        return config;
    }

    public static boolean hasShop(String shopId) {
        return shopMap.containsKey(shopId);
    }

    public static List<String> getShopItems(String shopId) {
        FileConfiguration config = shopMap.get(shopId);
        if (config == null) return Collections.emptyList();
        List<String> keys = new ArrayList<>();
        for (String key : config.getKeys(false)) {
            if (config.isConfigurationSection(key) && config.contains(key + ".source")) {
                keys.add(key);
            }
        }
        return keys;
    }

    public static String getSource(String shopId, String key) {
        return getSection(shopId, key).getString("source", "AIR");
    }

    public static int getSlot(String shopId, String key) {
        ConfigurationSection section = getSection(shopId, key);

        if (section == null) {
            org.bukkit.Bukkit.getLogger().info("[DEBUG] [ShopConfig - getSlot] Section not found for ShopID: " + shopId + ", Key: " + key);
            return 0;
        }

        int slot = section.getInt("slot", 0);

        org.bukkit.Bukkit.getLogger().info("[DEBUG] [ShopConfig - getSlot] ShopID: " + shopId + ", Key: " + key + ", Slot: " + slot);

        return slot;
    }

    public static double getCurrentPrice(String shopId, String key) {
        double raw = getSection(shopId, key).getDouble("current_price", 0);
        double min = getMinPrice(shopId, key);
        double max = getMaxPrice(shopId, key);
        return Math.max(min, Math.min(max, raw));
    }

    public static double getMinPrice(String shopId, String key) {
        return getSection(shopId, key).getDouble("min_price", 0);
    }

    public static double getMaxPrice(String shopId, String key) {
        return getSection(shopId, key).getDouble("max_price", 0);
    }

    public static double getSupply(String shopId, String key) {
        return getSection(shopId, key).getDouble("supply", 0);
    }

    public static boolean isRealSupply(String shopId, String key) {
        ConfigurationSection section = getSection(shopId, key);
        return section != null && section.getBoolean("real_supply", false);
    }

    public static double getDemand(String shopId, String key) {
        return getSection(shopId, key).getDouble("demand", 0);
    }

    public static double getPriceChangeRate(String shopId, String key) {
        return getSection(shopId, key).getDouble("price_change_rate", 0);
    }

    public static boolean canBuy(String shopId, String key) {
        return getSection(shopId, key).getBoolean("buy_enabled", true);
    }

    public static boolean canSell(String shopId, String key) {
        return getSection(shopId, key).getBoolean("sell_enabled", true);
    }

    public static double getBasePrice(String shopId, String key) {
        ConfigurationSection section = getSection(shopId, key);

        if (section == null) {
            org.bukkit.Bukkit.getLogger().warning("[ShopConfig] Shop ID: " + shopId + " - Item Key: " + key + " not found.");
            return Double.NaN;
        }

        double basePrice = section.getDouble("base_price", Double.NaN);

        if (Double.isNaN(basePrice)) {
            org.bukkit.Bukkit.getLogger().warning("[ShopConfig] Base Price not found or NaN for Shop ID: " + shopId + ", Item Key: " + key);
        } else {
            org.bukkit.Bukkit.getLogger().info("[ShopConfig] Base Price: " + basePrice + " for Shop ID: " + shopId + ", Item Key: " + key);
        }

        return basePrice;
    }

    public static double getBuyMultiplier(String shopId, String key) {
        return getSection(shopId, key).getDouble("buy_multiplier", 1.0);
    }

    public static double getSellMultiplier(String shopId, String key) {
        return getSection(shopId, key).getDouble("sell_multiplier", 0.7);
    }

    public static void incrementSupply(String shopId, String key, int amount) {
        ConfigurationSection section = getSection(shopId, key);
        if (section != null) {
            double currentSupply = section.getDouble("supply", 0);
            section.set("supply", currentSupply + amount);

            double newPrice = PriceHandler.calculateCurrentPrice(shopId, key);
            section.set("current_price", newPrice);

            saveShop(shopId);
        }
    }

    public static void incrementDemand(String shopId, String key, int amount) {
        ConfigurationSection section = getSection(shopId, key);
        if (section != null) {
            double currentDemand = section.getDouble("demand", 0);
            section.set("demand", currentDemand + amount);

            double newPrice = PriceHandler.calculateCurrentPrice(shopId, key);
            section.set("current_price", newPrice);

            saveShop(shopId);
        }
    }

    public static Map<String, FileConfiguration> getShopConfigMap() {
        return shopMap;
    }

    public static void saveShop(String shopId) {
        File folder = new File("plugins/Sushidynamicshop/shop");
        File file = new File(folder, shopId + ".yml");
        try {
            shopMap.get(shopId).save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ConfigurationSection getSection(String shopId, String key) {
        FileConfiguration config = shopMap.get(shopId);
        return config != null ? config.getConfigurationSection(key) : null;
    }

    public static ShopCurrency getCurrency(String shopId) {
        FileConfiguration config = getShopConfig(shopId);

        if (config == null) {
            org.bukkit.Bukkit.getLogger().warning("[ShopConfig] Shop '" + shopId + "' configuration not found.");
            return ShopCurrency.VAULT;
        }

        ConfigurationSection section = config.getConfigurationSection("shopcurrency");

        if (section == null) {
            org.bukkit.Bukkit.getLogger().warning("[ShopConfig] 'shopcurrency' section is missing in shop '" + shopId + "'.");
            return ShopCurrency.VAULT;
        }

        String type = section.getString("type", "VAULT").toUpperCase();

        try {
            return ShopCurrency.valueOf(type);
        } catch (IllegalArgumentException e) {
            org.bukkit.Bukkit.getLogger().warning("[ShopConfig] Invalid shopcurrency type for shop '" + shopId + "': " + type);
            return ShopCurrency.VAULT;
        }
    }

    public static String getCustomCurrencyId(String shopId) {
        FileConfiguration config = getShopConfig(shopId);

        if (config == null) {
            org.bukkit.Bukkit.getLogger().warning("[ShopConfig] Shop '" + shopId + "' configuration not found.");
            return "";
        }


        ConfigurationSection section = config.getConfigurationSection("shopcurrency");

        if (section == null) {
            org.bukkit.Bukkit.getLogger().warning("[ShopConfig] 'shopcurrency' section is missing in shop '" + shopId + "'.");
            return "";
        }

        String type = section.getString("type", "VAULT").toUpperCase();
        String currencyId = section.getString("currency", "").toLowerCase();

        if (!"CUSTOM".equals(type)) {
            return "";
        }

        if (currencyId.isEmpty()) {
            org.bukkit.Bukkit.getLogger().warning("[ShopConfig] Shop '" + shopId + "' is set to CUSTOM but no 'currency' key is defined.");
            return "no_currency_set";
        }

        return currencyId;
    }

    public static void createShop(String shopId, YamlConfiguration config) {
        File folder = new File("plugins/Sushidynamicshop/shop");
        if (!folder.exists()) folder.mkdirs();
        File file = new File(folder, shopId + ".yml");
        try {
            config.save(file);
            shopMap.put(shopId, config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addItemToShop(String shopId, String itemKey, ConfigurationSection section) {
        FileConfiguration config = shopMap.get(shopId);
        if (config == null) return;

        config.createSection(itemKey);
        for (String key : section.getKeys(false)) {
            config.set(itemKey + "." + key, section.get(key));
        }
        saveShop(shopId);
    }

    public static void setSupply(String shopId, String key, int supply) {
        ConfigurationSection section = getSection(shopId, key);
        if (section != null) {
            section.set("supply", supply);
            saveShop(shopId);
        }
    }

    public static String getShopName(String shopId) {
        FileConfiguration config = getShopConfig(shopId);
        if (config == null) return shopId;
        return config.getString("inventory.name", shopId);
    }

    public static int getSlotCount(String shopId) {
        FileConfiguration config = getShopConfig(shopId);
        if (config == null) return 54; // Default to 54 if not set
        int slot = config.getInt("inventory.slot", 54);

        // ตรวจสอบให้ slot เป็นค่าใน 9, 18, 27, 36, 45, 54 เท่านั้น
        if (slot % 9 != 0 || slot < 9 || slot > 54) {
            slot = 54;
        }
        return slot;
    }

    public static Material getShopDeco(String shopId) {
        FileConfiguration config = getShopConfig(shopId);
        if (config == null) return Material.PINK_STAINED_GLASS_PANE;

        String decoMaterial = config.getString("inventory.deco", "PINK_STAINED_GLASS_PANE");
        Material material = Material.matchMaterial(decoMaterial);

        return (material != null) ? material : Material.PINK_STAINED_GLASS_PANE;
    }
}