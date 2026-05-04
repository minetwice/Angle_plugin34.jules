package com.diablosmp.plugin.listeners;

import com.diablosmp.plugin.managers.AbilityManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import java.util.HashMap;
import java.util.UUID;

public class PlayerListener implements Listener {
    private final AbilityManager abilityManager;
    private final HashMap<UUID, Long> sneakTimes = new HashMap<>();

    public PlayerListener(AbilityManager abilityManager) { this.abilityManager = abilityManager; }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        if (!e.isSneaking()) return;
        Player p = e.getPlayer();
        long now = System.currentTimeMillis();
        if (sneakTimes.containsKey(p.getUniqueId()) && (now - sneakTimes.get(p.getUniqueId())) < 500) {
            abilityManager.cycleStage(p.getUniqueId());
            p.sendMessage("§eAbility switched to Stage " + abilityManager.getStage(p.getUniqueId()));
            sneakTimes.remove(p.getUniqueId());
        } else {
            sneakTimes.put(p.getUniqueId(), now);
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player p && e.getEntity() instanceof org.bukkit.entity.LivingEntity target) {
            
            // Ab ability sirf tab chalegi jab player ne absorb ki ho (PdcUtils.hasAbility == true)
            if (com.diablosmp.plugin.utils.PdcUtils.hasAbility(p)) {
                if (abilityManager.getStage(p.getUniqueId()) == 1 && !p.isSneaking()) {
                    abilityManager.executeSoulWeaver(p, target);
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        abilityManager.revertPossession(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDropDuringPossession(PlayerDropItemEvent e) {
        if (abilityManager.activePossessions.containsKey(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cYou cannot throw items while possessing a soul!");
        }
    }
}
