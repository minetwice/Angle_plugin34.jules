package com.diablosmp.plugin.listeners;

import com.diablosmp.plugin.DiabloPlugin;
import com.diablosmp.plugin.utils.ParticleUtils;
import com.diablosmp.plugin.utils.PdcUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Arrays;

public class AbsorptionGuiListener implements Listener {
    
    private final String ABSORB_GUI = "§5§lAbsorb Diablo Power";
    private final String MANAGE_GUI = "§5§lDiablo Power Menu";

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getAction().isRightClick() && e.getItem() != null && PdcUtils.isDiabloBook(e.getItem())) {
            e.setCancelled(true);
            openAbsorbGui(p);
        }
    }

    private void openAbsorbGui(Player p) {
        Inventory gui = Bukkit.createInventory(null, 27, Component.text(ABSORB_GUI));
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        
        for (int i = 0; i < 27; i++) { if (i != 13) gui.setItem(i, glass); }

        // Enchanting table slot
        ItemStack enchantTable = new ItemStack(Material.ENCHANTING_TABLE);
        ItemMeta tableMeta = enchantTable.getItemMeta();
        tableMeta.setDisplayName("§d§lAbsorb Power");
        tableMeta.setLore(Arrays.asList("§7Click with Diablo Book on your cursor", "§7to absorb this ancient power."));
        enchantTable.setItemMeta(tableMeta);
        gui.setItem(13, enchantTable);

        p.openInventory(gui);
    }

    public void openManageGui(Player p) {
        Inventory gui = Bukkit.createInventory(null, 27, Component.text(MANAGE_GUI));
        ItemStack glass = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        for (int i = 0; i < 27; i++) gui.setItem(i, glass);

        // Current Power Info
        ItemStack info = new ItemStack(Material.NETHER_STAR);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§e§lCurrent Power: Soul Weaver");
        infoMeta.setLore(Arrays.asList("§7Stage: 1", "§7Left-Click to take control of souls."));
        info.setItemMeta(infoMeta);
        gui.setItem(11, info);

        // Withdraw Button
        ItemStack withdraw = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta withMeta = withdraw.getItemMeta();
        withMeta.setDisplayName("§c§lWithdraw Power");
        withMeta.setLore(Arrays.asList("§7Click to extract the power", "§7back into a physical book."));
        withdraw.setItemMeta(withMeta);
        gui.setItem(15, withdraw);

        p.openInventory(gui);
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(ABSORB_GUI)) {
            
            // Allow picking up items from player's own inventory
            if (e.getClickedInventory() != null && e.getClickedInventory().equals(e.getWhoClicked().getInventory())) {
                return; 
            }
            e.setCancelled(true); // Protect GUI items

            if (e.getSlot() == 13) {
                Player p = (Player) e.getWhoClicked();
                ItemStack cursor = e.getCursor();

                if (cursor != null && PdcUtils.isDiabloBook(cursor)) {
                    // Start Absorption!
                    p.setItemOnCursor(null); // Remove book from cursor
                    
                    ItemStack bookVisual = PdcUtils.createDiabloBook();
                    e.getInventory().setItem(13, bookVisual); // Swap table with Book
                    p.playSound(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f);
                    
                    // Delay for animation effect
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            p.closeInventory();
                            ParticleUtils.playAbsorptionAnimation(p, Color.PURPLE);
                            PdcUtils.setHasAbility(p, true);
                            p.sendMessage("§a§lDiablo Power Absorbed Successfully!");
                            
                            // Open Manage GUI after particles finish (2 seconds)
                            new BukkitRunnable() {
                                @Override
                                public void run() { openManageGui(p); }
                            }.runTaskLater(DiabloPlugin.getInstance(), 40L);
                        }
                    }.runTaskLater(DiabloPlugin.getInstance(), 20L); // 1 sec delay
                }
            }
        } else if (e.getView().getTitle().equals(MANAGE_GUI)) {
            e.setCancelled(true); // Protect GUI items
            if (e.getSlot() == 15) { // Withdraw
                Player p = (Player) e.getWhoClicked();
                if (p.getInventory().firstEmpty() == -1) {
                    p.sendMessage("§cYour inventory is full! Cannot withdraw.");
                    p.closeInventory();
                    return;
                }
                PdcUtils.setHasAbility(p, false); // Remove ability
                p.getInventory().addItem(PdcUtils.createDiabloBook()); // Give book
                p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
                p.sendMessage("§cPower Withdrawn! Book returned to your inventory.");
                p.closeInventory();
            }
        }
    }
}
