package mcsushi.dynamicshop.sushidynamicshop.command.currency;

import mcsushi.dynamicshop.sushidynamicshop.command.SubCommand;
import mcsushi.dynamicshop.sushidynamicshop.init.PremiumInitializer;
import mcsushi.dynamicshop.sushidynamicshop.util.CurrencyRegistry;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CurrencyInfoCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        CurrencyRegistry registry = PremiumInitializer.getCurrencyRegistry();

        if (registry == null) {
            sender.sendMessage("§cThis feature is only available in the Premium version.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§eUsage: /ds currency info <currency> [player]");
            return true;
        }

        String currencyId = args[1].toLowerCase();
        String pronoun = registry.getPronoun(currencyId);

        if (!registry.currencyExists(currencyId)) {
            sender.sendMessage("§cCurrency ID not found: " + currencyId);
            return true;
        }

        if (args.length == 3) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
            UUID targetUUID = target.getUniqueId();
            String balanceMessage = registry.getPlayerBalance(targetUUID, currencyId, pronoun);
            sender.sendMessage(balanceMessage);
            return true;
        }

        // แสดง Top 10 Players
        sender.sendMessage("§6Currency ID: " + currencyId + " §7| Pronoun: §6" + pronoun);
        List<String> topPlayers = registry.getTop10Players(currencyId, pronoun);

        if (topPlayers.isEmpty()) {
            sender.sendMessage("§7No players found for this currency.");
        } else {
            for (String line : topPlayers) {
                sender.sendMessage(line);
            }
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        CurrencyRegistry registry = PremiumInitializer.getCurrencyRegistry();

        if (args.length == 2) {
            return registry.getAllCurrencies();
        } else if (args.length == 3) {
            return null; // ใช้ระบบ Player Name AutoComplete ของ Minecraft
        }

        return Arrays.asList();
    }
}
