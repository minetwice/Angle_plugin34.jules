package com.diablo.smp;

import com.diablo.smp.abilities.Ability;
import com.diablo.smp.abilities.AbilityItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DiabloCommand implements CommandExecutor, TabCompleter {
    private final DiabloSmp plugin;

    public DiabloCommand(DiabloSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("diablo.admin")) {
            sender.sendMessage("§cYou don't have permission.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§eDiabloSmp Admin Commands:");
            sender.sendMessage("§7/diablo give <player> <ability>");
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (args.length < 3) {
                sender.sendMessage("§cUsage: /diablo give <player> <ability>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found.");
                return true;
            }

            String abilityName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            Ability ability = plugin.getAbilityManager().getAbilityByName(abilityName);

            if (ability == null) {
                sender.sendMessage("§cAbility not found.");
                return true;
            }

            ItemStack book = AbilityItemUtils.createAbilityBook(ability.getName());
            target.getInventory().addItem(book);
            sender.sendMessage("§aGave " + ability.getName() + " book to " + target.getName());
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("give").stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return List.of("Soul Shackle").stream().filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
