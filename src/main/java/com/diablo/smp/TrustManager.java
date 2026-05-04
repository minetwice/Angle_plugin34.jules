package com.diablo.smp;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrustManager {
    private final Map<UUID, UUID> trustContracts = new HashMap<>();
    private final Map<UUID, Long> contractExpiry = new HashMap<>();

    public void createTrust(Player player1, Player player2) {
        trustContracts.put(player1.getUniqueId(), player2.getUniqueId());
        contractExpiry.put(player1.getUniqueId(), System.currentTimeMillis() + (5 * 60 * 1000));

        player1.sendMessage("§aTrust contract established with " + player2.getName() + " for 5 minutes.");
        player2.sendMessage("§a" + player1.getName() + " has established a trust contract with you.");
    }

    public boolean isTrustActive(Player player) {
        if (!trustContracts.containsKey(player.getUniqueId())) return false;
        if (System.currentTimeMillis() > contractExpiry.get(player.getUniqueId())) {
            trustContracts.remove(player.getUniqueId());
            contractExpiry.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public UUID getTrustedPlayer(Player player) {
        if (!isTrustActive(player)) return null;
        return trustContracts.get(player.getUniqueId());
    }
}
