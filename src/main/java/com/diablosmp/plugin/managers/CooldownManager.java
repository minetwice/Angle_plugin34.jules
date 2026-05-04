package com.diablosmp.plugin.managers;

import org.bukkit.entity.Player; // <--- YEH LINE MISSING THI
import java.util.HashMap;
import java.util.UUID;

public class CooldownManager {
    private final HashMap<String, HashMap<UUID, Long>> cooldowns = new HashMap<>();

    public void setCooldown(Player player, String ability, int seconds) {
        cooldowns.computeIfAbsent(ability, k -> new HashMap<>())
                 .put(player.getUniqueId(), System.currentTimeMillis() + (seconds * 1000L));
    }

    public boolean isOnCooldown(Player player, String ability) {
        if (!cooldowns.containsKey(ability)) return false;
        Long expiry = cooldowns.get(ability).get(player.getUniqueId());
        return expiry != null && expiry > System.currentTimeMillis();
    }
}
