package mcsushi.dynamicshop.sushidynamicshop.editor.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CategoryEditorHolder implements InventoryHolder {

    private final String categoryId;

    public CategoryEditorHolder(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}