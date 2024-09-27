package com.Acrobot.ChestShop.Events.Protection;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;

/**
 * @author Acrobot
 */
public class BuildPermissionEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Location chest;
    private final Location sign;

    private boolean allowed = true;

    public BuildPermissionEvent(Player player, @Nullable Location chest, Location sign) {
        this.player = player;
        this.chest = chest;
        this.sign = sign;
    }

    public Player getPlayer() {
        return player;
    }

    public @Nullable Location getChest() {
        return chest;
    }

    public Location getSign() {
        return sign;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void allow(boolean yesOrNot) {
        allowed = yesOrNot;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return !isAllowed();
    }

    @Override
    public void setCancelled(boolean cancel) {
        allow(!cancel);
    }
}
