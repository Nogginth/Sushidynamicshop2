package mcsushi.dynamicshop.sushidynamicshop.pricehandler;

import mcsushi.dynamicshop.sushidynamicshop.shop.ShopCurrency;
import mcsushi.dynamicshop.sushidynamicshop.util.ExpUtil;
import mcsushi.dynamicshop.sushidynamicshop.util.PointsUtil;
import mcsushi.dynamicshop.sushidynamicshop.util.VaultUtil;
import org.bukkit.entity.Player;
import net.milkbowl.vault.economy.Economy;
import mcsushi.dynamicshop.sushidynamicshop.init.PremiumInitializer;
import mcsushi.dynamicshop.sushidynamicshop.util.CurrencyRegistry;
import java.util.UUID;

public class CurrencyHandler {

    /**
     * ตรวจสอบว่าผู้เล่นมีหน่วยเงินเพียงพอหรือไม่
     */
    public static boolean hasEnough(Player player, String currencyId, double amount) {
        currencyId = currencyId.toLowerCase();
        CurrencyRegistry registry = PremiumInitializer.getCurrencyRegistry();

        // ตรวจสอบว่า currencyId เป็น CUSTOM หรือไม่
        if (registry != null && registry.currencyExists(currencyId)) {
            UUID playerUUID = player.getUniqueId();
            String balanceStr = registry.getPlayerBalance(playerUUID, currencyId, "");

            double balance;
            try {
                balance = Double.parseDouble(balanceStr.replaceAll("[^0-9.]", ""));
            } catch (NumberFormatException e) {
                balance = 0.0;
            }

            return balance >= amount;
        }

        // ตรวจสอบ shopCurrency
        ShopCurrency shopCurrency;
        try {
            shopCurrency = ShopCurrency.valueOf(currencyId.toUpperCase());
        } catch (IllegalArgumentException e) {
            shopCurrency = ShopCurrency.VAULT;
        }

        switch (shopCurrency) {
            case EXP:
                return ExpUtil.hasEnough(player, (int) amount);

            case POINTS:
                return PointsUtil.hasPoints(player, (int) amount);

            case VAULT:
                Economy eco = VaultUtil.getEconomy();
                boolean hasEnough = eco != null && eco.has(player, amount);
                return hasEnough;

            default:
                return false;
        }
    }

    /**
     * หักหน่วยเงินจากผู้เล่น
     */
    public static boolean withdraw(Player player, String currencyId, double amount) {
        currencyId = currencyId.toLowerCase();
        CurrencyRegistry registry = PremiumInitializer.getCurrencyRegistry();

        // หากเป็น CUSTOM Currency
        if (registry != null && registry.currencyExists(currencyId)) {
            UUID playerUUID = player.getUniqueId();
            boolean success = registry.adjustPlayerBalance(playerUUID, currencyId, amount, "remove");
            return success;
        }

        // หากเป็น ENUM (EXP, POINTS, VAULT)
        ShopCurrency shopCurrency;
        try {
            shopCurrency = ShopCurrency.valueOf(currencyId.toUpperCase());
        } catch (IllegalArgumentException e) {
            shopCurrency = ShopCurrency.VAULT;
        }

        switch (shopCurrency) {
            case EXP:
                return ExpUtil.removeLevel(player, (int) amount);
            case POINTS:
                return PointsUtil.withdrawPoints(player, (int) amount);
            case VAULT:
                Economy eco = VaultUtil.getEconomy();
                boolean success = eco != null && eco.withdrawPlayer(player, amount).transactionSuccess();
                return success;
            default:
                return false;
        }
    }

    /**
     * เพิ่มหน่วยเงินให้กับผู้เล่น
     */
    public static void deposit(Player player, String currencyId, double amount) {
        currencyId = currencyId.toLowerCase();
        CurrencyRegistry registry = PremiumInitializer.getCurrencyRegistry();

        // หากเป็น CUSTOM Currency
        if (registry != null && registry.currencyExists(currencyId)) {
            UUID playerUUID = player.getUniqueId();
            registry.adjustPlayerBalance(playerUUID, currencyId, amount, "give");
            return;
        }

        // หากเป็น ENUM (EXP, POINTS, VAULT)
        ShopCurrency shopCurrency;
        try {
            shopCurrency = ShopCurrency.valueOf(currencyId.toUpperCase());
        } catch (IllegalArgumentException e) {
            shopCurrency = ShopCurrency.VAULT;
        }

        switch (shopCurrency) {
            case EXP:
                ExpUtil.addLevel(player, (int) amount);
                break;
            case POINTS:
                PointsUtil.depositPoints(player, (int) amount);
                break;
            case VAULT:
                Economy eco = VaultUtil.getEconomy();
                if (eco != null) {
                    eco.depositPlayer(player, amount);
                }
                break;
        }
    }

    /**
     * ดึงข้อมูลจำนวนเงินคงเหลือ
     */
    public static double getBalance(Player player, String currencyId) {
        currencyId = currencyId.toLowerCase();
        ShopCurrency shopCurrency = ShopCurrency.fromString(currencyId);

        if (shopCurrency == ShopCurrency.CUSTOM) {
            CurrencyRegistry registry = PremiumInitializer.getCurrencyRegistry();
            if (registry != null) {
                UUID playerUUID = player.getUniqueId();
                String balanceStr = registry.getPlayerBalance(playerUUID, currencyId, "").replaceAll("[^0-9.]", "");
                return balanceStr.isEmpty() ? 0.0 : Double.parseDouble(balanceStr);
            }
            return 0.0;
        }

        switch (shopCurrency) {
            case EXP:
                return ExpUtil.getLevel(player);
            case POINTS:
                return PointsUtil.getPoints(player);
            case VAULT:
                Economy eco = VaultUtil.getEconomy();
                return eco != null ? eco.getBalance(player) : 0.0;
            default:
                return 0.0;
        }
    }
}
