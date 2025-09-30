package mcsushi.dynamicshop.sushidynamicshop.gui;

import mcsushi.dynamicshop.sushidynamicshop.hook.MMOItemHook;
import mcsushi.dynamicshop.sushidynamicshop.init.PremiumInitializer;
import mcsushi.dynamicshop.sushidynamicshop.shop.ShopConfig;
import mcsushi.dynamicshop.sushidynamicshop.shop.ShopCurrency;
import mcsushi.dynamicshop.sushidynamicshop.util.*;
import mcsushi.dynamicshop.sushidynamicshop.pricehandler.PriceHandler;
import mcsushi.dynamicshop.sushidynamicshop.editor.ShopEditorGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShopGUI {
    public static void open(Player player, String shopId) {
        Set<Integer> usedSlots = new HashSet<>();
        if (!ShopConfig.hasShop(shopId)) return;

        List<String> itemKeys = ShopConfig.getShopItems(shopId);
        File file = new File("plugins/Sushidynamicshop/shop/" + shopId + ".yml");

        int slotCount = ShopConfig.getSlotCount(shopId);
        Inventory inv = Bukkit.createInventory(
                new ShopHolder(shopId, ShopConfig.getShopConfig(shopId), file, 0, 0),
                slotCount,
                ChatColor.translateAlternateColorCodes('&', ShopConfig.getShopName(shopId))
        );

        // decorate border
        Material decoMaterial = ShopConfig.getShopDeco(shopId);
        ItemStack border = new ItemStack(decoMaterial);
        ItemMeta meta = border.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            border.setItemMeta(meta);
        }

        int size = inv.getSize();
        for (int i = 0; i < size; i++) {
            boolean isBorder = (i < 9) || (i >= size - 9) || (i % 9 == 0) || (i % 9 == 8);
            if (isBorder) {
                inv.setItem(i, border);
            }
        }

        // back button
        int backSlot;
        switch (slotCount) {
            case 27 -> backSlot = 22; // แถวล่างสุด ช่องที่ 4 นับจากซ้าย
            case 36 -> backSlot = 31;
            case 45 -> backSlot = 40; // ปรับจาก 40 เป็น 44 เพื่อให้อยู่แถวล่างสุด ตรงกลาง
            case 54 -> backSlot = 49;
            default -> {
                Bukkit.getLogger().warning("[Sushidynamicshop] Invalid slot count: " + slotCount);
                return; // ไม่รองรับขนาดอื่น ๆ
            }
        }

        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(TranslationUtil.get("back"));
            back.setItemMeta(backMeta);
        }
        inv.setItem(backSlot, back);

        for (String key : itemKeys) {
            String source = ShopConfig.getSource(shopId, key);
            int slot = ShopConfig.getSlot(shopId, key);
            double buyPrice = PriceHandler.getCurrentBuyPrice(shopId, key);
            double sellPrice = PriceHandler.getCurrentSellPrice(shopId, key);
            double supply = ShopConfig.getSupply(shopId, key);
            double demand = ShopConfig.getDemand(shopId, key);
            boolean canBuy = ShopConfig.canBuy(shopId, key);
            boolean canSell = ShopConfig.canSell(shopId, key);

            org.bukkit.Bukkit.getLogger().info(
                    "[DEBUG] [ShopGUI - open] Shop: " + shopId +
                            ", Key: " + key +
                            ", Slot: " + slot +
                            ", Source: " + source +
                            ", Buy Price: " + buyPrice +
                            ", Sell Price: " + sellPrice +
                            ", Supply: " + supply +
                            ", Demand: " + demand +
                            ", Can Buy: " + canBuy +
                            ", Can Sell: " + canSell
            );

            ItemStack item;
            if (source.toLowerCase().startsWith("itemadder:")) {
                String id = source.substring("itemadder:".length());
                item = mcsushi.dynamicshop.sushidynamicshop.util.ItemAdderUtil.createItem(id, 1);
            } else {
                if (source.toLowerCase().startsWith("mmoitem:")) {
                    if (MMOItemHook.isHooked()) {
                        item = MMOItemFactory.createItem(source);
                    } else {
                        item = new ItemStack(Material.BARRIER);
                    }
                } else if (source.toLowerCase().startsWith("nexo:")) {
                    String id = source.substring("nexo:".length());
                    item = Nexoutil.createItem(id, 1);
                    if (item == null || item.getType() == Material.BARRIER) {
                        item = new ItemStack(Material.BARRIER); // ป้องกันไม่ให้ GUI พัง
                    }
                } else {
                    item = VanillaItemFactory.createItem(source);
                }
            }
            if (item == null) continue;

            ShopCurrency currency = ShopConfig.getCurrency(shopId);
            String displayCurrency = "";
            String customCurrencyId = null;

            if (currency == ShopCurrency.CUSTOM) {
                customCurrencyId = ShopConfig.getCustomCurrencyId(shopId);

                if (customCurrencyId != null && !customCurrencyId.isEmpty()) {
                    CurrencyRegistry registry = PremiumInitializer.getCurrencyRegistry();
                    displayCurrency = registry != null ? registry.getPronoun(customCurrencyId) : customCurrencyId;
                }

            } else if (currency != ShopCurrency.VAULT) {
                displayCurrency = currency.name();
            }

            double basePrice = ShopConfig.getBasePrice(shopId, key);

            ItemDisplayUtil.appendShopLore(item, shopId, key, basePrice, supply, demand, canBuy, canSell, displayCurrency, player);
            buyPrice = PriceHandler.getCurrentBuyPrice(shopId, key);
            sellPrice = PriceHandler.getCurrentSellPrice(shopId, key);

            if (!usedSlots.add(slot)) {
                System.out.println("[SushiShop] Duplicate slot in shop: " + shopId + " at slot " + slot);
                continue;
            }
            inv.setItem(slot, item);
        }

        player.openInventory(inv);
    }

    // ✅ เพิ่ม listener แบบ static สำหรับใช้งานใน ShopListener
    public static void handleClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ShopHolder shopHolder)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int clickedSlot = event.getRawSlot();
        int maxSlot = 0;
        if (shopHolder.getInventory() != null) {
            maxSlot = shopHolder.getInventory().getSize();
        }
        if (clickedSlot < 0 || clickedSlot >= maxSlot) return;

        ClickType click = event.getClick();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        String shopId = shopHolder.getShopId();
        List<String> keys = ShopConfig.getShopItems(shopId);
        for (String key : keys) {
            if (ShopConfig.getSlot(shopId, key) == clickedSlot) {
                if (click == ClickType.DROP) {
                    event.setCancelled(true);
                    if (!player.isOp()) {
                        player.sendMessage(ChatColor.RED + "You must be OP to edit shop items.");
                        return;
                    }
                    ShopEditorGUI.open(player, shopId, key);
                    return;
                }
                // การคลิกอื่น ๆ (ซื้อ/ขาย) จะจัดการใน ShopListener แยก
            }
        }
    }
}