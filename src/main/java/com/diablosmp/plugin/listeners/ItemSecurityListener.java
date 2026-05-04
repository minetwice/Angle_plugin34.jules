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

    // 🚀 DROP AUR MOVE KARNE KI FULL AZADI (FREEDOM) DE DI GAYI HAI!
    // Maine yahan se PlayerDropItemEvent aur InventoryClickEvent hata diye hain.
    // Ab player book ko aaram se 'Q' daba kar drop kar sakta hai aur kisi bhi chest me daal sakta hai.

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();
        
        // PVP Death Logic (Pehle wala Trust System): 
        // Agar player kisi aise bande ke hatho marta hai jisko usne /trust nahi kiya tha, 
        // toh book normal drop me nahi giregi (safe rahegi ya delete ho jayegi).
        // Agar tum chahte ho ki marne par har kisi ko book drop ho jaye, toh tum is method ko bhi hata sakte ho.
        if (killer == null || !trustManager.hasTrust(killer.getUniqueId(), victim.getUniqueId())) {
            e.getDrops().removeIf(PdcUtils::isDiabloBook); // Remove from drops if not trusted kill
        }
    }
}
