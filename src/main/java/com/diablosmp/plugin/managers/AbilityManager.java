package com.diablosmp.plugin.managers;

import com.diablosmp.plugin.DiabloPlugin;
import com.diablosmp.plugin.utils.ParticleUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.UUID;

public class AbilityManager {
    public HashMap<UUID, UUID> activePossessions = new HashMap<>(); // Attacker -> Victim
    private HashMap<UUID, ArmorStand> playerStatues = new HashMap<>();
    private HashMap<UUID, ItemStack[]> originalInventories = new HashMap<>();
    private HashMap<UUID, Integer> abilityStages = new HashMap<>();

    public int getStage(UUID id) { return abilityStages.getOrDefault(id, 1); }
    public void cycleStage(UUID id) {
        int current = getStage(id);
        int next = current >= 3 ? 1 : current + 1;
        abilityStages.put(id, next);
    }

    public void executeSoulWeaver(Player attacker, LivingEntity target) {
        if (activePossessions.containsKey(attacker.getUniqueId())) return;

        // Create Statue
        ArmorStand statue = attacker.getWorld().spawn(attacker.getLocation(), ArmorStand.class);
        statue.setInvulnerable(true);
        statue.setGravity(false);
        statue.setCustomNameVisible(true);
        statue.customName(Component.text(attacker.getName() + "'s Empty Shell", NamedTextColor.GRAY));
        statue.getEquipment().setArmorContents(attacker.getInventory().getArmorContents());
        playerStatues.put(attacker.getUniqueId(), statue);

        // Victim to Spectator
        if (target instanceof Player victim) {
            victim.setGameMode(GameMode.SPECTATOR);
            victim.sendMessage(Component.text("Your soul has been evicted for 30s!", NamedTextColor.RED));
        }

        // Setup Attacker
        originalInventories.put(attacker.getUniqueId(), attacker.getInventory().getContents());
        attacker.teleport(target.getLocation());
        
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrier.getItemMeta();
        meta.customName(Component.text("Soul Lock", NamedTextColor.RED));
        barrier.setItemMeta(meta);
        
        for (int i = 9; i < 36; i++) { attacker.getInventory().setItem(i, barrier); }
        activePossessions.put(attacker.getUniqueId(), target.getUniqueId());

        // Timer
        new BukkitRunnable() {
            int time = 30;
            @Override
            public void run() {
                if (time <= 0 || !attacker.isOnline() || attacker.isDead() || !target.isValid()) {
                    revertPossession(attacker.getUniqueId());
                    this.cancel();
                    return;
                }
                ParticleUtils.drawTether(statue.getLocation().add(0, 1, 0), attacker.getLocation().add(0, 1, 0));
                time--;
            }
        }.runTaskTimer(DiabloPlugin.getInstance(), 0, 20L);
    }

    public void revertPossession(UUID attackerId) {
        if (!activePossessions.containsKey(attackerId)) return;
        Player attacker = DiabloPlugin.getInstance().getServer().getPlayer(attackerId);
        UUID victimId = activePossessions.get(attackerId);
        Player victim = DiabloPlugin.getInstance().getServer().getPlayer(victimId);
        
        ArmorStand statue = playerStatues.remove(attackerId);
        if (attacker != null && statue != null) {
            attacker.teleport(statue.getLocation());
            attacker.getInventory().setContents(originalInventories.remove(attackerId));
            attacker.getWorld().spawnParticle(org.bukkit.Particle.SONIC_BOOM, attacker.getLocation(), 1);
        }
        if (statue != null) statue.remove();
        if (victim != null && victim.isOnline()) {
            victim.setGameMode(GameMode.SURVIVAL);
            if (attacker != null) victim.teleport(attacker.getLocation());
        }
        activePossessions.remove(attackerId);
    }

    public void revertAllPossessions() {
        for (UUID id : new HashMap<>(activePossessions).keySet()) revertPossession(id);
    }
}
