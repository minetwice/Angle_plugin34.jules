package com.diablo.smp.listeners;

import com.diablo.smp.DiabloSmp;
import com.diablo.smp.abilities.SoulShackleAbility;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class SoulShackleListener implements Listener {
    private final DiabloSmp plugin;

    public SoulShackleListener(DiabloSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            SoulShackleAbility ability = (SoulShackleAbility) plugin.getAbilityManager().getAbilityByName("Soul Shackle");
            if (ability != null && ability.isStatue(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        SoulShackleAbility ability = (SoulShackleAbility) plugin.getAbilityManager().getAbilityByName("Soul Shackle");
        if (ability != null && ability.isStatue(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        SoulShackleAbility ability = (SoulShackleAbility) plugin.getAbilityManager().getAbilityByName("Soul Shackle");
        if (ability != null && ability.isBeingControlled(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BARRIER) {
            if (event.getCurrentItem().getItemMeta().getDisplayName().equals("§c§lLOCKED")) {
                event.setCancelled(true);
            }
        }
    }
}
