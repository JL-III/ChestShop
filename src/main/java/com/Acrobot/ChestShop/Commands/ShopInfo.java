package com.Acrobot.ChestShop.Commands;

import com.Acrobot.ChestShop.Configuration.Messages;
import com.Acrobot.ChestShop.Events.tobesorted.ShopInfoEvent;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.uBlock;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * @author Phoenix616
 */
public class ShopInfo implements CommandExecutor {
    private final Plugin plugin;

    public ShopInfo(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (sender instanceof Player) {
            Block target = ((Player) sender).getTargetBlockExact(5);
            if (target != null) {
                Sign sign = null;
                if (ChestShopSign.isValid(target)) {
                    sign = (Sign) target.getState();
                } else if (uBlock.couldBeShopContainer(target)) {
                    sign = uBlock.getConnectedSign(target);
                }

                if (sign != null) {
                    ShopInfoEvent event = new ShopInfoEvent((Player) sender, sign);
                    plugin.getServer().getPluginManager().callEvent(event);
                } else {
                    Messages.NO_SHOP_FOUND.sendWithPrefix(sender);
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Command must be run by a player!");
        }
        return true;
    }
}
