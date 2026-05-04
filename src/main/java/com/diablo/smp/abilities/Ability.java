package com.diablo.smp.abilities;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class Ability {
    private final String name;
    private final Map<UUID, Long>[] cooldowns;
    private final Map<UUID, Integer> currentStage = new HashMap<>();

    @SuppressWarnings("unchecked")
    public Ability(String name) {
        this.name = name;
        this.cooldowns = new HashMap[3];
        for (int i = 0; i < 3; i++) {
            cooldowns[i] = new HashMap<>();
        }
    }

    public String getName() {
        return name;
    }

    public int getStage(Player player) {
        return currentStage.getOrDefault(player.getUniqueId(), 0);
    }

    public void nextStage(Player player) {
        int next = (getStage(player) + 1) % 3;
        currentStage.put(player.getUniqueId(), next);
        player.sendMessage("§aAbility stage switched to: " + (next + 1));
    }

    public abstract long getCooldown(int stage);

    public boolean isOnCooldown(Player player, int stage) {
        if (!cooldowns[stage].containsKey(player.getUniqueId())) return false;
        long lastUse = cooldowns[stage].get(player.getUniqueId());
        return (System.currentTimeMillis() - lastUse) < getCooldown(stage);
    }

    public void setCooldown(Player player, int stage) {
        cooldowns[stage].put(player.getUniqueId(), System.currentTimeMillis());
    }

    public long getRemainingCooldown(Player player, int stage) {
        if (!isOnCooldown(player, stage)) return 0;
        return getCooldown(stage) - (System.currentTimeMillis() - cooldowns[stage].get(player.getUniqueId()));
    }

    public abstract void execute(Player player, int stage);
}
