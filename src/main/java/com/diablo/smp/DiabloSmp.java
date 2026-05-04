package com.diablo.smp;

import com.diablo.smp.abilities.AbilityManager;
import com.diablo.smp.abilities.HellfireWingsAbility;
import com.diablo.smp.abilities.SoulShackleAbility;
import com.diablo.smp.listeners.*;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

public class DiabloSmp extends JavaPlugin {

    private static DiabloSmp instance;
    private AbilityManager abilityManager;
    private TrustManager trustManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        abilityManager = new AbilityManager();
        trustManager = new TrustManager();

        // Register abilities - Diablo SMP Core
        abilityManager.registerAbility(new SoulShackleAbility(this));
        abilityManager.registerAbility(new HellfireWingsAbility(this));

        getServer().getPluginManager().registerEvents(new AbilityListener(this), this);
        getServer().getPluginManager().registerEvents(new SecurityListener(this), this);
        getServer().getPluginManager().registerEvents(new TradeDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new AbsorptionListener(this), this);
        getServer().getPluginManager().registerEvents(new SoulShackleListener(this), this);

        getCommand("trust").setExecutor(new TrustCommand(this));
        getCommand("trust").setTabCompleter(new TrustCommand(this));
        getCommand("diablo").setExecutor(new DiabloCommand(this));
        getCommand("diablo").setTabCompleter(new DiabloCommand(this));

        Logger logger = getLogger();
        logger.info("DiabloSmp Plugin v1.1 has been enabled!");
    }

    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    public TrustManager getTrustManager() {
        return trustManager;
    }

    @Override
    public void onDisable() {
        if (abilityManager != null) {
            SoulShackleAbility soulShackle = (SoulShackleAbility) abilityManager.getAbilityByName("Soul Shackle");
            if (soulShackle != null) {
                soulShackle.cleanup();
            }
        }
        getLogger().info("DiabloSmp Plugin has been disabled!");
    }

    public static DiabloSmp getInstance() {
        return instance;
    }
}
