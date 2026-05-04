package com.diablosmp.plugin.commands;

import com.diablosmp.plugin.utils.PdcUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class DiabloCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("diablo.admin") && args.length == 2 && args[0].equalsIgnoreCase("give")) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target != null) {
                ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                ItemMeta meta = book.getItemMeta();
                meta.setDisplayName("§5§lDiablo Ability Book");
                book.setItemMeta(meta);
                PdcUtils.markDiabloBook(book);
                target.getInventory().addItem(book);
                sender.sendMessage("§aGiven Diablo Book to " + target.getName());
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(org.bukkit.command.CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) { list.add("give"); } 
        else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            for (Player p : Bukkit.getOnlinePlayers()) list.add(p.getName());
        }
        return list;
    }
}
