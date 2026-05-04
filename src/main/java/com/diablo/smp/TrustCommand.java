package com.diablo.smp;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrustCommand implements CommandExecutor, TabCompleter {
    private final DiabloSmp plugin;

    public TrustCommand(DiabloSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length != 1) {
            player.sendMessage("§cUsage: /trust <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§cPlayer not found.");
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage("§cYou cannot trust yourself.");
            return true;
        }

        plugin.getTrustManager().createTrust(player, target);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (sender instanceof Player && p.getName().equals(sender.getName())) continue;
                players.add(p.getName());
            }
            List<String> suggestions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], players, suggestions);
            Collections.sort(suggestions);
            return suggestions;
        }
        return Collections.emptyList();
    }
}
