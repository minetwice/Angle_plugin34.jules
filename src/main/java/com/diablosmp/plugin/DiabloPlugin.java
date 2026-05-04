package com.diablosmp.plugin;

import com.diablosmp.plugin.commands.DiabloCommand;
import com.diablosmp.plugin.commands.TrustCommand;
import com.diablosmp.plugin.listeners.AbsorptionGuiListener;
import com.diablosmp.plugin.listeners.ItemSecurityListener;
import com.diablosmp.plugin.listeners.PlayerListener;
import com.diablosmp.plugin.managers.AbilityManager;
import com.diablosmp.plugin.managers.CooldownManager;
import com.diablosmp.plugin.managers.TrustManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DiabloPlugin extends JavaPlugin {
    private static DiabloPlugin instance;
    public AbilityManager abilityManager;
    public TrustManager trustManager;
    public CooldownManager cooldownManager;

    @Override
    public void onEnable() {
        instance = this;
        abilityManager = new AbilityManager();
        trustManager = new TrustManager();
        cooldownManager = new CooldownManager();

        getServer().getPluginManager().registerEvents(new PlayerListener(abilityManager), this);
        getServer().getPluginManager().registerEvents(new ItemSecurityListener(trustManager), this);
        getServer().getPluginManager().registerEvents(new AbsorptionGuiListener(), this);

        getCommand("diablo").setExecutor(new DiabloCommand());
        getCommand("diablo").setTabCompleter(new DiabloCommand());
        getCommand("trust").setExecutor(new TrustCommand(trustManager));

        getLogger().info("🔥 Diablo SMP Plugin (Boss Pack) Enabled Successfully!");
    }

    @Override
    public void onDisable() {
        abilityManager.revertAllPossessions(); // Failsafe on restart
    }

    public static DiabloPlugin getInstance() { return instance; }
}
