package com.Acrobot.ChestShop.Listeners.PreTransaction;

import com.Acrobot.Breeze.Utils.InventoryUtil;
import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Events.Economy.CurrencyCheckEvent;
import com.Acrobot.ChestShop.Events.tobesorted.PreTransactionEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import static com.Acrobot.ChestShop.Events.tobesorted.PreTransactionEvent.TransactionOutcome.*;
import static com.Acrobot.ChestShop.Events.tobesorted.TransactionEvent.TransactionType.BUY;
import static com.Acrobot.ChestShop.Events.tobesorted.TransactionEvent.TransactionType.SELL;

/**
 * @author Acrobot
 */
public class AmountAndPriceChecker implements Listener {
    private final Plugin plugin;

    public AmountAndPriceChecker(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBuyItemCheck(PreTransactionEvent event) {
        if (event.getTransactionType() != BUY) {
            return;
        }

        ItemStack[] stock = event.getStock();
        Inventory ownerInventory = event.getOwnerInventory();

        CurrencyCheckEvent currencyCheckEvent = new CurrencyCheckEvent(event.getExactPrice(), event.getClient());
        plugin.getServer().getPluginManager().callEvent(currencyCheckEvent);

        if (!currencyCheckEvent.hasEnough()) {
            event.setCancelled(CLIENT_DOES_NOT_HAVE_ENOUGH_MONEY);
            return;
        }

        if (!InventoryUtil.hasItems(stock, ownerInventory)) {
            event.setCancelled(NOT_ENOUGH_STOCK_IN_CHEST);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSellItemCheck(PreTransactionEvent event) {
        if (event.getTransactionType() != SELL) {
            return;
        }

        ItemStack[] stock = event.getStock();
        Inventory clientInventory = event.getClientInventory();

        CurrencyCheckEvent currencyCheckEvent = new CurrencyCheckEvent(event.getExactPrice(),
                                                        event.getOwnerAccount().getUuid(),
                                                        event.getSign().getWorld());
        plugin.getServer().getPluginManager().callEvent(currencyCheckEvent);

        if (!currencyCheckEvent.hasEnough()) {
            event.setCancelled(SHOP_DOES_NOT_HAVE_ENOUGH_MONEY);
            return;
        }

        if (!InventoryUtil.hasItems(stock, clientInventory)) {
            event.setCancelled(NOT_ENOUGH_STOCK_IN_INVENTORY);
        }
    }
}
