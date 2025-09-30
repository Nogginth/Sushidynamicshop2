package mcsushi.dynamicshop.sushidynamicshop.command.currency;

import mcsushi.dynamicshop.sushidynamicshop.command.SubCommand;
import mcsushi.dynamicshop.sushidynamicshop.init.PremiumInitializer;
import mcsushi.dynamicshop.sushidynamicshop.util.CurrencyRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class CurrencyEditCommand implements SubCommand {

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        CurrencyRegistry registry = PremiumInitializer.getCurrencyRegistry();

        if (registry == null) {
            sender.sendMessage("§cThis feature is only available in the Premium version.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§eUsage: /ds currency edit <currency> <new_pronoun>");
            return true;
        }

        String currencyId = args[1].toLowerCase();
        String newPronoun = args[2];

        if (!registry.currencyExists(currencyId)) {
            sender.sendMessage("§cCurrency not found: " + currencyId);
            return true;
        }

        boolean success = registry.updateCurrency(currencyId, newPronoun);

        if (success) {
            sender.sendMessage("§aCurrency " + currencyId + " has been updated to " + newPronoun);
        } else {
            sender.sendMessage("§cFailed to update currency.");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        CurrencyRegistry registry = PremiumInitializer.getCurrencyRegistry();

        if (args.length == 2) {
            return registry.getAllCurrencies();
        } else if (args.length == 3) {
            return Arrays.asList("<new_pronoun>");
        }

        return Arrays.asList();
    }
}
