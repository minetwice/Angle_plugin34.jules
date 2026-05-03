package com.diablo.smp.listeners;

import com.diablo.smp.DiabloSmp;
import com.diablo.smp.abilities.AbilityItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class AbsorptionListener implements Listener {
    private final DiabloSmp plugin;
    private final String GUI_TITLE = "§8Ability Absorption";

    public AbsorptionListener(DiabloSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (AbilityItemUtils.isAbilityBook(item)) {
                openAbsorptionGUI(event.getPlayer());
            }
        }
    }

    private void openAbsorptionGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);
        // Fill with glass
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) {
            if (i != 13) gui.setItem(i, glass);
        }
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(GUI_TITLE)) {
            if (event.getRawSlot() == 13) {
                ItemStack item = event.getCursor();
                if (AbilityItemUtils.isAbilityBook(item)) {
                    Player player = (Player) event.getWhoClicked();
                    // Process absorption
                    String abilityName = item.getItemMeta().getDisplayName().replace("§6§l", "").replace(" §b[Ability Book]", "");

                    com.diablo.smp.abilities.Ability ability = plugin.getAbilityManager().getAbilityByName(abilityName);
                    if (ability != null) {
                        plugin.getAbilityManager().setAbility(player, ability);
                        event.setCursor(null);
                        player.closeInventory();
                        playAbsorptionEffect(player, abilityName);
                    } else {
                        player.sendMessage("§cInvalid ability book!");
                    }
                }
            } else if (event.getRawSlot() < 27) {
                event.setCancelled(true);
            }
        }
    }

    private void playAbsorptionEffect(Player player, String abilityName) {
        player.sendMessage("§6§lYou have absorbed the " + abilityName + " ability!");
        player.playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1.0f, 1.0f);

        new BukkitRunnable() {
            double t = 0;
            @Override
            public void run() {
                t += 0.2;
                double x = Math.sin(t) * 1.5;
                double z = Math.cos(t) * 1.5;
                player.getWorld().spawnParticle(Particle.DRAGON_BREATH, player.getLocation().add(x, t, z), 5, 0, 0, 0, 0.02);

                if (t >= 2.0) {
                    this.cancel();
                    playCrownEffect(player);
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void playCrownEffect(Player player) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks++;
                for (double i = 0; i < Math.PI * 2; i += Math.PI / 8) {
                    double x = Math.sin(i) * 0.5;
                    double z = Math.cos(i) * 0.5;
                    player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(x, 2.2, z), 1, new Particle.DustOptions(Color.YELLOW, 1));
                }
                if (ticks > 40) this.cancel();
            }
        }.runTaskTimer(plugin, 0, 2);
    }
}
