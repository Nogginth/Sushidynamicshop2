package mcsushi.dynamicshop.sushidynamicshop.editor;

import mcsushi.dynamicshop.sushidynamicshop.shop.ShopConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ShopEditorGUI {

    public static void open(Player player, String shopId, String itemKey) {
        ConfigurationSection section = ShopConfig.getShopConfig(shopId).getConfigurationSection(itemKey);
        if (section == null) {
            player.sendMessage(ChatColor.RED + "Item not found in shop.");
            return;
        }

        Inventory gui = Bukkit.createInventory(new ShopEditorHolder(shopId, itemKey), 45,
                ChatColor.LIGHT_PURPLE + "Edit Shop Item: " + itemKey);

        // กรอบรอบ GUI (ไม่ fill)
        ItemStack border = new ItemStack(Material.PINK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            border.setItemMeta(borderMeta);
        }
        for (int i = 0; i < 45; i++) {
            if (i <= 8 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, border);
            }
        }

        // ปุ่มหลัก (สมดุล)
        setButton(gui, 10, Material.ITEM_FRAME, "Slot", String.valueOf(section.getInt("slot", 0)));
        setButton(gui, 11, Material.EMERALD, "Base Price", String.valueOf(section.getDouble("base_price", 0)));
        setButton(gui, 12, Material.DIAMOND, "Current Price", String.valueOf(section.getDouble("current_price", 0)));
        setButton(gui, 13, Material.LIME_DYE, "Buy Multiplier", String.valueOf(section.getDouble("buy_multiplier", 1.0)));
        setButton(gui, 14, Material.RED_DYE, "Sell Multiplier", String.valueOf(section.getDouble("sell_multiplier", 0.7)));

        setButton(gui, 19, Material.BOOK, "Min Price", String.valueOf(section.getDouble("min_price", 0)));
        setButton(gui, 20, Material.BOOK, "Max Price", String.valueOf(section.getDouble("max_price", 0)));
        setButton(gui, 21, Material.SUNFLOWER, "Supply", String.valueOf(section.getDouble("supply", 0)));
        setButton(gui, 22, Material.NETHER_STAR, "Demand", String.valueOf(section.getDouble("demand", 0)));

        setButton(gui, 23,
                section.getBoolean("buy_enabled", true) ? Material.LIME_WOOL : Material.RED_WOOL,
                "Can Buy",
                section.getBoolean("buy_enabled", true) ? "ON" : "OFF");

        setButton(gui, 24,
                section.getBoolean("sell_enabled", true) ? Material.LIME_WOOL : Material.RED_WOOL,
                "Can Sell",
                section.getBoolean("sell_enabled", true) ? "ON" : "OFF");

        setButton(gui, 30, Material.CHEST, "Source", section.getString("source", "N/A"));
        setButton(gui, 31, Material.ITEM_FRAME, "Preview", "Click to preview");

        // ปุ่ม Back
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.RED + "Back");
            back.setItemMeta(backMeta);
        }
        gui.setItem(40, back);

        player.openInventory(gui);
    }

    private static void setButton(Inventory gui, int slot, Material mat, String label, String value) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "Edit " + label);
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Current:", ChatColor.GRAY + value));
            item.setItemMeta(meta);
        }
        gui.setItem(slot, item);
    }
}
