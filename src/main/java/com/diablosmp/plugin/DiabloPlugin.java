package com.diablosmp.plugin;

import com.diablosmp.plugin.commands.DiabloCommand;
import com.diablosmp.plugin.commands.TrustCommand;
import com.diablosmp.plugin.listeners.AbsorptionGuiListener;
import com.diablosmp.plugin.listeners.ItemSecurityListener;
import com.diablosmp.plugin.listeners.PlayerListener;
import com.diablosmp.plugin.managers.AbilityManager;
import com.diablosmp.plugin.managers.TrustManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class DiabloPlugin extends JavaPlugin {
    
    public static DiabloPlugin instance;
    public NamespacedKey diabloBookKey;
    private BukkitAudiences adventure; // For Kyori Adventure

    // Managers (public static to access from anywhere if needed, but usually via getInstance())
    public AbilityManager abilityManager;
    public TrustManager trustManager;
    // public CooldownManager cooldownManager; // If you add it

    @Override
    public void onEnable() {
        instance = this;
        
        // Kyori Adventure Initialization
        this.adventure = BukkitAudiences.create(this);
        
        // Initialize NamespacedKey
        diabloBookKey = new NamespacedKey(this, "diablo_book");
        
        // Initialize Managers
        abilityManager = new AbilityManager();
        trustManager = new TrustManager();
        // cooldownManager = new CooldownManager();
        
        // Register Listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(abilityManager, trustManager), this); // Pass trustManager if needed
        getServer().getPluginManager().registerEvents(new ItemSecurityListener(trustManager), this);
        getServer().getPluginManager().registerEvents(new AbsorptionGuiListener(abilityManager), this); // Needs to be registered
        
        // Register Commands
        getCommand("diablo").setExecutor(new DiabloCommand());
        getCommand("diablo").setTabCompleter(new DiabloCommand()); // Set tab completer
        getCommand("trust").setExecutor(new TrustCommand(trustManager));
        
        getLogger().info("🔥 Diablo SMP Plugin (Boss Pack) Enabled Flawlessly!");
        getLogger().info("Version: " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        // Clean up tasks, save data, etc.
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
        getLogger().info("Diablo SMP Plugin Disabled.");
    }

    // Getter for the instance
    public static DiabloPlugin getInstance() {
        return instance;
    }
    
    // Getter for Adventure (Kyori)
    public BukkitAudiences getAdventure() {
        return adventure;
    }
}
