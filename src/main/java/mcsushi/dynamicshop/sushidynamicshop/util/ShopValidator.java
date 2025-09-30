package mcsushi.dynamicshop.sushidynamicshop.util;

import mcsushi.dynamicshop.sushidynamicshop.shop.ShopConfig;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ShopValidator {

    public static void validateShopFile(String shopId) {
        FileConfiguration config = ShopConfig.getShopConfig(shopId);
        boolean changed = false;

        // 🔍 ตรวจสอบส่วน inventory
        if (!config.contains("inventory.name")) {
            config.set("inventory.name", "&fUnnamed Shop");
            changed = true;
        }
        if (!config.contains("inventory.slot")) {
            config.set("inventory.slot", 54);
            changed = true;
        }
        if (!config.contains("inventory.deco")) {
            config.set("inventory.deco", "PINK_STAINED_GLASS_PANE");
            changed = true;
        }
        if (!config.contains("shopcurrency")) {
            config.createSection("shopcurrency");
            config.set("shopcurrency.type", "VAULT");
            config.set("shopcurrency.currency", "test01");
            changed = true;
        }

        // 🔍 ตรวจสอบแต่ละ item
        List<String> keys = ShopConfig.getShopItems(shopId);
        for (String key : keys) {
            String path = key + ".";
            if (!config.contains(path + "source")) {
                config.set(path + "source", "STONE");
                changed = true;
            }
            if (!config.contains(path + "slot")) {
                config.set(path + "slot", 10);
                changed = true;
            }
            if (!config.contains(path + "base_price")) {
                config.set(path + "base_price", 10.0);
                changed = true;
            }
            if (!config.contains(path + "current_price")) {
                config.set(path + "current_price", 10.0);
                changed = true;
            }
            if (!config.contains(path + "min_price")) {
                config.set(path + "min_price", 1.0);
                changed = true;
            }
            if (!config.contains(path + "max_price")) {
                config.set(path + "max_price", 100.0);
                changed = true;
            }
            if (!config.contains(path + "price_change_rate")) {
                config.set(path + "price_change_rate", 0.05);
                changed = true;
            }
            if (!config.contains(path + "supply")) {
                config.set(path + "supply", 0);
                changed = true;
            }
            if (!config.contains(path + "demand")) {
                config.set(path + "demand", 0);
                changed = true;
            }
            if (!config.contains(path + "buy_enabled")) {
                config.set(path + "buy_enabled", true);
                changed = true;
            }
            if (!config.contains(path + "sell_enabled")) {
                config.set(path + "sell_enabled", true);
                changed = true;
            }
            if (!config.contains(path + "real_supply")) {
                config.set(path + "real_supply", false);
                changed = true;
            }
        }

        if (changed) {
            ShopConfig.saveShop(shopId);
            System.out.println("[SushiShop] Patched missing keys in shop: " + shopId);
        }
    }
}
