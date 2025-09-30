package mcsushi.dynamicshop.sushidynamicshop.pricehandler;

import mcsushi.dynamicshop.sushidynamicshop.shop.ShopConfig;

public class PriceHandler {

    public static double calculateCurrentPrice(String shopId, String key) {
        double basePrice = ShopConfig.getBasePrice(shopId, key);
        double supply = ShopConfig.getSupply(shopId, key);
        double demand = ShopConfig.getDemand(shopId, key);
        double rate = ShopConfig.getPriceChangeRate(shopId, key);

        double rawPrice = basePrice + (basePrice * (demand - supply) * rate);
        double minPrice = ShopConfig.getMinPrice(shopId, key);
        double maxPrice = ShopConfig.getMaxPrice(shopId, key);

        org.bukkit.Bukkit.getLogger().info("[DEBUG] basePrice: " + basePrice + ", rawPrice: " + rawPrice);

        return Math.max(minPrice, Math.min(maxPrice, rawPrice));
    }

    public static double getCurrentBuyPrice(String shopId, String key) {
        double currentPrice = calculateCurrentPrice(shopId, key);
        double minPrice = ShopConfig.getMinPrice(shopId, key);
        double maxPrice = ShopConfig.getMaxPrice(shopId, key);

        org.bukkit.Bukkit.getLogger().info("[DEBUG] Current Buy Price - Shop: " + shopId + ", Key: " + key + ", Current Price: " + currentPrice);
        org.bukkit.Bukkit.getLogger().info("[DEBUG] Current Buy Price Calculation - Shop: " + shopId + ", Key: " + key + ", Price: " + currentPrice);

        return Math.max(minPrice, Math.min(maxPrice, currentPrice));
    }

    public static double getCurrentSellPrice(String shopId, String key) {
        double currentBuy = getCurrentBuyPrice(shopId, key);
        return currentBuy * 0.70;
    }

    public static double CallBasePrice(String shopId, String key) {
        return ShopConfig.getBasePrice(shopId, key);
    }
}