package mcsushi.dynamicshop.sushidynamicshop.premium.guidisplay;

import mcsushi.dynamicshop.sushidynamicshop.premium.discount.DiscountUtil;
import mcsushi.dynamicshop.sushidynamicshop.pricehandler.PriceHandler;
import mcsushi.dynamicshop.sushidynamicshop.util.PremiumChecker;
import mcsushi.dynamicshop.sushidynamicshop.util.TranslationUtil;
import org.bukkit.entity.Player;

public class PriceFluctuationDisplay {

    public static String getFluctuationDisplay(Player player, String shopId, String key, boolean isBuy) {
        double basePrice = PriceHandler.CallBasePrice(shopId, key);

        if (Double.isNaN(basePrice) || basePrice == 0) {
            return TranslationUtil.getFluctuation(Double.NaN);
        }

        // คำนวณ Fluctuation
        double fluctuation = calculateFluctuation(player, shopId, key, isBuy);

        // แสดงผล Fluctuation ผ่าน TranslationUtil
        if (Double.isNaN(fluctuation)) {
            return TranslationUtil.getFluctuation(Double.NaN);
        } else if (fluctuation == 0) {
            return TranslationUtil.getFluctuation(0.0);
        } else {
            return TranslationUtil.getFluctuation(fluctuation);
        }
    }

    private static double calculateFluctuation(Player player, String shopId, String key, boolean isBuy) {
        double basePrice = PriceHandler.CallBasePrice(shopId, key);
        double currentPrice = 0.0;

        // เช็ค Premium Status
        boolean isPremium = PremiumChecker.isPremium();

        // ใช้ Switch Case แยกการคำนวณ
        switch (isPremium ? 1 : 0) {
            case 1:
                // ผู้เล่นมีส่วนลด
                currentPrice = isBuy ?
                        DiscountUtil.getDiscountedBuyPrice(player, shopId, key) :
                        DiscountUtil.getDiscountedSellPrice(player, shopId, key);
                break;

            case 0:
            default:
                // ไม่มีส่วนลด
                currentPrice = isBuy ?
                        PriceHandler.getCurrentBuyPrice(shopId, key) :
                        PriceHandler.getCurrentSellPrice(shopId, key);
                break;
        }

        // คำนวณ Fluctuation
        if (Double.isNaN(currentPrice) || basePrice == 0) {
            return Double.NaN;
        }

        return ((currentPrice - basePrice) / basePrice) * 100;
    }
}
