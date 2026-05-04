package com.diablosmp.plugin;

import org.bukkit.plugin.java.JavaPlugin;
import com.diablosmp.plugin.managers.AbilityManager;
import com.diablosmp.plugin.managers.TrustManager;
import com.diablosmp.plugin.commands.DiabloCommand;
import com.diablosmp.plugin.commands.TrustCommand;

public class DiabloPlugin extends JavaPlugin {

    private static DiabloPlugin instance;
    private AbilityManager abilityManager;
    private TrustManager trustManager;

    @Override
    public void onEnable() {
        instance = this;
        this.abilityManager = new AbilityManager();
        this.trustManager = new TrustManager();

        getCommand("diablo").setExecutor(new DiabloCommand());
        getCommand("trust").setExecutor(new TrustCommand());

        getLogger().info("DiabloPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("DiabloPlugin has been disabled!");
    }

    public static DiabloPlugin getInstance() {
        return instance;
    }
}