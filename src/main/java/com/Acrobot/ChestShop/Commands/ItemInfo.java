package com.Acrobot.ChestShop.Commands;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.StringUtil;
import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Configuration.Messages;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Events.tobesorted.ItemInfoEvent;
import com.Acrobot.ChestShop.Events.tobesorted.ItemParseEvent;
import com.Acrobot.ChestShop.Utils.ItemUtil;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.Acrobot.ChestShop.Configuration.Messages.iteminfo;
import static com.Acrobot.ChestShop.Configuration.Messages.iteminfo_shopname;

/**
 * @author Acrobot
 */
public class ItemInfo implements CommandExecutor {
    private final Plugin plugin;
    private final ItemUtil itemUtil;

    public ItemInfo(Plugin plugin, ItemUtil itemUtil) {
        this.plugin = plugin;
        this.itemUtil = itemUtil;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ItemStack item;

        if (args.length == 0) {
            if (!(sender instanceof HumanEntity)) {
                return false;
            }

            item = ((HumanEntity) sender).getItemInHand();
        } else {
            ItemParseEvent parseEvent = new ItemParseEvent(StringUtil.joinArray(args));
            Bukkit.getPluginManager().callEvent(parseEvent);
            item = parseEvent.getItem();
        }

        if (MaterialUtil.isEmpty(item)) {
            return false;
        }

        iteminfo.send(sender);
        if (!sendItemName(sender, item, Messages.iteminfo_fullname)) return true;

        try {
            iteminfo_shopname.send(sender, "item", itemUtil.getSignName(item));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Error while generating shop sign name. Please contact an admin or take a look at the console/log!");
            plugin.getLogger().log(Level.SEVERE, "Error while generating shop sign item name", e);
            return true;
        }

        ItemInfoEvent event = new ItemInfoEvent(sender, item);
        plugin.getServer().getPluginManager().callEvent(event);

        return true;
    }

    public boolean sendItemName(CommandSender sender, ItemStack item, Messages.Message message) {
        try {
            Map<String, String> replacementMap = ImmutableMap.of("item", itemUtil.getName(item));
            if (!Properties.SHOWITEM_MESSAGE || !(sender instanceof Player)) {
                message.send(sender, replacementMap);
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Error while generating full name. Please contact an admin or take a look at the console/log!");
            plugin.getLogger().log(Level.SEVERE, "Error while generating full item name", e);
            return false;
        }
        return true;
    }
}
