package com.diablo.smp.listeners;

import com.diablo.smp.DiabloSmp;
import com.diablo.smp.abilities.HellfireWingsAbility;
import com.diablo.smp.abilities.SoulShackleAbility;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class SoulShackleListener implements Listener {
    private final DiabloSmp plugin;

    public SoulShackleListener(DiabloSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            HellfireWingsAbility hellfire = (HellfireWingsAbility) plugin.getAbilityManager().getAbilityByName("Hellfire Wings");
            if (hellfire != null && hellfire.isInvulnerable(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        SoulShackleAbility ability = (SoulShackleAbility) plugin.getAbilityManager().getAbilityByName("Soul Shackle");
        // The Prompt says the user (controller) cannot throw items while controlling.
        if (ability != null && ability.isController(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BARRIER) {
            if (event.getCurrentItem().hasItemMeta() && "§c§lLOCKED".equals(event.getCurrentItem().getItemMeta().getDisplayName())) {
                event.setCancelled(true);
            }
        }
    }
}
