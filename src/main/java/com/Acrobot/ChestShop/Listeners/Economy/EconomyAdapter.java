package com.Acrobot.ChestShop.Listeners.Economy;

import com.Acrobot.ChestShop.Events.Economy.CurrencyAddEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencySubtractEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyTransferEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;

public abstract class EconomyAdapter implements Listener {
    private final Plugin plugin;

    public EconomyAdapter(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Convenience method to process transfers by first subtracting and then adding
     *
     * @param event The CurrencyTransferEvent to process
     */
    @EventHandler
    protected void processTransfer(CurrencyTransferEvent event) {
        if (event.wasHandled()) {
            return;
        }

        BigDecimal amountSent = event.getAmountSent();
        CurrencySubtractEvent currencySubtractEvent = new CurrencySubtractEvent(amountSent, event.getSender(), event.getWorld());
        plugin.getServer().getPluginManager().callEvent(currencySubtractEvent);

        if (!currencySubtractEvent.wasHandled()) {
            return;
        }

        BigDecimal amountReceived = event.getAmountReceived().subtract(amountSent.subtract(currencySubtractEvent.getAmount()));
        CurrencyAddEvent currencyAddEvent = new CurrencyAddEvent(amountReceived, event.getReceiver(), event.getWorld());
        plugin.getServer().getPluginManager().callEvent(currencyAddEvent);

        if (currencyAddEvent.wasHandled()) {
            event.setHandled(true);
        } else {
            CurrencyAddEvent currencyResetEvent = new CurrencyAddEvent(
                    currencySubtractEvent.getAmount(),
                    event.getSender(),
                    event.getWorld()
            );
            plugin.getServer().getPluginManager().callEvent(currencyResetEvent);
        }
    }
}
