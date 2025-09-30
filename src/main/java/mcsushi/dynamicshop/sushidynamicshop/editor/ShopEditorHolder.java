package mcsushi.dynamicshop.sushidynamicshop.editor;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ShopEditorHolder implements InventoryHolder {

    private final String shopId;
    private final String itemKey;

    public ShopEditorHolder(String shopId, String itemKey) {
        this.shopId = shopId;
        this.itemKey = itemKey;
    }

    public String getShopId() {
        return shopId;
    }

    public String getItemKey() {
        return itemKey;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
