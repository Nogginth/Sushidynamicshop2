package mcsushi.dynamicshop.sushidynamicshop.gui;

import mcsushi.dynamicshop.sushidynamicshop.editor.CategoryEditorGUI;
import mcsushi.dynamicshop.sushidynamicshop.hook.MMOItemHook;
import mcsushi.dynamicshop.sushidynamicshop.init.PremiumInitializer;
import mcsushi.dynamicshop.sushidynamicshop.pricehandler.CurrencyHandler;
import mcsushi.dynamicshop.sushidynamicshop.shop.ShopConfig;
import mcsushi.dynamicshop.sushidynamicshop.pricehandler.PriceHandler;
import mcsushi.dynamicshop.sushidynamicshop.shop.ShopCurrency;
import mcsushi.dynamicshop.sushidynamicshop.util.*;
import mcsushi.dynamicshop.sushidynamicshop.config.CategoryConfig;
import io.lumine.mythic.lib.api.item.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import org.bukkit.ChatColor;

public class ShopListener implements Listener {

    private ItemStack createItem(String source) {
        if (source == null || source.trim().isEmpty()) {
            return new ItemStack(Material.BARRIER);
        }

        source = source.toLowerCase();

        try {
            if (source.startsWith("mmoitem:")) {
                if (MMOItemHook.isHooked()) {
                    ItemStack item = MMOItemFactory.createItem(source);
                    if (item != null) return item;
                }
                return new ItemStack(Material.BARRIER);
            }

            if (source.startsWith("itemadder:")) {
                String id = source.substring("itemadder:".length());
                ItemStack item = ItemAdderUtil.createItem(id, 1);
                if (item != null) return item;
            }

            return VanillaItemFactory.createItem(source);

        } catch (Exception e) {
            e.printStackTrace();
            return new ItemStack(Material.BARRIER);
        }
    }

    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();

        if (top.getHolder() instanceof CategoryMenuHolder) {
            if (event.getRawSlot() < top.getSize()) {
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof Player player) {
                    int slot = event.getSlot();

                    // กำหนดตำแหน่ง Close ตามขนาดของ Category GUI
                    int closeSlot;
                    switch (top.getSize()) {
                        case 27 -> closeSlot = 22;
                        case 36 -> closeSlot = 31;
                        case 45 -> closeSlot = 40;
                        case 54 -> closeSlot = 49;
                        default -> closeSlot = -1;
                    }

                    // ตรวจจับปุ่ม Close
                    if (slot == closeSlot) {
                        player.closeInventory();
                        return;
                    }

                    // เปิด Category Editor หากคลิก Shift + Right
                    if (event.getClick() == ClickType.SHIFT_RIGHT && player.isOp()) {
                        String categoryId = CategoryConfig.getCategoryIdBySlot(slot);
                        if (categoryId != null) {
                            CategoryEditorGUI.open(player, categoryId);
                            return;
                        }
                    }

                    // การจัดการคลิกปุ่มอื่น ๆ
                    CategoryMenuGUI.handleClick(player, slot);
                }
            }
            return;
        }

        if (top.getHolder() instanceof ShopHolder holder) {
            if (event.getRawSlot() < top.getSize()) {
                event.setCancelled(true);

                // ✅ เรียก ShopGUI.handleClick เพื่อจัดการ ClickType.DROP
                ShopGUI.handleClick(event);

                if (!(event.getWhoClicked() instanceof Player player)) return;
                int slot = event.getSlot();
                int backSlot;
                switch (top.getSize()) {
                    case 27 -> backSlot = 22;
                    case 36 -> backSlot = 31;
                    case 45 -> backSlot = 40;
                    case 54 -> backSlot = 49;
                    default -> {
                        Bukkit.getLogger().warning("[Sushidynamicshop] Invalid slot size: " + top.getSize());
                        return;
                    }
                }

                if (slot == backSlot) {
                    event.setCancelled(true);
                    CategoryMenuGUI.open(player);
                    return;
                }

                String shopId = holder.getShopId();
                // ✅ Q to add new item logic
                if (event.getClick() == ClickType.DROP) {
                    if (!player.isOp()) return;
                    if (!player.isOp()) return;
                    String existingKey = ShopConfig.getShopItems(shopId).stream()
                            .filter(k -> ShopConfig.getSlot(shopId, k) == slot)
                            .findFirst().orElse(null);
                    if (existingKey != null) {
                        mcsushi.dynamicshop.sushidynamicshop.editor.ShopEditorGUI.open(player, shopId, existingKey);
                        return;
                    }

                    String key = "new_item_" + System.currentTimeMillis();
                    org.bukkit.configuration.file.FileConfiguration config = ShopConfig.getShopConfig(shopId);
                    org.bukkit.configuration.ConfigurationSection section = config.createSection(key);
                    section.set("source", "STONE");
                    section.set("slot", slot);
                    section.set("base_price", 10.0);
                    section.set("current_price", 10.0);
                    section.set("buy_multiplier", 1.0);
                    section.set("sell_multiplier", 0.7);
                    section.set("min_price", 1.0);
                    section.set("max_price", 100.0);
                    section.set("price_change_rate", 0.5);
                    section.set("supply", 0);
                    section.set("demand", 0);
                    section.set("buy_enabled", true);
                    section.set("sell_enabled", true);
                    ShopConfig.getShopConfigMap().put(shopId, config);
                    try {
                        config.save(new java.io.File("plugins/Sushidynamicshop/shop/" + shopId + ".yml"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mcsushi.dynamicshop.sushidynamicshop.editor.ShopEditorGUI.open(player, shopId, key);
                    return;
                }

                String key = ShopConfig.getShopItems(shopId).stream()
                        .filter(k -> ShopConfig.getSlot(shopId, k) == slot)
                        .findFirst().orElse(null);
                if (key == null) return;

                org.bukkit.Bukkit.getLogger().info("[DEBUG] [ShopListener - onGUIClick] Clicked Slot: " + slot + ", Key: " + key);

                String source = ShopConfig.getSource(shopId, key);
                if (source != null && source.toLowerCase().startsWith("itemadder:")) return;
                if (source != null && source.toLowerCase().startsWith("nexo:")) return;

                boolean isShift = event.getClick().isShiftClick();
                boolean isLeft = event.isLeftClick();
                boolean isRight = event.isRightClick();

                ItemStack previewItem = createItem(source);

                int maxStack = previewItem.getMaxStackSize();
                int amount = isShift ? maxStack : 1;

                if (isLeft && ShopConfig.canBuy(shopId, key)) {

                    boolean realSupply = ShopConfig.isRealSupply(shopId, key);
                    int supply = (int) ShopConfig.getSupply(shopId, key);

                    if (realSupply && supply == 0) {
                        player.sendMessage(TranslationUtil.get("out_of_stock"));
                        event.setCancelled(true);
                        return;
                    }

                    double totalPrice = 0.0;

                    // ตรวจสอบ Discount
                    boolean hasDiscount = PremiumInitializer.hasDiscount(player);

                    // ดึงราคาต่อชิ้น (CurrentBuyPrice)
                    double currentBuyPrice = PriceHandler.getCurrentBuyPrice(shopId, key);

                    // คำนวณราคาแบบต่อชิ้น (ไม่รวม Discount)
                    for (int i = 0; i < amount; i++) {
                        totalPrice += currentBuyPrice;
                    }

                    // ถ้าผู้เล่นมี Discount ให้คำนวณ Discount ที่นี่
                    if (hasDiscount) {
                        totalPrice = PremiumInitializer.CallBuyDiscount(player, shopId, key) * amount;
                    }

                    // Debug Logging
                    org.bukkit.Bukkit.getLogger().info("[DEBUG] Total Buy Price: " + totalPrice);

                    ShopCurrency shopCurrency = ShopConfig.getCurrency(shopId);
                    String customCurrencyId = ShopConfig.getCustomCurrencyId(shopId);

                    boolean success = false;

                    if (shopCurrency == ShopCurrency.CUSTOM) {
                        if ("no_currency_set".equals(customCurrencyId)) {
                            player.sendMessage(TranslationUtil.get("no_currency_set"));
                            return;
                        }

                        CurrencyRegistry registry = PremiumInitializer.getCurrencyRegistry();
                        boolean exists = registry.currencyExists(customCurrencyId);

                        if (!exists) {
                            player.sendMessage(TranslationUtil.get("no_currency_set"));
                            return;
                        }

                        String pronoun = (registry != null) ? registry.getPronoun(customCurrencyId) : customCurrencyId;

                        boolean hasEnough = CurrencyHandler.hasEnough(player, customCurrencyId, totalPrice);

                        if (!hasEnough) {
                            player.sendMessage(TranslationUtil.get("not_enough_custom", Map.of(
                                    "currency", pronoun,
                                    "required", String.format("%.2f", totalPrice)
                            )));
                            return;
                        }

                        success = CurrencyHandler.withdraw(player, customCurrencyId, totalPrice);

                    } else {
                        boolean hasEnough = CurrencyHandler.hasEnough(player, shopCurrency.name(), totalPrice);

                        if (!hasEnough) {
                            String messageKey = switch (shopCurrency) {
                                case EXP -> "not_enough_level";
                                case POINTS -> "not_enough_points";
                                default -> "not_enough_money";
                            };

                            player.sendMessage(TranslationUtil.get(messageKey, Map.of(
                                    "required", String.format("%.2f", totalPrice)
                            )));
                            return;
                        }

                        success = CurrencyHandler.withdraw(player, shopCurrency.name(), totalPrice);
                    }

                    if (success) {
                        ItemStack give = createItem(source);

                        if (give.getType() == Material.BARRIER) {
                            player.sendMessage(ChatColor.RED + "You cannot buy this item.");
                            return;
                        }

                        give.setAmount(amount);

                        Map<Integer, ItemStack> remaining = player.getInventory().addItem(give);

                        if (!remaining.isEmpty()) {
                            player.sendMessage(TranslationUtil.get("not_enough_space"));
                            return;
                        }

                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1.5f);
                        player.sendMessage(TranslationUtil.get("bought"));

                        if (realSupply) {
                            int remainingSupply = supply - amount;
                            remainingSupply = Math.max(0, remainingSupply);
                            ShopConfig.setSupply(shopId, key, remainingSupply);
                        }

                        ShopConfig.incrementDemand(shopId, key, amount);

                        ShopGUI.open(player, shopId);

                    }
                }

                if (isRight && ShopConfig.canSell(shopId, key)) {
                    int playerItemCount = countItem(player, source);

                    if (playerItemCount == 0) {
                        player.sendMessage(TranslationUtil.get("no_item_to_sell"));
                        return;
                    }

                    int actualAmount = Math.min(amount, playerItemCount);
                    double totalPrice = 0.0;

                    // ตรวจสอบ Discount
                    boolean hasDiscount = PremiumInitializer.hasDiscount(player);

                    // ดึงราคาต่อชิ้น (CurrentSellPrice)
                    double currentSellPrice = PriceHandler.getCurrentSellPrice(shopId, key);

                    // คำนวณราคาแบบต่อชิ้น (ไม่รวม Discount)
                    for (int i = 0; i < amount; i++) {
                        totalPrice += currentSellPrice;
                    }

                    // ถ้าผู้เล่นมี Discount ให้คำนวณ Discount ที่นี่
                    if (hasDiscount) {
                        totalPrice = PremiumInitializer.CallSellDiscount(player, shopId, key) * amount;
                    }

                    // Debug Logging
                    org.bukkit.Bukkit.getLogger().info("[DEBUG] Total Buy Price (หลัง Discount): " + totalPrice);

                    ShopCurrency shopCurrency = ShopConfig.getCurrency(shopId);
                    String customCurrencyId = ShopConfig.getCustomCurrencyId(shopId);

                    boolean success = false;

                    if (shopCurrency == ShopCurrency.CUSTOM) {
                        if ("no_currency_set".equals(customCurrencyId)) {
                            player.sendMessage(TranslationUtil.get("no_currency_set"));
                            return;
                        }

                        CurrencyRegistry registry = PremiumInitializer.getCurrencyRegistry();
                        boolean exists = registry.currencyExists(customCurrencyId);

                        if (!exists) {
                            player.sendMessage(TranslationUtil.get("no_currency_set"));
                            return;
                        }

                        String pronoun = (registry != null) ? registry.getPronoun(customCurrencyId) : customCurrencyId;

                        CurrencyHandler.deposit(player, customCurrencyId, totalPrice);

                        player.sendMessage(TranslationUtil.get("sold_custom", Map.of(
                                "currency", pronoun,
                                "amount", String.format("%.2f", totalPrice)
                        )));

                        success = true;

                    } else {
                        CurrencyHandler.deposit(player, shopCurrency.name(), totalPrice);

                        String messageKey = switch (shopCurrency) {
                            case EXP -> "sold_exp";
                            case POINTS -> "sold_points";
                            default -> "sold";
                        };

                        player.sendMessage(TranslationUtil.get(messageKey, Map.of(
                                "amount", String.format("%.2f", totalPrice)
                        )));

                        success = true;
                    }

                    if (success) {
                        removeItem(player, source, actualAmount);
                        ShopConfig.incrementSupply(shopId, key, actualAmount);
                        ShopGUI.open(player, shopId);
                    }
                }
            }
        }
    }

    private int countItem(Player player, String source) {
        ItemStack target = createItem(source);

        // ✅ ตรวจ BARRIER
        if (target.getType() == Material.BARRIER) {
            return 0;
        }

        // ✅ ป้องกัน hard crash: ห้าม import/use NBTItem หากไม่ได้ hook
        boolean isMMO = source.toLowerCase().startsWith("mmoitem:");

        int total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;

            if (isMMO) {
                try {
                    io.lumine.mythic.lib.api.item.NBTItem nbtTarget = io.lumine.mythic.lib.api.item.NBTItem.get(target);
                    io.lumine.mythic.lib.api.item.NBTItem nbt = io.lumine.mythic.lib.api.item.NBTItem.get(item);
                    if (nbt.hasType()
                            && nbt.getType().equals(nbtTarget.getType())
                            && nbt.getString("MMOITEMS_ITEM_ID").equals(nbtTarget.getString("MMOITEMS_ITEM_ID"))) {
                        total += item.getAmount();
                    }
                } catch (Throwable ignored) {
                    // ไม่โหลด MMOItem แต่หลุดมาถึงนี้ → ป้องกัน plugin พัง
                }
            } else if (item.isSimilar(target)) {
                total += item.getAmount();
            }
        }

        return total;
    }


    private void removeItem(Player player, String source, int amount) {
        ItemStack target = createItem(source);

        // ✅ หยุดทันทีหาก item สร้างไม่สำเร็จ
        if (target.getType() == Material.BARRIER) {
            return;
        }

        boolean isMMO = source.toLowerCase().startsWith("mmoitem:");
        io.lumine.mythic.lib.api.item.NBTItem nbtTarget = null;
        if (isMMO) {
            try {
                nbtTarget = io.lumine.mythic.lib.api.item.NBTItem.get(target);
            } catch (Throwable ignored) {
                return;
            }
        }

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null) continue;

            boolean match = false;
            if (isMMO && nbtTarget != null) {
                try {
                    io.lumine.mythic.lib.api.item.NBTItem nbt = io.lumine.mythic.lib.api.item.NBTItem.get(item);
                    if (nbt.hasType()
                            && nbt.getType().equals(nbtTarget.getType())
                            && nbt.getString("MMOITEMS_ITEM_ID").equals(nbtTarget.getString("MMOITEMS_ITEM_ID"))) {
                        match = true;
                    }
                } catch (Throwable ignored) {
                    continue;
                }
            } else if (item.isSimilar(target)) {
                match = true;
            }

            if (match) {
                int remove = Math.min(amount, item.getAmount());
                item.setAmount(item.getAmount() - remove);
                amount -= remove;
                if (amount <= 0) break;
            }
        }
    }



    @EventHandler
    public void onGUIDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top.getHolder() instanceof CategoryMenuHolder || top.getHolder() instanceof ShopHolder) {
            event.setCancelled(true);
        }
    }
}