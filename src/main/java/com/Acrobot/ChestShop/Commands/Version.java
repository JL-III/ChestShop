package com.Acrobot.ChestShop.Commands;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Events.tobesorted.ChestShopReloadEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * @author Acrobot
 */
public class Version implements CommandExecutor {
    private final ChestShop plugin;

    public Version(ChestShop plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0 && args[0].equals("reload")) {
            plugin.getServer().getPluginManager().callEvent(new ChestShopReloadEvent(sender));

            sender.sendMessage(ChatColor.DARK_GREEN + "The config was reloaded.");
            return true;
        }

        sender.sendMessage(ChatColor.GRAY + plugin.getName() + "'s version is: " + ChatColor.GREEN + plugin.getDescription().getVersion());
        return true;
    }
}
