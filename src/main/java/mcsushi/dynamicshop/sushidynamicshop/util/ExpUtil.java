package mcsushi.dynamicshop.sushidynamicshop.util;

import org.bukkit.entity.Player;

public class ExpUtil {

    // ตรวจสอบว่าผู้เล่นมีเลเวลเพียงพอหรือไม่
    public static boolean hasEnough(Player player, int requiredLevels) {
        return player.getLevel() >= requiredLevels;
    }

    // ลบจำนวนเลเวลจากผู้เล่นอย่างปลอดภัย
    public static boolean removeLevel(Player player, int levelsToRemove) {
        int current = player.getLevel();
        if (current < levelsToRemove) {
            return false;
        }
        player.setLevel(current - levelsToRemove);
        return true;
    }

    // เพิ่มเลเวลให้ผู้เล่น
    public static void addLevel(Player player, int levelsToAdd) {
        player.setLevel(player.getLevel() + levelsToAdd);
    }

    // ดึงเลเวลปัจจุบัน (สำหรับแสดงผล)
    public static int getLevel(Player player) {
        return player.getLevel();
    }
}
