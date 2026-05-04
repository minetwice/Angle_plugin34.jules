package com.diablosmp.plugin.listeners;

import com.diablosmp.plugin.managers.TrustManager;
import com.diablosmp.plugin.utils.PdcUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class ItemSecurityListener implements Listener {
    private final TrustManager trustManager;

    public ItemSecurityListener(TrustManager trustManager) { 
        this.trustManager = trustManager; 
    }

    // Chest restrictions permanently REMOVED! 
    
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();
        
        // PVP Trust System
        if (killer == null || !trustManager.hasTrust(killer.getUniqueId(), victim.getUniqueId())) {
            e.getDrops().removeIf(PdcUtils::isDiabloBook);
        }
    }
}
