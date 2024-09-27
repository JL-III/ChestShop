package com.Acrobot.ChestShop.Events.tobesorted;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a /shopinfo call or middle-click on a sign
 *
 * @author Phoenix616
 */
public class ShopInfoEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Player sender;
    private final Sign sign;
    private boolean cancelled = false;

    public ShopInfoEvent(Player sender, Sign sign) {
        this.sender = sender;
        this.sign = sign;
    }

    /**
     * @return The Player who initiated the call
     */
    public Player getSender() {
        return sender;
    }

    /**
     * @return The shop sign
     */
    public Sign getSign() {
        return sign;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
