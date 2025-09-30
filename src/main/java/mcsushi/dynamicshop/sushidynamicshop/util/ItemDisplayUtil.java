package mcsushi.dynamicshop.sushidynamicshop.util;

import mcsushi.dynamicshop.sushidynamicshop.init.PremiumInitializer;
import mcsushi.dynamicshop.sushidynamicshop.pricehandler.PriceHandler;
import mcsushi.dynamicshop.sushidynamicshop.shop.ShopConfig;
import org.bukkit.ChatColor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Bukkit;

public class ItemDisplayUtil {

    private static final Pattern unicodePattern = Pattern.compile("[⌀-⏿■-◿⭐-⯿]");

    public static String resolveName(FileConfiguration config, String itemKey, ItemStack item, boolean isMMOItem) {
        if (config.contains("items." + itemKey + ".name")) {
            String customName = config.getString("items." + itemKey + ".name");
            if (customName != null && !customName.trim().isEmpty()) {
                return customName;
            }
        }

        if (isMMOItem) {
            return getFilteredMMOItemName(item);
        } else {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                return meta.getDisplayName();
            } else {
                return formatMaterialName(item.getType());
            }
        }
    }

    private static String formatMaterialName(Material mat) {
        String name = mat.name().toLowerCase().replace("_", " ");
        String[] parts = name.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
        }
        return builder.toString().trim();
    }

    private static String getFilteredMMOItemName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return unicodePattern.matcher(meta.getDisplayName()).replaceAll("").trim();
        }
        return "Unnamed MMOItem";
    }


    public static void appendShopLore(ItemStack item, String shopId, String itemId, double basePrice, double supply, double demand, boolean canBuy, boolean canSell, String currency, Player player) {
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(item.getType());
            if (meta == null) return;
        }

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");

        boolean isPremium = PremiumChecker.isPremium();

        // ดึงรายการ key จาก ShopConfig
        List<String> keys = ShopConfig.getShopItems(shopId);
        if (keys.isEmpty()) return;

        // ประกาศตัวแปรนอก for-loop
        String key = "";
        double currentBuyPrice = 0.0;
        double currentSellPrice = 0.0;
        double discountedBuyPrice = 0.0;
        double discountedSellPrice = 0.0;
        String fluctuationBuyDisplay = "";
        String fluctuationSellDisplay = "";

        // Loop เพื่อกำหนดค่า
        for (String k : keys) {
            key = k;

            // ดึง currentBuyPrice และ currentSellPrice
            currentBuyPrice = PriceHandler.getCurrentBuyPrice(shopId, key);
            currentSellPrice = PriceHandler.getCurrentSellPrice(shopId, key);

            // ดึง discountedBuyPrice และ discountedSellPrice
            discountedBuyPrice = isPremium ? PremiumInitializer.CallBuyDiscount(player, shopId, key) : currentBuyPrice;
            discountedSellPrice = isPremium ? PremiumInitializer.CallSellDiscount(player, shopId, key) : currentSellPrice;

            // Fluctuation Buy
            fluctuationBuyDisplay = isPremium
                    ? PremiumInitializer.CallFluctuationDisplay(basePrice, discountedBuyPrice)
                    : PremiumInitializer.CallFluctuationDisplay(basePrice, currentBuyPrice);

            // Fluctuation Sell
            fluctuationSellDisplay = isPremium
                    ? PremiumInitializer.CallFluctuationDisplay(basePrice, discountedSellPrice)
                    : PremiumInitializer.CallFluctuationDisplay(basePrice, currentSellPrice);
        }

        boolean hasDiscount = PremiumInitializer.hasDiscount(player);

        // แปลคำว่า "Buy" และ "Sell"
        String buyLabel = TranslationUtil.get("buy");
        String sellLabel = TranslationUtil.get("sell");

        // แสดงผล Buy
        if (canBuy) {
            // สร้าง currencyDisplay
            String currencyDisplay = currency.isEmpty() ? "" : " " + currency;
            String buyLine;

            org.bukkit.Bukkit.getLogger().info(
                    "[DEBUG] [ItemDisplayUtil - appendShopLore] Shop: " + shopId +
                            ", Key: " + key +
                            ", Before BuyLine - Current Buy Price: " + currentBuyPrice +
                            ", Currency: " + currency +
                            ", isPremium: " + isPremium +
                            ", hasDiscount: " + hasDiscount
            );

            // Case 1: Free Version (ไม่ใช่ Premium)
            if (!isPremium) {
                buyLine = String.format(
                        "%s: %.2f%s",
                        buyLabel,
                        currentBuyPrice,
                        currencyDisplay
                ).trim();
            }
            // Case 2: Premium แต่ไม่มี Discount หรือ Discount = 0%
            else if (isPremium && !hasDiscount) {
                buyLine = String.format(
                        "%s: %.2f%s %s",
                        buyLabel,
                        currentBuyPrice,
                        currencyDisplay,
                        (fluctuationBuyDisplay != null ? fluctuationBuyDisplay : "")
                ).trim();
            }
            // Case 3: Premium และมี Discount (Discount != 0%)
            else {
                // ตรวจสอบว่า discountBuyPrice ต่างจาก currentBuyPrice หรือไม่
                if (discountedBuyPrice != currentBuyPrice) {
                    String basePriceDisplay = "&m" + String.format("%.2f", currentBuyPrice) + "&r ";
                    buyLine = String.format(
                            "%s: %s%.2f%s %s",
                            buyLabel,
                            basePriceDisplay,
                            discountedBuyPrice,
                            currencyDisplay,
                            fluctuationBuyDisplay
                    ).trim();
                } else {
                    // หาก Discount = 0% ให้แสดงผลเหมือน Case 2
                    buyLine = String.format(
                            "%s: %.2f%s %s",
                            buyLabel,
                            currentBuyPrice,
                            currencyDisplay,
                            fluctuationBuyDisplay
                    ).trim();
                }
            }

            org.bukkit.Bukkit.getLogger().info("[DEBUG] [ItemDisplayUtil - appendShopLore] Final BuyLine - " + buyLine);
            // แปลงสีและเพิ่มลงใน Lore
            lore.add(ChatColor.translateAlternateColorCodes('&', buyLine));
        }

        // แสดงผล Sell
        if (canSell) {
            // สร้าง currencyDisplay
            String currencyDisplay = currency.isEmpty() ? "" : " " + currency;
            String sellLine;

            // Case 1: Free Version (ไม่ใช่ Premium)
            if (!isPremium) {
                sellLine = String.format(
                        "%s: %.2f%s",
                        sellLabel,
                        currentSellPrice,
                        currencyDisplay
                ).trim();
            }
            // Case 2: Premium แต่ไม่มี Discount หรือ Discount = 0%
            else if (isPremium && !hasDiscount) {
                sellLine = String.format(
                        "%s: %.2f%s %s",
                        sellLabel,
                        currentSellPrice,
                        currencyDisplay,
                        (fluctuationSellDisplay != null ? fluctuationSellDisplay : "")
                ).trim();
            }
            // Case 3: Premium และมี Discount
            else {
                // ตรวจสอบว่า discountSellPrice ต่างจาก currentSellPrice หรือไม่
                if (discountedSellPrice != currentSellPrice) {
                    String basePriceDisplay = "&m" + String.format("%.2f", currentSellPrice) + "&r ";
                    sellLine = String.format(
                            "%s: %s%.2f%s %s",
                            sellLabel,
                            basePriceDisplay,
                            discountedSellPrice,
                            currencyDisplay,
                            fluctuationSellDisplay
                    ).trim();
                } else {
                    // หาก Discount = 0% ให้แสดงผลเหมือน Case 2
                    sellLine = String.format(
                            "%s: %.2f%s %s",
                            sellLabel,
                            currentSellPrice,
                            currencyDisplay,
                            fluctuationSellDisplay
                    ).trim();
                }
            }

            // แปลงสีและเพิ่มลงใน Lore
            lore.add(ChatColor.translateAlternateColorCodes('&', sellLine));
        }

        // Supply/Demand Display
        lore.add(TranslationUtil.get("supply_demand", Map.of(
                "stock", String.valueOf((int) supply),
                "demand", String.valueOf((int) demand)
        )));
        lore.add(TranslationUtil.get("left_click_buy"));
        lore.add(TranslationUtil.get("shift_left_click_buy_all"));
        lore.add(TranslationUtil.get("right_click_sell"));
        lore.add(TranslationUtil.get("shift_right_click_sell_all"));

        meta.setLore(lore);
        item.setItemMeta(meta);
    }
}
