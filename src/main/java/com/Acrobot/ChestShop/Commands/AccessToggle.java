package com.Acrobot.ChestShop.Commands;

import com.Acrobot.ChestShop.Configuration.Messages;
import com.google.common.base.Preconditions;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author g--o
 */
public class AccessToggle implements CommandExecutor {
    private static final Set<UUID> toggledPlayers = new HashSet<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if (setIgnoring(player, !isIgnoring(player))) {
            Messages.TOGGLE_ACCESS_OFF.sendWithPrefix(player);
        } else {
            Messages.TOGGLE_ACCESS_ON.sendWithPrefix(player);
        }

        return true;
    }

    public static boolean isIgnoring(OfflinePlayer player) {
        return player != null && isIgnoring(player.getUniqueId());
    }

    private static boolean isIgnoring(UUID playerId) {
        return toggledPlayers.contains(playerId);
    }

    public static boolean setIgnoring(Player player, boolean ignoring) {
        Preconditions.checkNotNull(player); // Make sure the player instance is not null, in case there are any errors in the code

        if (ignoring) {
            toggledPlayers.add(player.getUniqueId());
        } else {
            toggledPlayers.remove(player.getUniqueId());
        }

        return ignoring;
    }
}
