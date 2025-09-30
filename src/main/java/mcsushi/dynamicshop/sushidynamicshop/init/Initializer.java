package mcsushi.dynamicshop.sushidynamicshop.init;

import mcsushi.dynamicshop.sushidynamicshop.util.PremiumChecker;

import java.util.logging.Logger;

/**
 * Initializer - ตัวจัดการการเริ่มต้นของระบบ
 */
public class Initializer {

    private final Logger logger;

    public Initializer(Logger logger) {
        this.logger = logger;
    }

    public void init() {
        logger.info("[Initializer] Starting initialization...");

        // ✅ ตรวจสอบ Premium Status
        PremiumChecker.checkPremium(logger);

        // ✅ โหลด Premium DatabaseManager ผ่าน Dynamic Loading
        try {
            Class<?> dbManagerClass = Class.forName("mcsushi.dynamicshop.sushidynamicshop.premium.database.DatabaseManager");
            dbManagerClass.getMethod("init").invoke(null);
        } catch (ClassNotFoundException e) {
            logger.info("[Initializer] Premium Feature not found. Skipping initialization.");
        } catch (Exception e) {
            logger.warning("[Initializer] Error initializing DatabaseManager: " + e.getMessage());
        }

        // ✅ โหลด Premium Features
        try {
            Class<?> premiumInitClass = Class.forName("mcsushi.dynamicshop.sushidynamicshop.init.PremiumInitializer");
            premiumInitClass.getMethod("init").invoke(null);
        } catch (ClassNotFoundException e) {
            logger.info("[Initializer] Premium features not found.");
        } catch (Exception e) {
            logger.info("[Initializer] Error initializing premium features: " + e.getMessage());
        }

        logger.info("[Initializer] Initialization complete.");
    }

    public void shutdown() {
        logger.info("[Initializer] Shutting down system...");

        try {
            Class<?> dbManagerClass = Class.forName("mcsushi.dynamicshop.sushidynamicshop.premium.database.DatabaseManager");
            dbManagerClass.getMethod("shutdown").invoke(null);
        } catch (ClassNotFoundException e) {
            logger.info("[Initializer] Premium DatabaseManager not found. Skipping shutdown.");
        } catch (Exception e) {
            logger.warning("[Initializer] Error during shutdown: " + e.getMessage());
        }

        logger.info("[Initializer] Shutdown complete.");
    }
}
