package mcsushi.dynamicshop.sushidynamicshop.util;

import java.util.logging.Logger;

public class PremiumChecker {

    private static boolean isPremium = false;

    public static void checkPremium(Logger logger) {
        try {
            Class.forName("mcsushi.dynamicshop.sushidynamicshop.premium.database.DatabaseConfig");
            isPremium = true;
        } catch (ClassNotFoundException e) {
            isPremium = false;
        }
    }

    public static boolean isPremium() {
        return isPremium;
    }
}
