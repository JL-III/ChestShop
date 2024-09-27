package com.Acrobot.ChestShop.Events.tobesorted;

import com.Acrobot.ChestShop.Database.Account;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

import static com.Acrobot.ChestShop.Events.tobesorted.PreTransactionEvent.TransactionOutcome.OTHER;
import static com.Acrobot.ChestShop.Events.tobesorted.PreTransactionEvent.TransactionOutcome.TRANSACTION_SUCCESSFUL;

/**
 * Represents a state before transaction occurs
 *
 * @author Acrobot
 */
public class PreTransactionEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Player client;
    private final Account ownerAccount;

    private final TransactionEvent.TransactionType transactionType;
    private final Sign sign;

    private final Inventory ownerInventory;
    private final Inventory clientInventory;

    private ItemStack[] items;

    private BigDecimal exactPrice;

    private TransactionOutcome transactionOutcome = TRANSACTION_SUCCESSFUL;

    public PreTransactionEvent(Inventory ownerInventory, Inventory clientInventory, ItemStack[] items, BigDecimal exactPrice, Player client, Account ownerAccount, Sign sign, TransactionEvent.TransactionType type) {
        this.ownerInventory = ownerInventory;
        this.clientInventory = (clientInventory == null ? client.getInventory() : clientInventory);

        this.items = items;
        this.exactPrice = exactPrice;

        this.client = client;
        this.ownerAccount = ownerAccount;

        this.sign = sign;
        this.transactionType = type;
    }

    /**
     * @deprecated Use {@link #PreTransactionEvent(Inventory, Inventory, ItemStack[], BigDecimal, Player, Account, Sign, TransactionEvent.TransactionType)}
     */
    @Deprecated
    public PreTransactionEvent(Inventory ownerInventory, Inventory clientInventory, ItemStack[] items, double price, Player client, Account ownerAccount, Sign sign, TransactionEvent.TransactionType type) {
        this(ownerInventory, clientInventory, items, BigDecimal.valueOf(price), client, ownerAccount, sign, type);
    }

    /**
     * @return Shop's sign
     */
    public Sign getSign() {
        return sign;
    }

    /**
     * Get the exact total price
     *
     * @return Exact total price of the items
     */
    public BigDecimal getExactPrice() {
        return exactPrice;
    }

    /**
     * Sets the exact price of the items
     *
     * @param exactPrice Price of the items
     */
    public void setExactPrice(BigDecimal exactPrice) {
        this.exactPrice = exactPrice;
    }

    /**
     * Get the total price
     *
     * @return Total price of the items
     * @deprecated Use {@link #getExactPrice()}
     */
    @Deprecated
    public double getPrice() {
        return exactPrice.doubleValue();
    }

    /**
     * Sets the price of the items
     *
     * @param price Price of the items
     * @deprecated Use {@link #setExactPrice(BigDecimal)}
     */
    @Deprecated
    public void setPrice(double price) {
        this.exactPrice = BigDecimal.valueOf(price);
    }

    /**
     * Sets the stock
     *
     * @param stock Stock
     */
    public void setStock(ItemStack... stock) {
        items = stock;
    }

    /**
     * @return Stock available
     */
    public ItemStack[] getStock() {
        return items;
    }

    /**
     * @return Shop's client
     */
    public Player getClient() {
        return client;
    }

    /**
     * @return Account of the shop's owner
     */
    public Account getOwnerAccount() {
        return ownerAccount;
    }

    /**
     * @return Owner's inventory
     */
    public Inventory getOwnerInventory() {
        return ownerInventory;
    }

    /**
     * @return Client's inventory
     */
    public Inventory getClientInventory() {
        return clientInventory;
    }

    /**
     * @return Transaction's type
     */
    public TransactionEvent.TransactionType getTransactionType() {
        return transactionType;
    }

    /**
     * @return Is the transaction cancelled?
     */
    public boolean isCancelled() {
        return transactionOutcome != TRANSACTION_SUCCESSFUL;
    }

    @Override
    public void setCancelled(boolean cancel) {
        if (cancel) {
            transactionOutcome = OTHER;
        } else {
            transactionOutcome = TRANSACTION_SUCCESSFUL;
        }
    }

    /**
     * @return Transaction's outcome
     */
    public TransactionOutcome getTransactionOutcome() {
        return transactionOutcome;
    }

    /**
     * Sets the outcome of the transaction
     *
     * @param reason Transction's outcome
     */
    public void setCancelled(TransactionOutcome reason) {
        transactionOutcome = reason;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum TransactionOutcome {
        SHOP_DOES_NOT_BUY_THIS_ITEM,
        SHOP_DOES_NOT_SELL_THIS_ITEM,

        CLIENT_DOES_NOT_HAVE_PERMISSION,

        CLIENT_DOES_NOT_HAVE_ENOUGH_MONEY,
        SHOP_DOES_NOT_HAVE_ENOUGH_MONEY,

        CLIENT_DEPOSIT_FAILED,
        SHOP_DEPOSIT_FAILED,

        NOT_ENOUGH_SPACE_IN_CHEST,
        NOT_ENOUGH_SPACE_IN_INVENTORY,

        NOT_ENOUGH_STOCK_IN_CHEST,
        NOT_ENOUGH_STOCK_IN_INVENTORY,

        INVALID_SHOP,
        INVALID_CLIENT_NAME,

        SPAM_CLICKING_PROTECTION,
        CREATIVE_MODE_PROTECTION,
        SHOP_IS_RESTRICTED,

        OTHER, //For plugin use!

        TRANSACTION_SUCCESSFUL
    }
}
