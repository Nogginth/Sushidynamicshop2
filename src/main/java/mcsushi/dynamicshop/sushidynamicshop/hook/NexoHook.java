package mcsushi.dynamicshop.sushidynamicshop.hook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class NexoHook {

    private static final String PLUGIN_NAME = "Nexo";

    /**
     * ตรวจสอบว่า plugin Nexo ติดตั้งและเปิดใช้งานอยู่หรือไม่
     */
    public static boolean isHooked() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
        return plugin != null && plugin.isEnabled();
    }
}
