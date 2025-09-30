package mcsushi.dynamicshop.sushidynamicshop.command;

import mcsushi.dynamicshop.sushidynamicshop.debug.Custommodeldatachecker;
import mcsushi.dynamicshop.sushidynamicshop.gui.CategoryMenuGUI;
import mcsushi.dynamicshop.sushidynamicshop.util.TranslationUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class SushishopCommand implements CommandExecutor, TabCompleter {

    private final Map<String, SubCommand> subcommands = new HashMap<>();

    public SushishopCommand() {
        subcommands.put("reload", new ReloadCommand());
        subcommands.put("add", new AddCommand());
        subcommands.put("checkheld", new Custommodeldatachecker());
        subcommands.put("currency", new CurrencyCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sushishop.open") && !sender.isOp()) {
            sender.sendMessage(TranslationUtil.get("nopermission"));
            return true;
        }

        if (args.length == 0) {
            if (sender instanceof Player player) {
                CategoryMenuGUI.open(player);
            } else {
                sender.sendMessage(TranslationUtil.get("console_use"));
            }
            return true;
        }

        String sub = args[0].toLowerCase();
        SubCommand subCommand = subcommands.get(sub);
        if (subCommand != null) {
            return subCommand.execute(sender, command, label, args);
        }

        sender.sendMessage(TranslationUtil.get("unknowncommand"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("sushishop.open") && !sender.isOp()) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> completions = new ArrayList<>(subcommands.keySet());
            completions.removeIf(s -> !s.startsWith(args[0].toLowerCase()));
            return completions;
        } else if (args.length > 1) {
            SubCommand sub = subcommands.get(args[0].toLowerCase());
            if (sub != null) {
                return sub.tabComplete(sender, command, alias, args);
            }
        }
        return Collections.emptyList();
    }
}