package mcsushi.dynamicshop.sushidynamicshop.init;

import mcsushi.dynamicshop.sushidynamicshop.Sushidynamicshop;
import mcsushi.dynamicshop.sushidynamicshop.util.CurrencyRegistry;
import mcsushi.dynamicshop.sushidynamicshop.util.DefaultCurrencyRegistry;
import mcsushi.dynamicshop.sushidynamicshop.util.PremiumChecker;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.logging.Logger;
import java.lang.reflect.Method;
import java.util.stream.Collectors;


public class PremiumInitializer {

    private static final Logger logger = Sushidynamicshop.getLoggerInstance();
    private static Class<?> fluctuationClass;
    private static CurrencyRegistry currencyRegistry = new DefaultCurrencyRegistry();


    public static void init() {
        logger.info("[PremiumInitializer] Initializing Premium Features...");

        try {
            fluctuationClass = Class.forName("mcsushi.dynamicshop.sushidynamicshop.premium.guidisplay.PriceFluctuationDisplay");
            logger.info("[PremiumInitializer] Price Fluctuation Display loaded successfully.");
        } catch (ClassNotFoundException e) {
            logger.info("[PremiumInitializer] Disabled Price Fluctuation Display.");
        }

        try {
            Class<?> registryClass = Class.forName("mcsushi.dynamicshop.sushidynamicshop.premium.customcurrency.CurrencyRegistryImpl");
            currencyRegistry = (CurrencyRegistry) registryClass.getDeclaredConstructor().newInstance();
            logger.info("[PremiumInitializer] Custom Currency loaded successfully.");
        } catch (ClassNotFoundException e) {
            logger.info("[PremiumInitializer] Using Default Custom Currency.");
        } catch (Exception e) {
            logger.warning("[PremiumInitializer] Error initializing CurrencyRegistry: " + e.getMessage());
        }

        try {
            fluctuationClass = Class.forName("mcsushi.dynamicshop.sushidynamicshop.premium.guidisplay.PriceFluctuationDisplay");
        } catch (ClassNotFoundException e) {
            logger.warning("[PremiumInitializer] PriceFluctuationDisplay class not found. Premium features are not available.");
        }
    }

    public static String CallFluctuationDisplay(double basePrice, double currentPrice) {
        org.bukkit.Bukkit.getLogger().info("[DEBUG] Fluctuation Input - Base: " + basePrice + ", Current: " + currentPrice);

        if (fluctuationClass == null) {
            return "N/A";
        }

        try {
            Method method = fluctuationClass.getMethod("getFluctuationDisplay", double.class, double.class);
            return (String) method.invoke(null, basePrice, currentPrice);

        } catch (Exception e) {
            logger.warning("[PremiumInitializer] Error invoking getFluctuationDisplay: " + e.getMessage());
            return "N/A";
        }
    }

    public static CurrencyRegistry getCurrencyRegistry() {
        return currencyRegistry;
    }

    public static double CallBuyDiscount(Player player, String shopId, String key) {
        try {
            Class<?> discountClass = Class.forName("mcsushi.dynamicshop.sushidynamicshop.premium.discount.DiscountUtil");
            Method method = discountClass.getMethod("getDiscountedBuyPrice", Player.class, String.class, String.class);
            return (double) method.invoke(null, player, shopId, key);
        } catch (Exception e) {
            logger.warning("[PremiumInitializer] Error invoking getDiscountedBuyPrice: " + e.getMessage());
            return 0.0;
        }
    }

    public static double CallSellDiscount(Player player, String shopId, String key) {
        try {
            Class<?> discountClass = Class.forName("mcsushi.dynamicshop.sushidynamicshop.premium.discount.DiscountUtil");
            Method method = discountClass.getMethod("getDiscountedSellPrice", Player.class, String.class, String.class);
            return (double) method.invoke(null, player, shopId, key);
        } catch (Exception e) {
            logger.warning("[PremiumInitializer] Error invoking getDiscountedSellPrice: " + e.getMessage());
            return 0.0;
        }
    }

    public static boolean hasDiscount(Player player) {
        // หากผู้เล่นไม่ใช่ Premium ก็ไม่มีส่วนลด
        if (!PremiumChecker.isPremium()) {
            return false;
        }

        // ตรวจสอบ Permission ที่ขึ้นต้นด้วย "dynamicshop.discount."
        return player.getEffectivePermissions().stream()
                .map(permission -> permission.getPermission())
                .anyMatch(perm -> perm.startsWith("dynamicshop.discount."));
    }
}
