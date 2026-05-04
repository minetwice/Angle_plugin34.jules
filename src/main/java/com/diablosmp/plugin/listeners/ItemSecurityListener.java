package com.diablosmp.plugin.listeners;

import com.diablosmp.plugin.managers.TrustManager;
import com.diablosmp.plugin.utils.PdcUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import java.util.HashMap;
import java.util.UUID;

public class ItemSecurityListener implements Listener {
    private final TrustManager trustManager;
    private final HashMap<UUID, Long> warnCooldown = new HashMap<>();

    public ItemSecurityListener(TrustManager trustManager) { this.trustManager = trustManager; }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (PdcUtils.isDiabloBook(e.getItemDrop().getItemStack())) {
            Player p = e.getPlayer();
            // Allow drop if someone trusted them recently (logic varies, assuming trade prep)
            e.setCancelled(true);
            
            long lastWarn = warnCooldown.getOrDefault(p.getUniqueId(), 0L);
            if (System.currentTimeMillis() - lastWarn > 60000) { // 1 min cooldown
                p.getWorld().spawnParticle(org.bukkit.Particle.CAMPFIRE_COSY_SMOKE, p.getLocation(), 20);
                p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1f, 0.5f);
                p.sendMessage("§4You cannot abandon the Diablo Power!");
                warnCooldown.put(p.getUniqueId(), System.currentTimeMillis());
            }
        }
    }

    @EventHandler
    public void onStore(InventoryClickEvent e) {
        if (e.getClickedInventory() != null && e.getClickedInventory().getType() != InventoryType.PLAYER) {
            if (e.getCurrentItem() != null && PdcUtils.isDiabloBook(e.getCurrentItem())) {
                e.setCancelled(true);
                e.getWhoClicked().sendMessage("§cDiablo books cannot be stored in chests!");
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();
        // If killer trusted the victim, book drops normally (handled by game).
        // To enforce removing it otherwise:
        if (killer == null || !trustManager.hasTrust(killer.getUniqueId(), victim.getUniqueId())) {
            e.getDrops().removeIf(PdcUtils::isDiabloBook); // Remove from drops if not trusted kill
        }
    }
}
