package mcsushi.dynamicshop.sushidynamicshop.debug;

import mcsushi.dynamicshop.sushidynamicshop.command.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;

public class Custommodeldatachecker implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You are not holding any item.");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasCustomModelData()) {
            int data = meta.getCustomModelData();
            player.sendMessage(ChatColor.GREEN + "CustomModelData: " + ChatColor.YELLOW + data);
        } else {
            player.sendMessage(ChatColor.RED + "This item does NOT have CustomModelData.");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}