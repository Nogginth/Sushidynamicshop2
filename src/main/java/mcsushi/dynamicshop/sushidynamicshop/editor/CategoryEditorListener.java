package mcsushi.dynamicshop.sushidynamicshop.editor;

import mcsushi.dynamicshop.sushidynamicshop.config.CategoryConfig;
import mcsushi.dynamicshop.sushidynamicshop.editor.holder.CategoryEditorHolder;
import mcsushi.dynamicshop.sushidynamicshop.gui.CategoryMenuGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CategoryEditorListener implements Listener {

    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        InventoryHolder holder = inv.getHolder();
        if (!(holder instanceof CategoryEditorHolder categoryHolder)) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        switch (slot) {
            case 10 -> {
                ItemStack held = player.getInventory().getItemInMainHand();
                if (held == null || held.getType() == Material.AIR) {
                    player.sendMessage(ChatColor.RED + "Please hold an item to use as the icon.");
                    return;
                }

                ConfigurationSection section = CategoryConfig.get().getConfigurationSection("categories." + categoryHolder.getCategoryId());
                if (section == null) return;

                section.set("material", held.getType().name());

                ItemMeta meta = held.getItemMeta();
                if (meta != null && meta.hasCustomModelData()) {
                    ItemStack copy = new ItemStack(held.getType());
                    ItemMeta clean = copy.getItemMeta();
                    if (clean != null) {
                        clean.setCustomModelData(meta.getCustomModelData());
                        copy.setItemMeta(clean);
                        section.set("CustomModelData", meta.getCustomModelData());
                    }
                } else {
                    section.set("CustomModelData", null);
                }

                CategoryConfig.save();
                CategoryEditorGUI.open(player, categoryHolder.getCategoryId());
                player.sendMessage(ChatColor.GREEN + "Icon updated from held item.");
            }
            case 11 -> CategoryEditorInputManager.startInput(player, categoryHolder.getCategoryId(), CategoryField.SLOT);
            case 12 -> CategoryEditorInputManager.startInput(player, categoryHolder.getCategoryId(), CategoryField.SHOPID);
            case 13 -> CategoryEditorInputManager.startInput(player, categoryHolder.getCategoryId(), CategoryField.NAME);
            case 14 -> CategoryEditorInputManager.startInput(player, categoryHolder.getCategoryId(), CategoryField.LORE);
            case 15 -> CategoryEditorInputManager.startInput(player, categoryHolder.getCategoryId(), CategoryField.PERMISSION);
            case 22 -> CategoryMenuGUI.open(player);
            default -> player.sendMessage(ChatColor.RED + "This field is not editable yet.");
        }
    }

    @EventHandler
    public void onGUIDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof CategoryEditorHolder) {
            event.setCancelled(true);
        }
    }
}