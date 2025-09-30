package mcsushi.dynamicshop.sushidynamicshop.command.currency;

import mcsushi.dynamicshop.sushidynamicshop.command.SubCommand;
import mcsushi.dynamicshop.sushidynamicshop.init.PremiumInitializer;
import mcsushi.dynamicshop.sushidynamicshop.util.CurrencyRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CurrencyRemoveCommand implements SubCommand, Listener {

    private final Map<UUID, String> confirmationMap = new HashMap<>();
    private final Plugin plugin;

    public CurrencyRemoveCommand(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        CurrencyRegistry registry = PremiumInitializer.getCurrencyRegistry();

        if (registry == null) {
            sender.sendMessage("§cThis feature is only available in the Premium version.");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can confirm currency removal.");
            return true;
        }

        UUID playerUUID = player.getUniqueId();

        // หากผู้เล่นมี pending confirmation อยู่
        if (confirmationMap.containsKey(playerUUID)) {
            sender.sendMessage("§cYou already have a pending confirmation. Type Yes or No in the chat.");
            return true;
        }

        // หากไม่มี args หรือ args.length น้อยกว่า 2
        if (args.length < 2) {
            sender.sendMessage("§eUsage: /ds currency remove <currency>");
            return true;
        }

        String currencyId = args[1].toLowerCase();

        if (!registry.currencyExists(currencyId)) {
            sender.sendMessage("§cCurrency not found: " + currencyId);
            return true;
        }

        // Store the confirmation
        confirmationMap.put(playerUUID, currencyId);
        sender.sendMessage("§eAre you sure you want to delete §6" + currencyId + "§e? Type §aYes §eor §cNo §ein the chat to confirm.");

        // 15-second timeout
        new BukkitRunnable() {
            @Override
            public void run() {
                if (confirmationMap.containsKey(playerUUID)) {
                    confirmationMap.remove(playerUUID);
                    player.sendMessage("§cCurrency removal cancelled due to timeout.");
                }
            }
        }.runTaskLater(plugin, 15 * 20);

        return true;
    }

    /**
     * Listener สำหรับรับการยืนยันในแชท
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        String message = event.getMessage().trim().toLowerCase();

        if (!confirmationMap.containsKey(playerUUID)) return;

        event.setCancelled(true); // ป้องกันไม่ให้แสดงข้อความแชทต่อผู้เล่นอื่น

        if (message.equals("yes") || message.equals("no")) {
            handleConfirmation(player, message);
        }
    }

    /**
     * Confirmation Handler
     */
    public void handleConfirmation(Player player, String input) {
        UUID playerUUID = player.getUniqueId();
        String currencyId = confirmationMap.remove(playerUUID);

        if (currencyId == null) return;

        CurrencyRegistry registry = PremiumInitializer.getCurrencyRegistry();

        if (input.equals("yes")) {
            boolean success = registry.removeCurrency(currencyId);

            if (success) {
                player.sendMessage("§aCurrency " + currencyId + " has been removed.");
            } else {
                player.sendMessage("§cFailed to remove currency.");
            }
        } else {
            player.sendMessage("§eCurrency removal cancelled.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        CurrencyRegistry registry = PremiumInitializer.getCurrencyRegistry();

        if (args.length == 2) {
            return registry.getAllCurrencies();
        }
        return Collections.emptyList();
    }
}
