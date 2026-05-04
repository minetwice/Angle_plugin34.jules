package com.diablosmp.plugin.utils;

import com.diablosmp.plugin.DiabloPlugin;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleUtils {
    public static void playAbsorptionAnimation(Player player, Color color) {
        new BukkitRunnable() {
            double t = 0;
            @Override
            public void run() {
                t += Math.PI / 8;
                Location loc = player.getLocation();
                double x = 1.5 * Math.cos(t);
                double z = 1.5 * Math.sin(t);
                double y = t * 0.15;
                
                loc.add(x, y, z);
                player.getWorld().spawnParticle(Particle.DUST, loc, 5, new Particle.DustOptions(color, 1.5f));
                
                if (y > 2.2) {
                    createHalo(player.getLocation().add(0, 2.2, 0), color);
                    player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation().add(0, 1, 0), 2);
                    this.cancel();
                }
            }
        }.runTaskTimer(DiabloPlugin.getInstance(), 0, 1);
    }

    private static void createHalo(Location loc, Color color) {
        for (int i = 0; i < 360; i += 15) {
            double angle = i * Math.PI / 180;
            loc.getWorld().spawnParticle(Particle.DUST, loc.clone().add(0.4 * Math.cos(angle), 0, 0.4 * Math.sin(angle)), 1, new Particle.DustOptions(color, 1f));
        }
    }

    public static void drawTether(Location loc1, Location loc2) {
        double dist = loc1.distance(loc2);
        for (double d = 0; d <= dist; d += 0.5) {
            Location pt = loc1.clone().add(loc2.clone().subtract(loc1).toVector().normalize().multiply(d));
            pt.getWorld().spawnParticle(Particle.WITCH, pt, 1, 0, 0, 0, 0);
        }
    }
}
