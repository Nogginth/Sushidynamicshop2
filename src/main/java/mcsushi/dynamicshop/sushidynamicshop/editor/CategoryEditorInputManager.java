package mcsushi.dynamicshop.sushidynamicshop.editor;

import mcsushi.dynamicshop.sushidynamicshop.config.CategoryConfig;
import mcsushi.dynamicshop.sushidynamicshop.util.GuiSlotHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

public class CategoryEditorInputManager implements Listener {

    private static final Map<Player, CategorySession> inputMap = new HashMap<>();

    public static void startInput(Player player, String categoryId, CategoryField field) {
        inputMap.put(player, new CategorySession(categoryId, field));
        player.sendMessage(ChatColor.AQUA + "Enter value for " + field.name().toLowerCase() + ":");
        player.closeInventory();
    }

    public static void cancelInput(Player player) {
        inputMap.remove(player);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!inputMap.containsKey(player)) return;

        event.setCancelled(true);
        CategorySession session = inputMap.remove(player);
        String input = event.getMessage();

        Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("Sushidynamicshop"), () -> {
            ConfigurationSection section = CategoryConfig.get().getConfigurationSection("categories." + session.categoryId);
            if (section == null) {
                player.sendMessage(ChatColor.RED + "Category not found.");
                return;
            }

            Bukkit.getLogger().info("[DEBUG] Input received: " + input + " for field: " + session.field.name());

            switch (session.field) {
                case SHOPID -> section.set("shopid", input);
                case NAME -> section.set("name", input);
                case PERMISSION -> section.set("permission", input);
                case LORE -> {
                    String lore = input.contains("&") ? input : "&f" + input;
                    section.set("lore", Collections.singletonList(lore));
                }
                case SLOT -> {
                    try {
                        int slot = Integer.parseInt(input);
                        if (slot < 0 || slot >= 54) {
                            player.sendMessage(ChatColor.RED + "❌ Invalid slot. Please enter a number between 0-53.");
                            return;
                        }
                        if (GuiSlotHolder.getCategoryEditorReservedSlots().contains(slot)) {
                            player.sendMessage(ChatColor.RED + "❌ This slot is reserved by the GUI.");
                            return;
                        }
                        String conflict = CategoryConfig.getCategoryIdBySlot(slot);
                        if (conflict != null && !conflict.equals(session.categoryId)) {
                            player.sendMessage(ChatColor.RED + "❌ This slot is already used by category '" + conflict + "'.");
                            return;
                        }
                        section.set("slot", slot);
                        player.sendMessage(ChatColor.GREEN + "✅ Slot updated to " + slot);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "❌ Invalid number format.");
                        return;
                    }
                }
                default -> player.sendMessage(ChatColor.RED + "Field not supported yet.");
            }

            CategoryConfig.save();
            CategoryEditorGUI.open(player, session.categoryId);
        });
    }

    private record CategorySession(String categoryId, CategoryField field) {}
}