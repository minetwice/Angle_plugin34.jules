package com.diablo.smp.listeners;

import com.diablo.smp.DiabloSmp;
import com.diablo.smp.abilities.Ability;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class AbilityListener implements Listener {
    private final DiabloSmp plugin;

    public AbilityListener(DiabloSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            plugin.getAbilityManager().handleSneak(event.getPlayer());
        }
    }

    @EventHandler
    public void onLeftClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            Ability ability = plugin.getAbilityManager().getAbility(player);
            if (ability != null) {
                int stage = ability.getStage(player);
                if (ability.isOnCooldown(player, stage)) {
                    player.sendMessage("§cAbility is on cooldown! (" + (ability.getRemainingCooldown(player, stage) / 1000) + "s)");
                    return;
                }
                ability.execute(player, stage);
                ability.setCooldown(player, stage);
            }
        }
    }
}
