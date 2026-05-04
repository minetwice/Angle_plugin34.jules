package com.diablosmp.plugin.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import com.diablosmp.plugin.DiabloPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrustManager {

    // contractMap: { ownerUUID -> { trustedUUID -> expiryTimeMillis } }
    private final Map<UUID, Map<UUID, Long>> contractMap = new HashMap<>();
    private final Map<UUID, UUID> ownerMap = new HashMap<>(); // For easier lookup: { trustedUUID -> ownerUUID }

    public void createContract(Player owner, Player trusted, long durationMillis) {
        UUID ownerId = owner.getUniqueId();
        UUID trustedId = trusted.getUniqueId();

        // Remove any existing contract for the owner or trusted player
        removeContractByOwner(ownerId);
        removeContractByTrusted(trustedId);

        Map<UUID, Long> ownerContracts = contractMap.computeIfAbsent(ownerId, k -> new HashMap<>());
        ownerContracts.put(trustedId, System.currentTimeMillis() + durationMillis);
        ownerMap.put(trustedId, ownerId);

        owner.sendMessage(Component.text("You have trusted ", NamedTextColor.GREEN)
                .append(Component.text(trusted.getName(), NamedTextColor.YELLOW))
                .append(Component.text(" for " + (durationMillis / 1000 / 60) + " minutes.", NamedTextColor.GREEN)));
        trusted.sendMessage(Component.text(owner.getName(), NamedTextColor.YELLOW)
                .append(Component.text(" has trusted you for " + (durationMillis / 1000 / 60) + " minutes.", NamedTextColor.GREEN)));

        // Schedule task to remove contract
        new BukkitRunnable() {
            @Override
            public void run() {
                if (hasActiveContract(ownerId, trustedId)) { // Check if still active before removing
                    removeContract(ownerId, trustedId);
                    owner.sendMessage(Component.text("Your trust contract with ", NamedTextColor.RED)
                            .append(Component.text(trusted.getName(), NamedTextColor.YELLOW))
                            .append(Component.text(" has expired.", NamedTextColor.RED)));
                    trusted.sendMessage(Component.text("Your trust contract with ", NamedTextColor.RED)
                            .append(Component.text(owner.getName(), NamedTextColor.YELLOW))
                            .append(Component.text(" has expired.", NamedTextColor.RED)));
                }
            }
        }.runTaskLater(DiabloPlugin.getInstance(), durationMillis / 50); // RunTaskLater uses ticks (20 ticks/sec)
    }

    public boolean hasActiveContract(UUID ownerId, UUID trustedId) {
        if (!contractMap.containsKey(ownerId)) return false;
        Map<UUID, Long> contracts = contractMap.get(ownerId);
        if (!contracts.containsKey(trustedId)) return false;

        return contracts.get(trustedId) > System.currentTimeMillis();
    }
    
    public boolean hasActiveContract(UUID playerId) {
        return contractMap.containsKey(playerId) && !contractMap.get(playerId).isEmpty();
    }

    public boolean isTrusted(UUID victimId, UUID killerId) {
        if (!ownerMap.containsKey(victimId)) return false;
        UUID ownerId = ownerMap.get(victimId);
        return hasActiveContract(ownerId, victimId) && killerId.equals(ownerId);
    }

    public void removeContract(UUID ownerId, UUID trustedId) {
        if (contractMap.containsKey(ownerId)) {
            contractMap.get(ownerId).remove(trustedId);
            if (contractMap.get(ownerId).isEmpty()) {
                contractMap.remove(ownerId);
            }
        }
        ownerMap.remove(trustedId);
    }
    
    public void removeContractByOwner(UUID ownerId) {
         if (contractMap.containsKey(ownerId)) {
             contractMap.get(ownerId).forEach((trustedId, expiry) -> ownerMap.remove(trustedId));
             contractMap.remove(ownerId);
         }
    }
    
    public void removeContractByTrusted(UUID trustedId) {
        if (ownerMap.containsKey(trustedId)) {
            UUID ownerId = ownerMap.get(trustedId);
            removeContract(ownerId, trustedId);
        }
    }

    // Method to get owner for a trusted player, useful for /untrust or checking contracts
    public UUID getContractOwner(UUID trustedId) {
        return ownerMap.get(trustedId);
    }
}
