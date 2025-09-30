package mcsushi.dynamicshop.sushidynamicshop.util;

import mcsushi.dynamicshop.sushidynamicshop.hook.NexoHook;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;

public class Nexoutil {

    /**
     * ตรวจสอบว่า item นี้เป็นของ Nexo หรือไม่
     */
    public static boolean isNexoItem(ItemStack item) {
        if (!NexoHook.isHooked() || item == null) return false;
        try {
            return NexoItems.idFromItem(item) != null;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * ดึง Nexo ID ของไอเทมนี้ (หรือ null ถ้าไม่ใช่ Nexo)
     */
    @Nullable
    public static String getNexoId(ItemStack item) {
        if (!NexoHook.isHooked() || item == null) return null;
        try {
            return NexoItems.idFromItem(item);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * สร้างไอเทม Nexo จาก ID และจำนวน หากไม่สามารถสร้างได้จะคืน BARRIER
     */
    public static ItemStack createItem(String id, int amount) {
        if (!NexoHook.isHooked() || id == null || id.isEmpty()) {
            return buildBarrier("Nexo not available or ID invalid");
        }

        try {
            ItemBuilder builder = NexoItems.itemFromId(id);
            if (builder == null) {
                return buildBarrier("Unknown Nexo ID: " + id);
            }

            ItemStack item = builder.build();
            item.setAmount(amount);
            return item;
        } catch (Throwable t) {
            return buildBarrier("Exception creating Nexo item: " + id);
        }
    }

    /**
     * คืน ItemStack BARRIER พร้อม display name แสดงข้อผิดพลาด
     */
    private static ItemStack buildBarrier(String reason) {
        ItemStack barrier = new ItemStack(Material.BARRIER);
        // คุณสามารถเสริม display name หรือ lore ได้ที่นี่หากต้องการแสดงใน GUI
        return barrier;
    }
}
