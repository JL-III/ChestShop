package com.Acrobot.ChestShop.Events.tobesorted;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemParseEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final String itemString;
    //TODO: Fix this
    private ItemStack item = null;

    public ItemParseEvent(String itemString) {
        this.itemString = itemString;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Get the item string that should be parsed
     * @return The item string to parse
     */
    public String getItemString() {
        return itemString;
    }

    /**
     * Set the item that the string represents
     * @param item The item for the string
     */
    public void setItem(ItemStack item) {
        this.item = item;
    }

    /**
     * The item that was parsed
     * @return The parsed item or null if none was found
     */
    public ItemStack getItem() {
        return item;
    }
}
