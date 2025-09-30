package mcsushi.dynamicshop.sushidynamicshop.shop;

public enum ShopCurrency {
    VAULT,
    EXP,
    POINTS,
    CUSTOM;

    public static ShopCurrency fromString(String str) {
        if (str == null) return VAULT;

        try {
            return valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            if (str.equalsIgnoreCase("CUSTOM")) {
                return CUSTOM;
            }
            return VAULT;
        }
    }
}
