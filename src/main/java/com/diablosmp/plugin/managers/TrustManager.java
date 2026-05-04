package com.diablosmp.plugin.managers;

import com.diablosmp.plugin.DiabloPlugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.UUID;

public class TrustManager {
    private final HashMap<UUID, HashMap<UUID, Long>> contracts = new HashMap<>();

    public void createContract(Player owner, Player trusted) {
        long duration = 5 * 60 * 1000L; // 5 Minutes
        contracts.computeIfAbsent(owner.getUniqueId(), k -> new HashMap<>())
                 .put(trusted.getUniqueId(), System.currentTimeMillis() + duration);
        
        owner.sendMessage("§aYou trusted " + trusted.getName() + " for 5 minutes.");
        trusted.sendMessage("§a" + owner.getName() + " trusted you. You can trade Diablo items.");

        new BukkitRunnable() {
            @Override
            public void run() {
                if (hasTrust(owner.getUniqueId(), trusted.getUniqueId())) {
                    contracts.get(owner.getUniqueId()).remove(trusted.getUniqueId());
                    owner.sendMessage("§cTrust contract with " + trusted.getName() + " expired.");
                }
            }
        }.runTaskLater(DiabloPlugin.getInstance(), 5 * 60 * 20L); // 5 mins in ticks
    }

    public boolean hasTrust(UUID owner, UUID target) {
        if (!contracts.containsKey(owner)) return false;
        Long expiry = contracts.get(owner).get(target);
        return expiry != null && expiry > System.currentTimeMillis();
    }
}
