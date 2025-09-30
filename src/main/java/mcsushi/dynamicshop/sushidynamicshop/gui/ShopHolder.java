package mcsushi.dynamicshop.sushidynamicshop.gui;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.io.File;

public class ShopHolder implements InventoryHolder {

    private final String shopId;
    private final FileConfiguration config;
    private final File file;
    private final int page;
    private final int menuType;

    public ShopHolder(String shopId, FileConfiguration config, File file, int page, int menuType) {
        this.shopId = shopId;
        this.config = config;
        this.file = file;
        this.page = page;
        this.menuType = menuType;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public File getFile() {
        return file;
    }

    public int getPage() {
        return page;
    }

    public int getMenuType() {
        return menuType;
    }

    public String getShopId() {
        return shopId;
    }
}