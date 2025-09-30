package mcsushi.dynamicshop.sushidynamicshop.command;

import mcsushi.dynamicshop.sushidynamicshop.Sushidynamicshop;
import mcsushi.dynamicshop.sushidynamicshop.command.currency.*;
import mcsushi.dynamicshop.sushidynamicshop.init.PremiumInitializer;
import mcsushi.dynamicshop.sushidynamicshop.util.CurrencyRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;

public class CurrencyCommand implements SubCommand {

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public CurrencyCommand() {
        subCommands.put("info", new CurrencyInfoCommand());
        subCommands.put("add", new CurrencyAddCommand());
        subCommands.put("remove", new CurrencyRemoveCommand(Sushidynamicshop.getInstance()));
        subCommands.put("edit", new CurrencyEditCommand());
        subCommands.put("player", new CurrencyPlayerCommand());
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        // ตรวจสอบ Premium ก่อน
        CurrencyRegistry registry = PremiumInitializer.getCurrencyRegistry();
        if (registry instanceof mcsushi.dynamicshop.sushidynamicshop.util.DefaultCurrencyRegistry) {
            sender.sendMessage("§cThis feature is available only in the Premium version.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§eUsage: /ds currency <subcommand>");
            return true;
        }

        String subCommandKey = args[1].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandKey);

        if (subCommand != null) {
            return subCommand.execute(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
        }

        sender.sendMessage("§cUnknown currency subcommand: " + subCommandKey);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2) {
            List<String> completions = new ArrayList<>(subCommands.keySet());
            completions.removeIf(s -> !s.startsWith(args[1].toLowerCase()));
            return completions;
        } else if (args.length > 2) {
            SubCommand subCommand = subCommands.get(args[1].toLowerCase());
            if (subCommand != null) {
                return subCommand.tabComplete(sender, command, alias, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return Collections.emptyList();
    }
}
