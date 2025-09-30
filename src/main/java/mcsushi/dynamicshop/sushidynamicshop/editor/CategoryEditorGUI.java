package mcsushi.dynamicshop.sushidynamicshop.editor;

import mcsushi.dynamicshop.sushidynamicshop.config.CategoryConfig;
import mcsushi.dynamicshop.sushidynamicshop.editor.holder.CategoryEditorHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.Arrays;

public class CategoryEditorGUI {

    public static void open(Player player, String categoryId) {
        // ตรวจสอบ key ที่จำเป็น
        ConfigurationSection section = CategoryConfig.get().getConfigurationSection("categories." + categoryId);
        if (section == null) {
            section = CategoryConfig.get().createSection("categories." + categoryId);
        }

        Set<Integer> reserved = new HashSet<>(Arrays.asList(
        ));
        if (!section.contains("name")) section.set("name", categoryId);
        if (!section.contains("material")) section.set("material", "STONE");

        if (!section.contains("slot")) {
            Set<Integer> usedSlots = new HashSet<>();
            ConfigurationSection all = CategoryConfig.get().getConfigurationSection("categories");
            if (all != null) {
                for (String key : all.getKeys(false)) {
                    ConfigurationSection cat = all.getConfigurationSection(key);
                    if (cat != null && cat.contains("slot")) {
                        usedSlots.add(cat.getInt("slot"));
                    }
                }
            }
            int freeSlot = -1;
            for (int i = 0; i < 54; i++) {
                if (!usedSlots.contains(i) && !reserved.contains(i)) {
                    freeSlot = i;
                    break;
                }
            }
            section.set("slot", freeSlot == -1 ? 10 : freeSlot);
            Bukkit.getLogger().info("[DEBUG] Slot assigned to category '" + categoryId + "': " + (freeSlot == -1 ? 10 : freeSlot));
        }

        if (!section.contains("shopid")) section.set("shopid", categoryId);
        if (!section.contains("permission")) section.set("permission", "");
        if (!section.contains("lore")) section.set("lore", new ArrayList<String>());
        if (!section.contains("CustomModelData")) section.set("CustomModelData", null);
        CategoryConfig.save();

        Inventory gui = Bukkit.createInventory(new CategoryEditorHolder(categoryId), 27,
                ChatColor.LIGHT_PURPLE + "Edit Category: " + categoryId);

        // Border
        ItemStack border = new ItemStack(Material.PINK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            border.setItemMeta(borderMeta);
        }
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, border);
        }

        // ปุ่ม
        gui.setItem(10, mcsushi.dynamicshop.sushidynamicshop.util.VanillaItemFactory.createIconFromSection(section));
        setButton(gui, 11, Material.ITEM_FRAME, "slot", String.valueOf(section.getInt("slot")));
        setButton(gui, 12, Material.CHEST, "shopid", section.getString("shopid"));
        setButton(gui, 13, Material.NAME_TAG, "name", section.getString("name"));
        setButton(gui, 14, Material.BOOK, "lore", String.join("\n", section.getStringList("lore")));
        setButton(gui, 15, Material.STRUCTURE_VOID, "permission", section.getString("permission"));

        // กลับ
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.RED + "Back");
            back.setItemMeta(backMeta);
        }
        gui.setItem(22, back); // ปุ่มกลางล่าง


        ItemMeta iconMeta = gui.getItem(10).getItemMeta();
        if (iconMeta != null) {
            iconMeta.setDisplayName(ChatColor.YELLOW + "Edit Icon");
            gui.getItem(10).setItemMeta(iconMeta);
        }

        player.openInventory(gui);
    }

    private static void setButton(Inventory gui, int slot, Material material, String label, String value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "Edit " + label);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Current: " + value);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        gui.setItem(slot, item);
    }
}