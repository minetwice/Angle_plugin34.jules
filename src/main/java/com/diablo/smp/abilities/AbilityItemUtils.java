package com.diablo.smp.abilities;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AbilityItemUtils {

    public static ItemStack createAbilityBook(String abilityName) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§l" + abilityName + " §b[Ability Book]");
            List<String> lore = new ArrayList<>();
            lore.add("§7This book contains ancient power.");
            lore.add("§7Right-click to absorb.");
            lore.add("");
            lore.add("§c§lCannot be dropped or stored!");
            meta.setLore(lore);
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            book.setItemMeta(meta);
        }
        return book;
    }

    public static boolean isAbilityBook(ItemStack item) {
        if (item == null || item.getType() != Material.ENCHANTED_BOOK) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getDisplayName().contains("[Ability Book]");
    }
}
