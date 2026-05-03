package com.diablo.smp.abilities;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityManager {
    private final Map<UUID, Ability> playerAbilities = new HashMap<>();
    private final Map<String, Ability> registeredAbilities = new HashMap<>();
    private final Map<UUID, Long> lastSneak = new HashMap<>();

    public void registerAbility(Ability ability) {
        registeredAbilities.put(ability.getName(), ability);
    }

    public Ability getAbilityByName(String name) {
        return registeredAbilities.get(name);
    }

    public void setAbility(Player player, Ability ability) {
        playerAbilities.put(player.getUniqueId(), ability);
    }

    public Ability getAbility(Player player) {
        return playerAbilities.get(player.getUniqueId());
    }

    public void handleSneak(Player player) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (lastSneak.containsKey(uuid) && (now - lastSneak.get(uuid)) < 500) {
            Ability ability = getAbility(player);
            if (ability != null) {
                ability.nextStage(player);
            }
        }
        lastSneak.put(uuid, now);
    }
}
