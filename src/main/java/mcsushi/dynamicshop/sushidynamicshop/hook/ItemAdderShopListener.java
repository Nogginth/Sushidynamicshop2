package mcsushi.dynamicshop.sushidynamicshop.hook;

import mcsushi.dynamicshop.sushidynamicshop.gui.ShopHolder;
import mcsushi.dynamicshop.sushidynamicshop.gui.ShopGUI;
import mcsushi.dynamicshop.sushidynamicshop.pricehandler.PriceHandler;
import mcsushi.dynamicshop.sushidynamicshop.shop.ShopConfig;
import mcsushi.dynamicshop.sushidynamicshop.util.ItemAdderUtil;
import mcsushi.dynamicshop.sushidynamicshop.util.TranslationUtil;
import mcsushi.dynamicshop.sushidynamicshop.util.VaultUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemAdderShopListener implements Listener {

    @EventHandler
    public void onItemAdderClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof ShopHolder holder)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getRawSlot() >= top.getSize()) return;

        String shopId = holder.getShopId();
        int slot = event.getSlot();

        List<String> keys = ShopConfig.getShopItems(shopId);
        String key = keys.stream()
                .filter(k -> ShopConfig.getSlot(shopId, k) == slot)
                .findFirst()
                .orElse(null);
        if (key == null) return;

        String source = ShopConfig.getSource(shopId, key);
        if (source == null || !source.toLowerCase().startsWith("itemadder:")) return;

        event.setCancelled(true);

        boolean isLeft = event.isLeftClick();
        boolean isRight = event.isRightClick();
        boolean isShift = event.getClick().isShiftClick();

        ItemStack previewItem = ItemAdderUtil.createItem(source.substring("itemadder:".length()), 1);
        if (previewItem == null) return;

        int amount = isShift ? previewItem.getMaxStackSize() : 1;

        if (isLeft && ShopConfig.canBuy(shopId, key)) {
            double totalPrice = PriceHandler.getCurrentBuyPrice(shopId, key);

            if (VaultUtil.getEconomy().withdrawPlayer(player, totalPrice).transactionSuccess()) {
                ItemStack give = ItemAdderUtil.createItem(source.substring("itemadder:".length()), amount);
                player.getInventory().addItem(give);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1.5f);
                player.sendMessage(TranslationUtil.get("bought", null));

                ShopConfig.incrementDemand(shopId, key, amount);
                ShopGUI.open(player, shopId);
            } else {
                player.sendMessage(TranslationUtil.get("not_enough_money", null));
            }
        }

        if (isRight && ShopConfig.canSell(shopId, key)) {
            int sellAmount = Math.min(amount, countItem(player, previewItem));
            if (sellAmount == 0) {
                player.sendMessage(TranslationUtil.get("no_item_to_sell", null));
                return;
            }

            double totalPrice = PriceHandler.getCurrentSellPrice(shopId, key);

            removeItem(player, previewItem, sellAmount);
            VaultUtil.getEconomy().depositPlayer(player, totalPrice);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 0.5f);
            player.sendMessage(TranslationUtil.get("sold", null));

            ShopConfig.incrementSupply(shopId, key, sellAmount);
            ShopGUI.open(player, shopId);
        }
    }

    private int countItem(Player player, ItemStack target) {
        int total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            if (ItemAdderUtil.isItemAdderItem(item)) {
                String id1 = ItemAdderUtil.getNamespacedId(item);
                String id2 = ItemAdderUtil.getNamespacedId(target);
                if (id1 != null && id1.equals(id2)) {
                    total += item.getAmount();
                }
            }
        }
        return total;
    }

    private void removeItem(Player player, ItemStack target, int amount) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null) continue;

            if (ItemAdderUtil.isItemAdderItem(item)) {
                String id1 = ItemAdderUtil.getNamespacedId(item);
                String id2 = ItemAdderUtil.getNamespacedId(target);
                if (id1 != null && id1.equals(id2)) {
                    int remove = Math.min(amount, item.getAmount());
                    item.setAmount(item.getAmount() - remove);
                    amount -= remove;
                    if (amount <= 0) break;
                }
            }
        }
    }
}
