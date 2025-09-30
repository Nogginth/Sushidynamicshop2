package mcsushi.dynamicshop.sushidynamicshop.util;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemAdderUtil {

    /**
     * สร้าง ItemStack จาก ItemAdder โดยใช้ source ในรูปแบบ namespace:id
     */
    public static ItemStack createItem(String source, int amount) {
        if (source == null || source.isEmpty()) {
            System.out.println("[Sushidynamicshop] ItemAdder source is null or empty.");
            return new ItemStack(Material.BARRIER);
        }

        try {
            if (!CustomStack.isInRegistry(source)) {
                System.out.println("[Sushidynamicshop] ItemAdder item not in registry: " + source);
                return new ItemStack(Material.BARRIER);
            }

            CustomStack stack = CustomStack.getInstance(source);
            if (stack == null) {
                System.out.println("[Sushidynamicshop] Failed to get CustomStack instance for: " + source);
                return new ItemStack(Material.BARRIER);
            }

            ItemStack item = stack.getItemStack();
            item.setAmount(amount);
            return item;

        } catch (Exception e) {
            System.out.println("[Sushidynamicshop] Exception while building ItemAdder item: " + e.getMessage());
            e.printStackTrace();
            return new ItemStack(Material.BARRIER);
        }
    }

    /**
     * ตรวจสอบว่า item นี้คือไอเทมจาก ItemAdder หรือไม่
     */
    public static boolean isItemAdderItem(ItemStack item) {
        return CustomStack.byItemStack(item) != null;
    }

    /**
     * ดึง namespaced id ของไอเทม (เช่น myitems:katana)
     */
    public static String getNamespacedId(ItemStack item) {
        CustomStack stack = CustomStack.byItemStack(item);
        return (stack != null) ? stack.getNamespacedID() : null;
    }
}
