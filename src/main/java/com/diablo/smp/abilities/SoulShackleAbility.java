package com.diablo.smp.abilities;

import com.diablo.smp.DiabloSmp;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SoulShackleAbility extends Ability {
    private final DiabloSmp plugin;
    private final Map<UUID, UUID> controlling = new HashMap<>(); // Controller -> Victim
    private final Map<UUID, ArmorStand> statues = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final Map<UUID, GameMode> savedGameModes = new HashMap<>();

    public SoulShackleAbility(DiabloSmp plugin) {
        super("Soul Shackle");
        this.plugin = plugin;
    }

    @Override
    public long getCooldown(int stage) {
        return switch (stage) {
            case 0 -> 60000; // 1 min
            case 1 -> 45000;
            case 2 -> 30000;
            default -> 60000;
        };
    }

    @Override
    public void execute(Player player, int stage) {
        if (stage == 0) {
            Entity target = getTarget(player, 15);
            if (target instanceof LivingEntity && target != player) {
                startSoulExchange(player, (LivingEntity) target);
            } else {
                player.sendMessage("§c§lDIABLO §8» §7No valid target found! Look at a player or mob.");
            }
        } else {
            player.sendMessage("§c§lDIABLO §8» §eStage " + (stage + 1) + " is under development.");
        }
    }

    private Entity getTarget(Player player, int range) {
        var result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getLocation().getDirection(), range, 1.0, (entity) -> entity instanceof LivingEntity && entity != player);
        return result != null ? result.getHitEntity() : null;
    }

    private void startSoulExchange(Player player, LivingEntity victim) {
        UUID playerUuid = player.getUniqueId();
        UUID victimUuid = victim.getUniqueId();
        Location statueLoc = player.getLocation().clone();

        // Create Realistic Statue
        ArmorStand statue = (ArmorStand) player.getWorld().spawnEntity(statueLoc, EntityType.ARMOR_STAND);
        statue.setBasePlate(false);
        statue.setArms(true);
        statue.setCustomName("§b§l" + player.getName() + "'s Physical Shell");
        statue.setCustomNameVisible(true);
        statue.setInvulnerable(true);
        statue.setGravity(false);

        // Visuals
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            head.setItemMeta(skullMeta);
        }
        statue.getEquipment().setHelmet(head);
        statue.getEquipment().setChestplate(player.getInventory().getChestplate());
        statue.getEquipment().setLeggings(player.getInventory().getLeggings());
        statue.getEquipment().setBoots(player.getInventory().getBoots());
        statue.getEquipment().setItemInMainHand(player.getInventory().getItemInMainHand());

        statues.put(playerUuid, statue);
        controlling.put(playerUuid, victimUuid);

        // Save states
        savedInventories.put(playerUuid, player.getInventory().getContents().clone());
        if (victim instanceof Player) {
            Player victimPlayer = (Player) victim;
            savedInventories.put(victimUuid, victimPlayer.getInventory().getContents().clone());
            savedGameModes.put(victimUuid, victimPlayer.getGameMode());

            player.getInventory().setContents(victimPlayer.getInventory().getContents());
            victimPlayer.getInventory().clear();
            victimPlayer.setGameMode(GameMode.SPECTATOR);
        } else {
            player.getInventory().clear();
        }

        player.teleport(victim.getLocation());
        player.sendMessage("§b§lSoul Shackle §8» §7You have possessed §f" + victim.getName());

        lockInnerInventory(player);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 600 || !player.isOnline() || !victim.isValid()) {
                    stopSoulExchange(player, victim);
                    this.cancel();
                    return;
                }

                drawParticleLine(statue.getLocation().clone().add(0, 1, 0), player.getLocation().clone().add(0, 1, 0));
                victim.teleport(player.getLocation());

                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void stopSoulExchange(Player player, LivingEntity victim) {
        UUID playerUuid = player.getUniqueId();
        UUID victimUuid = victim.getUniqueId();

        controlling.remove(playerUuid);
        ArmorStand statue = statues.remove(playerUuid);
        Location returnLoc = null;
        if (statue != null) {
            returnLoc = statue.getLocation();
            statue.remove();
        }

        if (player != null && player.isOnline()) {
            player.getInventory().setContents(savedInventories.get(playerUuid));
            if (returnLoc != null) player.teleport(returnLoc);
            player.sendMessage("§b§lSoul Shackle §8» §7The soul connection has been severed.");
        }

        if (victim instanceof Player victimPlayer && victimPlayer.isOnline()) {
            victimPlayer.getInventory().setContents(savedInventories.get(victimUuid));
            victimPlayer.setGameMode(savedGameModes.getOrDefault(victimUuid, GameMode.SURVIVAL));
            victimPlayer.sendMessage("§b§lSoul Shackle §8» §7Your soul has returned to its vessel.");
        }

        savedInventories.remove(playerUuid);
        savedInventories.remove(victimUuid);
        savedGameModes.remove(victimUuid);
    }

    public void cleanup() {
        for (UUID playerUuid : new java.util.HashSet<>(statues.keySet())) {
            Player player = Bukkit.getPlayer(playerUuid);
            UUID victimUuid = controlling.get(playerUuid);
            if (victimUuid != null) {
                Entity victim = Bukkit.getEntity(victimUuid);
                if (victim instanceof LivingEntity) {
                    stopSoulExchange(player, (LivingEntity) victim);
                }
            }
        }
    }

    private void lockInnerInventory(Player player) {
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrier.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lLOCKED");
            barrier.setItemMeta(meta);
        }
        for (int i = 9; i < 36; i++) {
            player.getInventory().setItem(i, barrier);
        }
    }

    private void drawParticleLine(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null || loc1.getWorld() != loc2.getWorld()) return;
        Vector direction = loc2.toVector().subtract(loc1.toVector());
        double distance = loc1.distance(loc2);
        if (distance > 100) return;

        double spacing = 1.5;
        for (double d = 0; d < distance; d += spacing) {
            Location point = loc1.clone().add(direction.clone().normalize().multiply(d));
            loc1.getWorld().spawnParticle(Particle.DUST, point, 1, new Particle.DustOptions(Color.AQUA, 0.6f));
        }
    }

    public boolean isBeingControlled(UUID uuid) {
        return controlling.containsValue(uuid);
    }

    public boolean isController(UUID uuid) {
        return controlling.containsKey(uuid);
    }

    public boolean isStatue(UUID uuid) {
        return statues.containsKey(uuid);
    }
}
