package com.diablosmp.plugin.listeners;

import com.diablosmp.plugin.utils.ParticleUtils;
import com.diablosmp.plugin.utils.PdcUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AbsorptionGuiListener implements Listener {
    
    private final String GUI_NAME = "§5§lAbsorb Diablo Power";

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (e.getAction().isRightClick() && e.getItem() != null && PdcUtils.isDiabloBook(e.getItem())) {
            Inventory gui = Bukkit.createInventory(null, 27, Component.text(GUI_NAME));
            // Fill with glass, leave slot 13 open
            ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            for (int i = 0; i < 27; i++) { if (i != 13) gui.setItem(i, glass); }
            e.getPlayer().openInventory(gui);
        }
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(GUI_NAME)) {
            e.setCancelled(true);
            if (e.getSlot() == 13 && e.getCursor() != null && PdcUtils.isDiabloBook(e.getCursor())) {
                Player p = (Player) e.getWhoClicked();
                e.getCursor().setAmount(0); // Consume item
                p.closeInventory();
                ParticleUtils.playAbsorptionAnimation(p, Color.PURPLE);
                p.sendMessage("§a§lYou have absorbed the Diablo Power!");
            }
        }
    }
}
