package com.diablosmp.plugin.commands;

import com.diablosmp.plugin.managers.TrustManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;

public class TrustCommand implements CommandExecutor {
    private final TrustManager trustManager;
    public TrustCommand(TrustManager trustManager) { this.trustManager = trustManager; }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p && args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null && target != p) {
                trustManager.createContract(p, target);
                return true;
            }
        }
        return false;
    }
}
