package com.diablosmp.plugin.utils;

import com.diablosmp.plugin.DiabloPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PdcUtils {
    public static void markDiabloBook(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(new NamespacedKey(DiabloPlugin.getInstance(), "diablo_book"), PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
    }

    public static boolean isDiabloBook(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(DiabloPlugin.getInstance(), "diablo_book"), PersistentDataType.BYTE);
    }
}
