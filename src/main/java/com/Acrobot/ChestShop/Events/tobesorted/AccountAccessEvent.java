package com.Acrobot.ChestShop.Events.tobesorted;

import com.Acrobot.ChestShop.Database.Account;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an access request for a specific account.
 */
public class AccountAccessEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Account account;
    private boolean canAccess = false;

    public AccountAccessEvent(Player player, Account account) {
        super(player);
        this.account = account;
    }

    /**
     * The account to check the access for
     *
     * @return The account
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Whether the player can access the account.
     *
     * @return Whether the player can access the account
     */
    public boolean canAccess() {
        return canAccess;
    }

    /**
     * Set whether the player can access the account.
     *
     * @param canAccess Whether the player can access the account
     */
    public void setAccess(boolean canAccess) {
        this.canAccess = canAccess;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
