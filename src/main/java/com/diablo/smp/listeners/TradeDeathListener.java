package com.diablo.smp.listeners;

import com.diablo.smp.DiabloSmp;
import com.diablo.smp.abilities.AbilityItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class TradeDeathListener implements Listener {
    private final DiabloSmp plugin;

    public TradeDeathListener(DiabloSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null) {
            UUID trustedUuid = plugin.getTrustManager().getTrustedPlayer(victim);
            if (killer.getUniqueId().equals(trustedUuid)) {
                // Killer is trusted, drop ability books
                List<ItemStack> booksToDrop = new ArrayList<>();
                Iterator<ItemStack> iterator = event.getDrops().iterator();
                while (iterator.hasNext()) {
                    ItemStack item = iterator.next();
                    if (AbilityItemUtils.isAbilityBook(item)) {
                        booksToDrop.add(item);
                        // We keep it in drops but normally our security might prevent it from being in drops if not careful
                        // Actually event.getDrops() contains everything that WOULD drop.
                    }
                }
                // If security listener cancelled drop before, we might need to manually handle it here
            } else {
                // Killer not trusted, remove ability books from drops to prevent theft
                event.getDrops().removeIf(AbilityItemUtils::isAbilityBook);
            }
        } else {
            // No killer, don't drop books (security)
            event.getDrops().removeIf(AbilityItemUtils::isAbilityBook);
        }
    }
}
