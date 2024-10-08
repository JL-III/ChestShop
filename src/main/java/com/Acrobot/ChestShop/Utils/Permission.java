package com.Acrobot.ChestShop.Utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * @author Acrobot
 */
public enum Permission {
    SHOP_CREATION_BUY("ChestShop.shop.create.buy"),
    SHOP_CREATION_BUY_ID("ChestShop.shop.create.buy."),

    SHOP_CREATION_SELL("ChestShop.shop.create.sell"),
    SHOP_CREATION_SELL_ID("ChestShop.shop.create.sell."),

    SHOP_CREATION("ChestShop.shop.create"),
    SHOP_CREATION_ID("ChestShop.shop.create."),

    BUY("ChestShop.shop.buy"),
    BUY_ID("ChestShop.shop.buy."),

    SELL_ID("ChestShop.shop.sell."),
    SELL("ChestShop.shop.sell"),

    ADMIN("ChestShop.admin"),
    OTHER_NAME("ChestShop.name"),
    OTHER_NAME_CREATE("ChestShop.othername.create"),
    OTHER_NAME_DESTROY("ChestShop.othername.destroy"),
    OTHER_NAME_ACCESS("ChestShop.othername.access"),
    GROUP("ChestShop.group."),

    NOTIFY_TOGGLE("ChestShop.toggle"),
    ACCESS_TOGGLE("ChestShop.accesstoggle"),
    ITEMINFO("ChestShop.iteminfo"),
    SHOPINFO("ChestShop.shopinfo");

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public static boolean has(CommandSender sender, Permission permission) {
        return has(sender, permission.permission);
    }

    public static boolean has(CommandSender sender, String node) {
        return sender.hasPermission(node) || sender.hasPermission(node.toLowerCase(Locale.ROOT));
    }

    public static boolean otherName(Player player, Permission base, String name) {
        boolean hasBase = base != OTHER_NAME && otherName(player, OTHER_NAME, name);
        if (hasBase || has(player, base + ".*")) {
            return !hasPermissionSetFalse(player, base+ "." + name) && !hasPermissionSetFalse(player, base + "." + name.toLowerCase(Locale.ROOT));
        }

        return has(player, base + "." + name) || has(player, base + "." + name.toLowerCase(Locale.ROOT));
    }

    public static boolean hasPermissionSetFalse(CommandSender sender, String permission) {
        return (sender.isPermissionSet(permission) && !sender.hasPermission(permission))
                || (sender.isPermissionSet(permission.toLowerCase(Locale.ROOT)) && !sender.hasPermission(permission.toLowerCase(Locale.ROOT)));
    }

    public org.bukkit.permissions.Permission getPermission() {
        return new org.bukkit.permissions.Permission(permission);
    }

    public String toString() {
        return permission;
    }
}
