package com.Acrobot.ChestShop.Events.tobesorted;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a plugin reload call
 *
 * @author Acrobot
 */
public class ChestShopReloadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final CommandSender sender;

    public ChestShopReloadEvent(CommandSender sender) {
        this.sender = sender;
    }

    /**
     * @return CommandSender who initiated the call
     */
    public CommandSender getSender() {
        return sender;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
