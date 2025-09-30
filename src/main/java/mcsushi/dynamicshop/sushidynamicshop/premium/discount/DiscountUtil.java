package mcsushi.dynamicshop.sushidynamicshop.premium.discount;

import mcsushi.dynamicshop.sushidynamicshop.pricehandler.PriceHandler;
import mcsushi.dynamicshop.sushidynamicshop.util.PremiumChecker;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.stream.Collectors;

public class DiscountUtil {

    public static double applyDiscount(Player player, double price) {
        if (!PremiumChecker.isPremium()) {
            return price;
        }

        double discountRate = 0.0;
        StringBuilder sourceLog = new StringBuilder();

        Set<String> permissions = player.getEffectivePermissions().stream()
                .map(permission -> permission.getPermission())
                .filter(perm -> perm.startsWith("dynamicshop.discount."))
                .collect(Collectors.toSet());

        for (String perm : permissions) {
            try {
                int value = Integer.parseInt(perm.replace("dynamicshop.discount.", ""));
                discountRate += value;
                sourceLog.append(perm).append(" ");
            } catch (NumberFormatException e) {
                // ไม่ต้องทำอะไร เพราะ permission ที่ไม่ใช่ตัวเลขจะถูกข้าม
            }
        }

        discountRate = Math.max(-100.0, Math.min(100.0, discountRate));

        double discountedPrice = price + (price * discountRate / 100);

        org.bukkit.Bukkit.getLogger().info("[DEBUG] Discounts: " + sourceLog.toString().trim() +
                " | Total: " + discountRate + "% | Final Price: " + discountedPrice);

        return discountedPrice;
    }

    public static double getDiscountedBuyPrice(Player player, String shopId, String key) {
        double currentBuy = PriceHandler.getCurrentBuyPrice(shopId, key);
        return applyDiscount(player, currentBuy);
    }

    public static double getDiscountedSellPrice(Player player, String shopId, String key) {
        double currentSell = PriceHandler.getCurrentSellPrice(shopId, key);
        return applyDiscount(player, currentSell);
    }
}