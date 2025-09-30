
package mcsushi.dynamicshop.sushidynamicshop.debug;

import mcsushi.dynamicshop.sushidynamicshop.shop.ShopConfig;
import mcsushi.dynamicshop.sushidynamicshop.util.GuiSlotHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;

import java.util.Set;

public class QClickDebugger {

    public static void debug(Player player, InventoryView view, InventoryHolder holder, int slot, String shopId) {
        Bukkit.getLogger().info("[DEBUG/Q] Player: " + player.getName());
        Bukkit.getLogger().info("[DEBUG/Q] Clicked slot: " + slot);
        Bukkit.getLogger().info("[DEBUG/Q] Holder type: " + holder.getClass().getSimpleName());

        Set<Integer> reserved = GuiSlotHolder.getReservedSlotsFor(holder);
        if (reserved.contains(slot)) {
            Bukkit.getLogger().info("[DEBUG/Q] Slot " + slot + " is RESERVED");
        } else {
            Bukkit.getLogger().info("[DEBUG/Q] Slot " + slot + " is NOT reserved");
        }

        String existingKey = ShopConfig.getShopItems(shopId).stream()
                .filter(k -> ShopConfig.getSlot(shopId, k) == slot)
                .findFirst().orElse(null);
        if (existingKey != null) {
            Bukkit.getLogger().info("[DEBUG/Q] Slot " + slot + " already has item with key: " + existingKey);
        } else {
            Bukkit.getLogger().info("[DEBUG/Q] Slot " + slot + " is empty and ready for new item");
        }
    }
}
