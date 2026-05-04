package com.diablo.smp.listeners;

import com.diablo.smp.DiabloSmp;
import com.diablo.smp.abilities.AbilityItemUtils;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SecurityListener implements Listener {
    private final DiabloSmp plugin;
    private final Map<UUID, Long> lastSecurityNotify = new HashMap<>();

    public SecurityListener(DiabloSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (AbilityItemUtils.isAbilityBook(event.getItemDrop().getItemStack())) {
            Player player = event.getPlayer();

            // Check for /trust logic later. For now, basic block.
            if (!plugin.getTrustManager().isTrustActive(player)) {
                event.setCancelled(true);
                handleSecurityWarning(player);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getType() != InventoryType.PLAYER) {
            if (AbilityItemUtils.isAbilityBook(event.getCurrentItem()) || AbilityItemUtils.isAbilityBook(event.getCursor())) {
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof Player) {
                    handleSecurityWarning((Player) event.getWhoClicked());
                }
            }
        }
    }

    private void handleSecurityWarning(Player player) {
        long now = System.currentTimeMillis();
        if (!lastSecurityNotify.containsKey(player.getUniqueId()) || (now - lastSecurityNotify.get(player.getUniqueId())) > 60000) {
            player.sendMessage("§c§lSECURITY: §7You cannot throw or store this powerful artifact!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation().add(0, 1, 0), 20, 0.2, 0.2, 0.2, 0.05);
            lastSecurityNotify.put(player.getUniqueId(), now);
        }
    }
}
