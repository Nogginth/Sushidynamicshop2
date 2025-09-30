
package mcsushi.dynamicshop.sushidynamicshop.editor;

import mcsushi.dynamicshop.sushidynamicshop.gui.ShopGUI;
import mcsushi.dynamicshop.sushidynamicshop.gui.ShopHolder;
import mcsushi.dynamicshop.sushidynamicshop.shop.ShopConfig;
import mcsushi.dynamicshop.sushidynamicshop.util.GuiSlotHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class ShopEditorAddItemListener implements Listener {

    @EventHandler
    public void onShiftLeftClickEmptySlot(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        InventoryHolder holder = top.getHolder();

        if (!(holder instanceof ShopHolder shopHolder)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getClick().equals(ClickType.SHIFT_LEFT)) return;
        if (!player.isOp()) return;

        int slot = event.getRawSlot();
        if (GuiSlotHolder.getReservedSlotsFor(holder).contains(slot)) return;

        if (slot >= top.getSize()) return;

        ItemStack currentItem = top.getItem(slot);
        if (currentItem != null && currentItem.getType().isItem()) return;

        String shopId = shopHolder.getShopId();
        boolean slotUsed = ShopConfig.getShopItems(shopId).stream()
                .anyMatch(k -> ShopConfig.getSlot(shopId, k) == slot);
        if (slotUsed) return;

        event.setCancelled(true);
        ShopEditorInputManager.startConfirmCreate(player, shopId, slot);
    }
}
