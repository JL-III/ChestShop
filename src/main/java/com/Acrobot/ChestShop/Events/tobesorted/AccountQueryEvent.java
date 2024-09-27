package com.Acrobot.ChestShop.Events.tobesorted;

import com.Acrobot.ChestShop.Database.Account;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a query for an account by using the name (e.g. from the shop sign)
 */
public class AccountQueryEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String name;
    private Account account = null;
    private boolean searchOfflinePlayers = false;

    public AccountQueryEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    /**
     * Get whether offline player data should be searched (too)
     * @return Whether offline player data should be searched (too)
     */
    public boolean searchOfflinePlayers() {
        return searchOfflinePlayers;
    }

    /**
     * Set whether offline player data should be searched (too).
     * This could lead to network lookups if the player by the name never joined the server!
     * @param searchOfflinePlayers Whether offline player data should be searched (too)
     */
    public void searchOfflinePlayers(boolean searchOfflinePlayers) {
        this.searchOfflinePlayers = searchOfflinePlayers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
