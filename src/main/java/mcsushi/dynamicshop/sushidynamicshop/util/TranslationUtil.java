
package mcsushi.dynamicshop.sushidynamicshop.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.Map;

import static mcsushi.dynamicshop.sushidynamicshop.Sushidynamicshop.getInstance;

public class TranslationUtil {

    private static FileConfiguration translation;

    public static void load() {
        File file = new File(getInstance().getDataFolder(), "translation.yml");
        if (!file.exists()) {
            getInstance().saveResource("translation.yml", false);
        }
        translation = YamlConfiguration.loadConfiguration(file);
        validateKeys(file);
    }

    private static void validateKeys(File file) {
        FileConfiguration userConfig = YamlConfiguration.loadConfiguration(file);
        InputStream stream = getInstance().getResource("translation.yml");
        if (stream == null) return;

        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
        ConfigurationSection defaultSection = defaultConfig.getConfigurationSection("translation");

        boolean updated = false;

        if (defaultSection != null) {
            for (String key : defaultSection.getKeys(false)) {
                String fullKey = "translation." + key;
                if (!userConfig.contains(fullKey)) {
                    userConfig.set(fullKey, defaultConfig.get(fullKey));
                    updated = true;
                }
            }
        }

        String[][] requiredKeys = {
                    {"buy", "&eBuy: %buy_price% %currency%"},
                    {"sell", "&eSell: %sell_price% %currency%"},
                    {"buy_level", "&aBuy: &f%level_price% Levels"},
                    {"sell_level", "&cSell: &f%level_price% Levels"},
                    {"buy_points", "&aBuy: &f%points_price% Points"},
                    {"sell_points", "&cSell: &f%points_price% Points"},
                    {"supply_demand", "&7Stock: &f%stock% &8| &7Demand: &f%demand%"},
                    {"fluctuation_positive", "&a+%percentage%%"},
                    {"fluctuation_negative", "&c%percentage%%"},
                    {"fluctuation_zero", "&7%percentage%%"},
                    {"fluctuation_na", "&7N/A"},
                    {"bought", "&aYou bought item successfully."},
                    {"sold", "&aYou received %amount% for selling items."},
                    {"sold_custom", "&aYou received %amount% %currency% for selling items."},
                    {"sold_exp", "&aYou received %amount% EXP for selling items."},
                    {"sold_points", "&aYou received %amount% Points for selling items."},
                    {"not_enough_money", "&cYou don't have enough money."},
                    {"not_enough_level", "&cYou need at least %required% levels."},
                    {"not_enough_points", "&cYou don't have enough points."},
                    {"no_item_to_sell", "&cYou don't have item to sell."},
                    {"nopermission", "You do not have permission to use /sushishop."},
                    {"usage", "[SushiShop] Usage: /sushishop <reload|...>"},
                    {"unknowncommand", "Unknown subcommand. Use /sushishop <reload|...>"},
                    {"console_use", "Console cannot use this command."},
                    {"nopermission_reload", "&cYou do not have permission to use /sushishop reload."},
                    {"no_shop_permission", "&cYou do not have permission to open this shop."},
                    {"reload_success", "&a[SushiShop] Reloaded %count% YML file(s) in %time% ms."},
                    {"next", "&b» Next"},
                    {"previous", "&b« Previous"},
                    {"close", "&c✕ Close"},
                    {"back", "&cBack"},
                    {"left_click_buy", "&eLeft click to Buy"},
                    {"shift_left_click_buy_all", "&eShift + Left Click to Buy all"},
                    {"right_click_sell", "&6Right click to Sell"},
                    {"shift_right_click_sell_all", "&6Shift + Right Click to Sell all"},
                    {"no_currency_set", "&cThis shop has no custom currency set."},
                    {"not_enough_custom", "&cYou do not have enough %currency%. Required: %required%"},
                    {"not_enough_space", "&cYou do not have enough inventory space to buy this item"},
                    {"out_of_stock", "&cThis item is out of stock."}
            };
        for (String[] pair : requiredKeys) {
            String fullKey = "translation." + pair[0];
            if (!userConfig.contains(fullKey)) {
                userConfig.set(fullKey, pair[1]);
                updated = true;
            }
        }

        if (updated) {
            try {
                userConfig.save(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String get(String key) {
        if (translation == null) load();
        String raw = translation.getString("translation." + key, key);
        raw = ChatColor.translateAlternateColorCodes('&', raw);
        return applyGradient(raw);
    }

    public static String get(String key, Map<String, String> placeholders) {
        String text = get(key);
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                String value = entry.getValue();

                if (entry.getKey().equalsIgnoreCase("amount")) {
                    try {
                        double amount = Double.parseDouble(value);
                        value = String.format("%.2f", amount);
                    } catch (NumberFormatException ignored) {
                        // หากไม่สามารถแปลงได้ ให้ใช้ค่าเดิม
                    }
                }

                text = text.replace("%" + entry.getKey() + "%", value);
            }
        }
        return text;
    }

    public static String getFluctuation(double percentage) {
        String key;

        if (Double.isNaN(percentage)) {
            key = "fluctuation_na";
        } else if (percentage == 0) {
            key = "fluctuation_zero";
        } else if (percentage > 0) {
            key = "fluctuation_positive";
        } else {
            key = "fluctuation_negative";
        }

        String template = get(key);
        return template.replace("%percentage%", String.format("%.2f", percentage));
    }

    private static String applyGradient(String text) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<gradient:(#[0-9a-fA-F]{6}):(#(?:[0-9a-fA-F]{6}))>(.*?)</gradient>");
        java.util.regex.Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String startColor = matcher.group(1);
            String endColor = matcher.group(2);
            String content = matcher.group(3);

            String gradientText = generateGradient(content, startColor, endColor);
            text = text.replace(matcher.group(0), gradientText);
        }

        return text;
    }

    private static String generateGradient(String content, String startColor, String endColor) {
        StringBuilder builder = new StringBuilder();
        java.awt.Color start = java.awt.Color.decode(startColor);
        java.awt.Color end = java.awt.Color.decode(endColor);
        int length = content.length();

        for (int i = 0; i < length; i++) {
            float ratio = (float) i / (length - 1);
            int red = (int) (start.getRed() + ratio * (end.getRed() - start.getRed()));
            int green = (int) (start.getGreen() + ratio * (end.getGreen() - start.getGreen()));
            int blue = (int) (start.getBlue() + ratio * (end.getBlue() - start.getBlue()));

            String hex = String.format("#%02x%02x%02x", red, green, blue);
            builder.append(net.md_5.bungee.api.ChatColor.of(hex)).append(content.charAt(i));
        }

        return builder.toString();
    }
}
