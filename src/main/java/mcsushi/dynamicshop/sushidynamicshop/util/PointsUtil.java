package mcsushi.dynamicshop.sushidynamicshop.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;


public class PointsUtil {

    private static PlayerPointsAPI playerPointsAPI;

    public static void setupPlayerPoints() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            playerPointsAPI = PlayerPoints.getInstance().getAPI();
            Bukkit.getLogger().info("[Sushidynamicshop] PlayerPoints successfully hooked.");
        } else {
            Bukkit.getLogger().warning("[Sushidynamicshop] PlayerPoints not found.");
        }
    }

    private static boolean isHooked() {
        return playerPointsAPI != null;
    }

    public static int getPoints(Player player) {
        if (!isHooked()) return 0;
        return playerPointsAPI.look(player.getUniqueId());
    }

    public static boolean hasPoints(Player player, int amount) {
        if (!isHooked()) return false;
        return getPoints(player) >= amount;
    }

    public static boolean withdrawPoints(Player player, int amount) {
        if (!isHooked() || amount <= 0) return false;
        return playerPointsAPI.take(player.getUniqueId(), amount);
    }

    public static void depositPoints(Player player, int amount) {
        if (!isHooked() || amount <= 0) return;
        playerPointsAPI.give(player.getUniqueId(), amount);
    }

    public static boolean canWithdrawPoints(Player player, int amount) {
        return hasPoints(player, amount);
    }

    public static String getPointsFormatted(Player player) {
        if (!isHooked()) return "0 Points";
        return playerPointsAPI.lookFormatted(player.getUniqueId());
    }
}
