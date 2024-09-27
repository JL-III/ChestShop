package com.Acrobot.ChestShop.Listeners.PostTransaction;

import com.Acrobot.ChestShop.Events.Economy.CurrencyTransferEvent;
import com.Acrobot.ChestShop.Events.tobesorted.TransactionEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import static com.Acrobot.ChestShop.Events.tobesorted.TransactionEvent.TransactionType.BUY;

/**
 * @author Acrobot
 */
public class EconomicModule implements Listener {
    private final Plugin plugin;

    public EconomicModule(Plugin plugin) {
        this.plugin = plugin;
    }


    @EventHandler(ignoreCancelled = true)
    public void onBuyTransaction(TransactionEvent event) {
        CurrencyTransferEvent currencyTransferEvent = new CurrencyTransferEvent(
                event.getExactPrice(),
                event.getClient(),
                event.getOwnerAccount().getUuid(),
                event.getTransactionType() == BUY ? CurrencyTransferEvent.Direction.PARTNER : CurrencyTransferEvent.Direction.INITIATOR,
                event
        );
        plugin.getServer().getPluginManager().callEvent(currencyTransferEvent);
        if (!currencyTransferEvent.wasHandled()) {
            event.setCancelled(true);
        }
    }
}
