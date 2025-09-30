package mcsushi.dynamicshop.sushidynamicshop.command.currency;

import mcsushi.dynamicshop.sushidynamicshop.command.SubCommand;
import mcsushi.dynamicshop.sushidynamicshop.init.PremiumInitializer;
import mcsushi.dynamicshop.sushidynamicshop.util.CurrencyRegistry;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CurrencyPlayerCommand implements SubCommand {

    private final List<String> actions = Arrays.asList("give", "remove", "set");

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        CurrencyRegistry registry = PremiumInitializer.getCurrencyRegistry();

        if (registry == null) {
            sender.sendMessage("§cThis feature is only available in the Premium version.");
            return true;
        }

        if (args.length < 5) {
            sender.sendMessage("§eUsage: /ds currency player <give/remove/set> <player> <currency> <amount>");
            return true;
        }

        String action = args[1].toLowerCase();
        String playerName = args[2];
        String currencyId = args[3].toLowerCase();
        String amountStr = args[4];

        if (!actions.contains(action)) {
            sender.sendMessage("§cInvalid action. Use give, remove, or set.");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        UUID playerUUID = target.getUniqueId();

        if (!registry.currencyExists(currencyId)) {
            sender.sendMessage("§cCurrency not found: " + currencyId);
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount. Must be a number.");
            return true;
        }

        boolean success = false;
        switch (action) {
            case "give" -> success = registry.adjustPlayerBalance(playerUUID, currencyId, amount, "give");
            case "remove" -> success = registry.adjustPlayerBalance(playerUUID, currencyId, amount, "remove");
            case "set" -> {
                org.bukkit.Bukkit.getLogger().info("[DEBUG] Setting balance for '" + currencyId + "' to " + amount);
                success = registry.adjustPlayerBalance(playerUUID, currencyId, amount, "set");
            }
        }

        if (success) {
            sender.sendMessage("§aSuccessfully " + action + " " + amount + " " + currencyId + " to " + playerName);
        } else {
            sender.sendMessage("§cFailed to " + action + " currency.");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        CurrencyRegistry registry = PremiumInitializer.getCurrencyRegistry();

        if (args.length == 2) {
            return actions;
        } else if (args.length == 3) {
            return null; // Player Name AutoComplete
        } else if (args.length == 4) {
            return registry.getAllCurrencies();
        } else if (args.length == 5) {
            return Arrays.asList("<amount>");
        }

        return Arrays.asList();
    }
}
