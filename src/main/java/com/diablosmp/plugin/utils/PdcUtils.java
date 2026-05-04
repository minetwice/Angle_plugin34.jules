package com.diablosmp.plugin.utils;

import com.diablosmp.plugin.DiabloPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PdcUtils {
    private static final NamespacedKey BOOK_KEY = new NamespacedKey(DiabloPlugin.getInstance(), "diablo_book");
    private static final NamespacedKey ABILITY_KEY = new NamespacedKey(DiabloPlugin.getInstance(), "has_diablo_ability");

    public static ItemStack createDiabloBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        meta.setDisplayName("§5§lDiablo Ability Book");
        meta.getPersistentDataContainer().set(BOOK_KEY, PersistentDataType.BYTE, (byte) 1);
        book.setItemMeta(meta);
        return book;
    }

    public static void markDiabloBook(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(BOOK_KEY, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
    }

    public static boolean isDiabloBook(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(BOOK_KEY, PersistentDataType.BYTE);
    }

    // Absorption check karne ke methods
    public static void setHasAbility(Player p, boolean hasAbility) {
        if (hasAbility) {
            p.getPersistentDataContainer().set(ABILITY_KEY, PersistentDataType.BYTE, (byte) 1);
        } else {
            p.getPersistentDataContainer().remove(ABILITY_KEY);
        }
    }

    public static boolean hasAbility(Player p) {
        return p.getPersistentDataContainer().has(ABILITY_KEY, PersistentDataType.BYTE);
    }
}
