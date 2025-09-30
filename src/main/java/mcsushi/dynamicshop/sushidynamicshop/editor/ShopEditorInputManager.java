package mcsushi.dynamicshop.sushidynamicshop.editor;

import mcsushi.dynamicshop.sushidynamicshop.shop.ShopConfig;
import mcsushi.dynamicshop.sushidynamicshop.gui.ShopGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Map;

public class ShopEditorInputManager implements Listener {

    private static final Map<Player, ShopSession> inputMap = new HashMap<>();

    public static void startInput(Player player, String shopId, String itemKey, ShopField field) {
        inputMap.put(player, new ShopSession(shopId, itemKey, field));
        player.sendMessage(ChatColor.AQUA + "Enter value for " + field.name().toLowerCase() + ":");
        player.closeInventory();
    }

    public static void cancelInput(Player player) {
        inputMap.remove(player);
    }

    public static void startConfirmCreate(Player player, String shopId, int slot) {
        inputMap.put(player, new ShopSession(shopId, String.valueOf(slot), null));

        player.sendMessage(ChatColor.YELLOW + "Type 'yes' to confirm creating a new item at slot " + slot + ", or 'no' to cancel.");
        player.closeInventory();

        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("Sushidynamicshop"), () -> {
            if (inputMap.containsKey(player)) {
                inputMap.remove(player);
                player.sendMessage(ChatColor.RED + "⏱ Timed out.");
                ShopGUI.open(player, shopId);
            }
        }, 15 * 20L);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!inputMap.containsKey(player)) return;

        ShopSession session = inputMap.get(player);
        if (session.field == null) {
            // confirm create case
            event.setCancelled(true);
            inputMap.remove(player);
            String input = event.getMessage().trim().toLowerCase();

            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("Sushidynamicshop"), () -> {
                if (input.equals("yes")) {
                    ConfigurationSection section = ShopConfig.getShopConfig(session.shopId).createSection(session.itemKey);
                    section.set("source", "STONE");
                    section.set("slot", Integer.parseInt(session.itemKey));
                    section.set("base_price", 10.0);
                    section.set("current_price", 10.0);
                    section.set("buy_multiplier", 1.0);
                    section.set("sell_multiplier", 0.7);
                    section.set("min_price", 1.0);
                    section.set("max_price", 100.0);
                    section.set("price_change_rate", 0.5);
                    section.set("supply", 0);
                    section.set("demand", 0);
                    section.set("buy_enabled", true);
                    section.set("sell_enabled", true);

                    ShopGUI.open(player, session.shopId);
                    player.sendMessage(ChatColor.GREEN + "✅ Item created.");
                } else if (input.equals("no")) {
                    player.sendMessage(ChatColor.RED + "❌ Cancelled.");
                    ShopGUI.open(player, session.shopId);
                } else {
                    player.sendMessage(ChatColor.RED + "❌ Invalid input. Please type 'yes' or 'no'.");
                    ShopGUI.open(player, session.shopId);
                }
            });

        } else {
            // normal edit input case
            event.setCancelled(true);
            inputMap.remove(player);
            String input = event.getMessage();

            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("Sushidynamicshop"), () -> {
                ConfigurationSection section = ShopConfig.getShopConfig(session.shopId).getConfigurationSection(session.itemKey);
                if (section == null) {
                    player.sendMessage(ChatColor.RED + "Item not found.");
                    return;
                }

                try {
                    switch (session.field) {
                        case SLOT -> section.set("slot", Integer.parseInt(input));
                        case BASE_PRICE -> section.set("base_price", Double.parseDouble(input));
                        case BUY_MULTIPLIER -> section.set("buy_multiplier", Double.parseDouble(input));
                        case SELL_MULTIPLIER -> section.set("sell_multiplier", Double.parseDouble(input));
                        case MIN_PRICE -> section.set("min_price", Double.parseDouble(input));
                        case MAX_PRICE -> section.set("max_price", Double.parseDouble(input));
                        case SOURCE -> section.set("source", input);
                        case SUPPLY -> section.set("supply", Double.parseDouble(input));
                        case DEMAND -> section.set("demand", Double.parseDouble(input));
                        case PRICE_RANGE -> {
                            String[] parts = input.split("-");
                            if (parts.length != 2) {
                                player.sendMessage(ChatColor.RED + "Please enter in format: min-max");
                                return;
                            }
                            double min = Double.parseDouble(parts[0].trim());
                            double max = Double.parseDouble(parts[1].trim());
                            section.set("min_price", min);
                            section.set("max_price", max);
                        }
                    }

                    player.sendMessage(ChatColor.GREEN + "✅ Updated " + session.field.name().toLowerCase() + " successfully.");
                    ShopGUI.open(player, session.shopId);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "❌ Invalid input.");
                }
            });
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        inputMap.remove(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        inputMap.remove(event.getPlayer());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        inputMap.remove(event.getPlayer());
    }

    private record ShopSession(String shopId, String itemKey, ShopField field) {}
}