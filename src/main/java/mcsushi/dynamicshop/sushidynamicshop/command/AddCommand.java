
package mcsushi.dynamicshop.sushidynamicshop.command;

import mcsushi.dynamicshop.sushidynamicshop.editor.CategoryEditorGUI;
import mcsushi.dynamicshop.sushidynamicshop.gui.ShopGUI;
import mcsushi.dynamicshop.sushidynamicshop.config.CategoryConfig;
import mcsushi.dynamicshop.sushidynamicshop.shop.ShopConfig;
import mcsushi.dynamicshop.sushidynamicshop.util.GuiSlotHolder;
import mcsushi.dynamicshop.sushidynamicshop.editor.ShopEditorHolder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class AddCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.isOp()) {
            player.sendMessage("You must be OP to use this command.");
            return true;
        }

        if (args.length != 3) {
            player.sendMessage("Usage: /sushishop add <category|shop> <id>");
            return true;
        }

        String type = args[1].toLowerCase();
        String id = args[2];

        if (type.equals("category")) {
            if (CategoryConfig.hasCategory(id)) {
                player.sendMessage("This category ID already exists.");
                return true;
            }

            int slot = GuiSlotHolder.findAvailableSlot(new mcsushi.dynamicshop.sushidynamicshop.gui.CategoryMenuHolder());
            CategoryConfig.createCategory(id);
            CategoryConfig.get().set("categories." + id + ".slot", slot);
            CategoryConfig.save();
            CategoryEditorGUI.open(player, id);
            player.sendMessage("Category '" + id + "' created at slot " + slot + " and opened for editing.");
            return true;
        }

        if (type.equals("shop")) {
            if (ShopConfig.hasShop(id)) {
                player.sendMessage("This shop ID already exists.");
                return true;
            }

            YamlConfiguration config = new YamlConfiguration();
            config.set("inventory.name", "&fNew Shop");
            config.set("inventory.slot", 54);
            config.set("inventory.shopcurrency", "VAULT"); // ✅ เพิ่มบรรทัดนี้
            ShopConfig.createShop(id, config); // สร้างไฟล์และลงทะเบียน shop

            ConfigurationSection vanillaSection = config.createSection("VANILLA_STONE");
            vanillaSection.set("source", "STONE");
            vanillaSection.set("slot", 10);
            vanillaSection.set("base_price", 10.0);
            vanillaSection.set("current_price", 10.0);
            vanillaSection.set("buy_multiplier", 1.0);
            vanillaSection.set("sell_multiplier", 0.7);
            vanillaSection.set("min_price", 1.0);
            vanillaSection.set("max_price", 100.0);
            vanillaSection.set("price_change_rate", 0.5);
            vanillaSection.set("supply", 0);
            vanillaSection.set("demand", 0);
            vanillaSection.set("buy_enabled", true);
            vanillaSection.set("sell_enabled", true);
            ShopConfig.addItemToShop(id, "VANILLA_STONE", vanillaSection);

            ConfigurationSection mmoSection = config.createSection("MMOITEM_CUTLASS");
            mmoSection.set("source", "mmoitem:sword:cutlass");
            mmoSection.set("slot", 11);
            mmoSection.set("base_price", 50.0);
            mmoSection.set("current_price", 50.0);
            mmoSection.set("buy_multiplier", 1.0);
            mmoSection.set("sell_multiplier", 0.7);
            mmoSection.set("min_price", 10.0);
            mmoSection.set("max_price", 200.0);
            mmoSection.set("price_change_rate", 1.0);
            mmoSection.set("supply", 0);
            mmoSection.set("demand", 0);
            mmoSection.set("buy_enabled", true);
            mmoSection.set("sell_enabled", true);
            ShopConfig.addItemToShop(id, "MMOITEM_CUTLASS", mmoSection);

            player.sendMessage("Shop '" + id + "' created successfully.");
            ShopGUI.open(player, id);
            return true;
        }

        player.sendMessage("Usage: /sushishop add <category|shop> <id>");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2) {
            return List.of("category", "shop");
        }
        if (args.length == 3 && (args[1].equalsIgnoreCase("category") || args[1].equalsIgnoreCase("shop"))) {
            return List.of("<id>");
        }
        return Collections.emptyList();
    }
}
