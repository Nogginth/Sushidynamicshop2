
package mcsushi.dynamicshop.sushidynamicshop.util;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MMOItemFactory {

    public static ItemStack createItem(String source) {
        if (source == null || source.isEmpty()) {
            System.out.println("[Sushidynamicshop] MMOItem source is null or empty.");
            return new ItemStack(Material.BARRIER);
        }

        try {
            String[] split = source.split(":");
            if (split.length != 3) {
                System.out.println("[Sushidynamicshop] MMOItem source format invalid: " + source);
                return new ItemStack(Material.BARRIER);
            }

            String typeName = split[1].toUpperCase();
            String id = split[2].toUpperCase();

            Type type = MMOItems.plugin.getTypes().get(typeName);
            if (type == null) {
                System.out.println("[Sushidynamicshop] MMOItem Type not found: " + typeName);
                return new ItemStack(Material.BARRIER);
            }

            MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplate(type, id);
            if (template == null) {
                System.out.println("[Sushidynamicshop] MMOItem Template not found: " + typeName + ":" + id);
                return new ItemStack(Material.BARRIER);
            }

            MMOItem mmoItem = template.newBuilder().build();
            return mmoItem.newBuilder().build();
        } catch (Exception e) {
            System.out.println("[Sushidynamicshop] Exception while building MMOItem: " + e.getMessage());
            e.printStackTrace();
            return new ItemStack(Material.BARRIER);
        }
    }
}
