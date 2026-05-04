package com.diablo.smp.abilities;

import com.diablo.smp.DiabloSmp;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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

    // Stage 2 tracking
    private final Map<UUID, UUID> boundTargets = new HashMap<>(); // User -> Target

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
        switch (stage) {
            case 0 -> { // Stage 1: Soul Exchange
                Entity target = getTarget(player, 15);
                if (target instanceof LivingEntity && target != player) {
                    startSoulExchange(player, (LivingEntity) target);
                } else {
                    player.sendMessage("§c§lDIABLO §8» §7No valid target found!");
                }
            }
            case 1 -> { // Stage 2: Soul Bind
                Entity target = getTarget(player, 15);
                if (target instanceof LivingEntity && target != player) {
                    startSoulBind(player, (LivingEntity) target);
                } else {
                    player.sendMessage("§c§lDIABLO §8» §7No valid target found for Soul Bind!");
                }
            }
            case 2 -> { // Stage 3: Soul Shatter
                if (boundTargets.containsKey(player.getUniqueId())) {
                    executeSoulShatter(player);
                } else {
                    player.sendMessage("§c§lDIABLO §8» §7You must have a target bound first (Stage 2)!");
                }
            }
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

        ArmorStand statue = (ArmorStand) player.getWorld().spawnEntity(statueLoc, EntityType.ARMOR_STAND);
        statue.setBasePlate(false);
        statue.setArms(true);
        statue.setCustomName("§b§l" + player.getName() + "'s Shell");
        statue.setCustomNameVisible(true);
        statue.setInvulnerable(true);
        statue.setGravity(false);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            head.setItemMeta(skullMeta);
        }
        statue.getEquipment().setHelmet(head);

        statues.put(playerUuid, statue);
        controlling.put(playerUuid, victimUuid);

        savedInventories.put(playerUuid, player.getInventory().getContents().clone());
        if (victim instanceof Player victimPlayer) {
            savedInventories.put(victimUuid, victimPlayer.getInventory().getContents().clone());
            savedGameModes.put(victimUuid, victimPlayer.getGameMode());
            player.getInventory().setContents(victimPlayer.getInventory().getContents());
            victimPlayer.getInventory().clear();
            victimPlayer.setGameMode(GameMode.SPECTATOR);
        } else {
            player.getInventory().clear();
        }

        player.teleport(victim.getLocation());
        player.sendMessage("§b§lSoul Shackle §8» §7Possessed §f" + victim.getName());
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
                drawParticleLine(statue.getLocation().clone().add(0, 1, 0), player.getLocation().clone().add(0, 1, 0), Color.AQUA);
                victim.teleport(player.getLocation());
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void startSoulBind(Player player, LivingEntity target) {
        boundTargets.put(player.getUniqueId(), target.getUniqueId());
        player.sendMessage("§b§lSoul Shackle §8» §7You have §fBound §7the soul of §f" + target.getName());
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 2));

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 200 || !player.isOnline() || !target.isValid() || !boundTargets.containsKey(player.getUniqueId())) {
                    boundTargets.remove(player.getUniqueId());
                    this.cancel();
                    return;
                }
                drawParticleLine(player.getLocation().clone().add(0, 1, 0), target.getLocation().clone().add(0, 1, 0), Color.PURPLE);
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    private void executeSoulShatter(Player player) {
        UUID targetUuid = boundTargets.remove(player.getUniqueId());
        Entity target = Bukkit.getEntity(targetUuid);
        if (target instanceof LivingEntity living) {
            living.damage(10.0, player);
            living.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, living.getLocation(), 1);
            living.getWorld().playSound(living.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
            player.sendMessage("§b§lSoul Shackle §8» §fSoul Shattered! §7Target suffered massive damage.");
        }
    }

    private void stopSoulExchange(Player player, LivingEntity victim) {
        UUID playerUuid = player.getUniqueId();
        UUID victimUuid = victim.getUniqueId();
        controlling.remove(playerUuid);
        ArmorStand statue = statues.remove(playerUuid);
        if (statue != null) {
            Location returnLoc = statue.getLocation();
            statue.remove();
            if (player != null && player.isOnline()) {
                player.getInventory().setContents(savedInventories.get(playerUuid));
                player.teleport(returnLoc);
                player.sendMessage("§b§lSoul Shackle §8» §7Connection lost.");
            }
        }
        if (victim instanceof Player victimPlayer && victimPlayer.isOnline()) {
            victimPlayer.getInventory().setContents(savedInventories.get(victimUuid));
            victimPlayer.setGameMode(savedGameModes.getOrDefault(victimUuid, GameMode.SURVIVAL));
            victimPlayer.sendMessage("§b§lSoul Shackle §8» §7Soul returned.");
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
                if (victim instanceof LivingEntity) stopSoulExchange(player, (LivingEntity) victim);
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
        for (int i = 9; i < 36; i++) player.getInventory().setItem(i, barrier);
    }

    private void drawParticleLine(Location loc1, Location loc2, Color color) {
        if (loc1 == null || loc2 == null || loc1.getWorld() != loc2.getWorld()) return;
        Vector direction = loc2.toVector().subtract(loc1.toVector());
        double distance = loc1.distance(loc2);
        if (distance > 100) return;
        for (double d = 0; d < distance; d += 1.5) {
            Location point = loc1.clone().add(direction.clone().normalize().multiply(d));
            loc1.getWorld().spawnParticle(Particle.DUST, point, 1, new Particle.DustOptions(color, 0.6f));
        }
    }

    public boolean isBeingControlled(UUID uuid) { return controlling.containsValue(uuid); }
    public boolean isController(UUID uuid) { return controlling.containsKey(uuid); }
    public boolean isStatue(UUID uuid) { return statues.containsKey(uuid); }
}
