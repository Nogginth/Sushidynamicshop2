package mcsushi.dynamicshop.sushidynamicshop.command;

import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import java.util.List;

public interface SubCommand {
    boolean execute(CommandSender sender, Command command, String label, String[] args);
    List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args);
}
