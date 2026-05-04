package com.diablo.smp.abilities;

import com.diablo.smp.DiabloSmp;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HellfireWingsAbility extends Ability {
    private final DiabloSmp plugin;
    private final Map<UUID, Long> invulnerability = new HashMap<>();

    public HellfireWingsAbility(DiabloSmp plugin) {
        super("Hellfire Wings");
        this.plugin = plugin;
    }

    @Override
    public long getCooldown(int stage) {
        return switch (stage) {
            case 0 -> 10000; // 10s
            case 1 -> 15000; // 15s
            case 2 -> 120000; // 2 min
            default -> 30000;
        };
    }

    @Override
    public void execute(Player player, int stage) {
        switch (stage) {
            case 0 -> executeInfernalAscent(player);
            case 1 -> executeFireDash(player);
            case 2 -> executePhoenixRebirth(player);
        }
    }

    private void executeInfernalAscent(Player player) {
        player.setVelocity(new Vector(0, 1.2, 0));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks > 20) { this.cancel(); return; }
                player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 10, 0.2, 0.2, 0.2, 0.05);
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void executeFireDash(Player player) {
        Vector dir = player.getLocation().getDirection().normalize().multiply(2.0);
        player.setVelocity(dir);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.2f);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks > 10) { this.cancel(); return; }
                Location loc = player.getLocation();
                player.getWorld().spawnParticle(Particle.LAVA, loc, 5, 0.1, 0.1, 0.1, 0.02);
                if (loc.getBlock().getType() == Material.AIR) {
                    loc.getBlock().setType(Material.FIRE);
                    BukkitRunnable cleanup = new BukkitRunnable() {
                        @Override
                        public void run() { loc.getBlock().setType(Material.AIR); }
                    };
                    cleanup.runTaskLater(plugin, 40);
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void executePhoenixRebirth(Player player) {
        UUID uuid = player.getUniqueId();
        invulnerability.put(uuid, System.currentTimeMillis() + 5000);
        player.sendMessage("§6§lHellfire Wings §8» §fPhoenix Rebirth Activated! §7You are invulnerable for 5s.");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.5f);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 100, 2, 2, 2, 0.1);
                player.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, player.getLocation(), 5);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);
                player.getNearbyEntities(5, 5, 5).forEach(e -> {
                    if (e instanceof org.bukkit.entity.LivingEntity && e != player) {
                        ((org.bukkit.entity.LivingEntity) e).damage(15.0, player);
                        e.setFireTicks(100);
                    }
                });
                invulnerability.remove(uuid);
            }
        }.runTaskLater(plugin, 100); // 5 seconds later
    }

    public boolean isInvulnerable(UUID uuid) {
        if (!invulnerability.containsKey(uuid)) return false;
        if (System.currentTimeMillis() > invulnerability.get(uuid)) {
            invulnerability.remove(uuid);
            return false;
        }
        return true;
    }
}
