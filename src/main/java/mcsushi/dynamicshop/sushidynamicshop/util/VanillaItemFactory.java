package mcsushi.dynamicshop.sushidynamicshop.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class VanillaItemFactory {

    public static ItemStack createItem(String source) {
        try {
            Material material = Material.matchMaterial(source);
            if (material == null) {
                return new ItemStack(Material.BARRIER);
            }
            return new ItemStack(material);
        } catch (Exception e) {
            return new ItemStack(Material.BARRIER);
        }
    }

    public static ItemStack createIconFromSection(ConfigurationSection section) {
        String matStr = section.getString("material", "STONE");
        Material material = Material.matchMaterial(matStr);
        if (material == null) material = Material.STONE;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(material);

        if (meta != null) {
            if (section.contains("CustomModelData")) {
                meta.setCustomModelData(section.getInt("CustomModelData"));
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "Hold an item and click to set as icon.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }
}
