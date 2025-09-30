package mcsushi.dynamicshop.sushidynamicshop.editor;

import mcsushi.dynamicshop.sushidynamicshop.gui.ShopGUI;
import mcsushi.dynamicshop.sushidynamicshop.hook.MMOItemHook;
import mcsushi.dynamicshop.sushidynamicshop.shop.ShopConfig;
import mcsushi.dynamicshop.sushidynamicshop.util.MMOItemFactory;
import mcsushi.dynamicshop.sushidynamicshop.util.VanillaItemFactory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class ShopEditorListener implements Listener {

    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof ShopEditorHolder shopHolder)) return;

        int rawSlot = event.getRawSlot();
        if (rawSlot >= event.getView().getTopInventory().getSize()) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        switch (rawSlot) {
            case 10 -> ShopEditorInputManager.startInput(player, shopHolder.getShopId(), shopHolder.getItemKey(), ShopField.SLOT);
            case 11 -> ShopEditorInputManager.startInput(player, shopHolder.getShopId(), shopHolder.getItemKey(), ShopField.BASE_PRICE);
            case 12 -> player.sendMessage(ChatColor.GRAY + "Current price is auto-calculated.");
            case 13 -> ShopEditorInputManager.startInput(player, shopHolder.getShopId(), shopHolder.getItemKey(), ShopField.BUY_MULTIPLIER);
            case 14 -> ShopEditorInputManager.startInput(player, shopHolder.getShopId(), shopHolder.getItemKey(), ShopField.SELL_MULTIPLIER);
            case 19 -> ShopEditorInputManager.startInput(player, shopHolder.getShopId(), shopHolder.getItemKey(), ShopField.MIN_PRICE);
            case 20 -> ShopEditorInputManager.startInput(player, shopHolder.getShopId(), shopHolder.getItemKey(), ShopField.MAX_PRICE);
            case 21 -> ShopEditorInputManager.startInput(player, shopHolder.getShopId(), shopHolder.getItemKey(), ShopField.SUPPLY);
            case 22 -> ShopEditorInputManager.startInput(player, shopHolder.getShopId(), shopHolder.getItemKey(), ShopField.DEMAND);
            case 23 -> {
                boolean current = ShopConfig.getShopConfig(shopHolder.getShopId()).getBoolean(shopHolder.getItemKey() + ".buy_enabled", true);
                ShopConfig.getShopConfig(shopHolder.getShopId()).set(shopHolder.getItemKey() + ".buy_enabled", !current);
                ShopEditorGUI.open(player, shopHolder.getShopId(), shopHolder.getItemKey());
            }
            case 24 -> {
                boolean current = ShopConfig.getShopConfig(shopHolder.getShopId()).getBoolean(shopHolder.getItemKey() + ".sell_enabled", true);
                ShopConfig.getShopConfig(shopHolder.getShopId()).set(shopHolder.getItemKey() + ".sell_enabled", !current);
                ShopEditorGUI.open(player, shopHolder.getShopId(), shopHolder.getItemKey());
            }
            case 30 -> ShopEditorInputManager.startInput(player, shopHolder.getShopId(), shopHolder.getItemKey(), ShopField.SOURCE);
            case 31 -> {
                String source = ShopConfig.getSource(shopHolder.getShopId(), shopHolder.getItemKey());
                ItemStack preview;
                if (source.toLowerCase().startsWith("mmoitem:")) {
                    if (MMOItemHook.isHooked()) {
                        preview = MMOItemFactory.createItem(source);
                    } else {
                        preview = new ItemStack(Material.BARRIER);
                    }
                } else {
                    preview = VanillaItemFactory.createItem(source);
                }
                player.getInventory().addItem(preview);
                player.sendMessage(ChatColor.GREEN + "Item preview added to your inventory.");
            }
            case 40 -> ShopGUI.open(player, shopHolder.getShopId());
            default -> player.sendMessage(ChatColor.RED + "This field is not editable yet.");
        }
    }

    @EventHandler
    public void onGUIDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof ShopEditorHolder) {
            event.setCancelled(true);
        }
    }
}
