package mcsushi.dynamicshop.sushidynamicshop.command;

import mcsushi.dynamicshop.sushidynamicshop.Sushidynamicshop;
import mcsushi.dynamicshop.sushidynamicshop.util.TranslationUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import mcsushi.dynamicshop.sushidynamicshop.shop.ShopConfig;
import mcsushi.dynamicshop.sushidynamicshop.config.CategoryConfig;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ReloadCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sushishop.reload") && !sender.isOp()) {
            sender.sendMessage(TranslationUtil.get("nopermission_reload"));
            return true;
        }

        long start = System.currentTimeMillis();
        int count = 0;

        File dataFolder = Sushidynamicshop.getInstance().getDataFolder();
        if (dataFolder.exists()) {
            TranslationUtil.load(); // ✅ โหลด translation.yml ใหม่
            CategoryConfig.reload(); // ✅ โหลด category.yml ใหม่
            ShopConfig.clearCache(); // ✅ เคลียร์ shop cache
            ShopConfig.loadAll(Sushidynamicshop.getInstance()); // ✅ โหลด shop ใหม่
            count = reloadAllYamlFiles(dataFolder);
        }

        long duration = System.currentTimeMillis() - start;
        sender.sendMessage(TranslationUtil.get("reload_success", Map.of("count", String.valueOf(count), "time", String.valueOf(duration))));
        return true;
    }

    private int reloadAllYamlFiles(File folder) {
        int count = 0;
        File[] files = folder.listFiles();
        if (files == null) return 0;

        for (File file : files) {
            if (file.isDirectory()) {
                count += reloadAllYamlFiles(file); // recursive
            } else if (file.getName().endsWith(".yml")) {
                YamlConfiguration.loadConfiguration(file);
                count++;
            }
        }
        return count;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}