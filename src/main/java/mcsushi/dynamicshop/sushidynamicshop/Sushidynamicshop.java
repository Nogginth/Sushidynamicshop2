package mcsushi.dynamicshop.sushidynamicshop;

import mcsushi.dynamicshop.sushidynamicshop.bstats.Metrics;
import mcsushi.dynamicshop.sushidynamicshop.command.SushishopCommand;
import mcsushi.dynamicshop.sushidynamicshop.command.currency.CurrencyRemoveCommand;
import mcsushi.dynamicshop.sushidynamicshop.config.CategoryConfig;
import mcsushi.dynamicshop.sushidynamicshop.util.PremiumChecker;
import mcsushi.dynamicshop.sushidynamicshop.editor.CategoryEditorInputManager;
import mcsushi.dynamicshop.sushidynamicshop.editor.CategoryEditorListener;
import mcsushi.dynamicshop.sushidynamicshop.editor.ShopEditorListener;
import mcsushi.dynamicshop.sushidynamicshop.editor.ShopEditorInputManager;
import mcsushi.dynamicshop.sushidynamicshop.editor.ShopEditorAddItemListener;
import mcsushi.dynamicshop.sushidynamicshop.gui.ShopListener;
import mcsushi.dynamicshop.sushidynamicshop.hook.ItemAdderShopListener;
import mcsushi.dynamicshop.sushidynamicshop.hook.MMOItemHook;
import mcsushi.dynamicshop.sushidynamicshop.shop.ShopConfig;
import mcsushi.dynamicshop.sushidynamicshop.util.PointsUtil;
import mcsushi.dynamicshop.sushidynamicshop.util.TranslationUtil;
import mcsushi.dynamicshop.sushidynamicshop.util.VaultUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;
import mcsushi.dynamicshop.sushidynamicshop.debug.DragEventDebugger;

public final class Sushidynamicshop extends JavaPlugin {

    private static Sushidynamicshop instance;
    private static Economy economy;

    @Override
    public void onEnable() {
        instance = this;
        Logger logger = getLogger();
        new mcsushi.dynamicshop.sushidynamicshop.init.Initializer(logger).init();
        saveDefaultConfig();
        reloadConfig();

        int pluginId = 25798;
        Metrics metrics = new Metrics(this, pluginId);
        TranslationUtil.load();

        ShopConfig.ensureDefaultShop(this);
        ShopConfig.loadAll(this);
        if (ShopConfig.hasShop("default")) {
            mcsushi.dynamicshop.sushidynamicshop.util.ShopValidator.validateShopFile("default");
        }
        CategoryConfig.setup(this);
        PointsUtil.setupPlayerPoints();

        PluginCommand cmd = getCommand("sushishop");
        if (cmd != null) {
            cmd.setExecutor(new SushishopCommand());
        } else {
            getLogger().warning("Failed to register /sushishop command.");
        }

        // ✅ ลงทะเบียน listener สำหรับ GUI
        getServer().getPluginManager().registerEvents(new ShopListener(), this);
        getServer().getPluginManager().registerEvents(new DragEventDebugger(), this);
        getServer().getPluginManager().registerEvents(new CategoryEditorListener(), this);
        getServer().getPluginManager().registerEvents(new CategoryEditorInputManager(), this);
        getServer().getPluginManager().registerEvents(new ShopEditorListener(), this);
        getServer().getPluginManager().registerEvents(new ShopEditorInputManager(), this);
        getServer().getPluginManager().registerEvents(new ShopEditorAddItemListener(), this);
        getServer().getPluginManager().registerEvents(new ItemAdderShopListener(), this);
        getServer().getPluginManager().registerEvents(new CurrencyRemoveCommand(this), this);

        if (setupVault()) {
            VaultUtil.setupEconomy(economy); // ✅ เพิ่มการเชื่อม VaultUtil
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Sushidynamicshop] Vault detected and hooked.");
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Sushidynamicshop] Vault not detected or failed to hook. Plugin disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (Bukkit.getPluginManager().isPluginEnabled("MMOItems")) {
            if (MMOItemHook.setup()) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Sushidynamicshop] MMOitem detected and hooked.");
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Sushidynamicshop] MMOitem detected but can't hook.");
            }
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Sushidynamicshop] MMOitem not detected.");
        }
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (mcsushi.dynamicshop.sushidynamicshop.hook.NexoHook.isHooked()) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Sushidynamicshop] Nexo detected. Registering NexoShoplistener...");
                Bukkit.getPluginManager().registerEvents(new mcsushi.dynamicshop.sushidynamicshop.hook.NexoShoplistener(), this);
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Sushidynamicshop] Nexo not detected. Skipping NexoShoplistener.");
            }
        }, 1L);
    }

    @Override
    public void onDisable() {
        Logger logger = getLogger();

        if (PremiumChecker.isPremium()) {
            try {
                Class<?> managerClass = Class.forName("mcsushi.dynamicshop.sushidynamicshop.premium.database.DatabaseManager");
                managerClass.getMethod("shutdown").invoke(null);
                logger.info("[Sushidynamicshop] Database connection closed successfully.");
            } catch (Exception e) {
                logger.warning("[Sushidynamicshop] Failed to close Database connection: " + e.getMessage());
            }
        }
    }

    private boolean setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public static Sushidynamicshop getInstance() {
        return instance;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static Logger getLoggerInstance() {
        return getInstance().getLogger();
    }

}