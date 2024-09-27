package com.Acrobot.ChestShop.Listeners.Economy;

import com.Acrobot.ChestShop.Events.Economy.CurrencyAddEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencySubtractEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyTransferEvent;
import com.Acrobot.ChestShop.Utils.NameManager;
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
    protected void processTransfer(CurrencyTransferEvent event) {
        if (event.wasHandled()) {
            return;
        }

        BigDecimal amountSent = event.getAmountSent();
        CurrencySubtractEvent currencySubtractEvent = new CurrencySubtractEvent(amountSent, event.getSender(), event.getWorld());
        if (!NameManager.isAdminShop(event.getSender())) {
            plugin.getServer().getPluginManager().callEvent(currencySubtractEvent);
        } else {
            currencySubtractEvent.setHandled(true);
        }

        if (!currencySubtractEvent.wasHandled()) {
            return;
        }

        BigDecimal amountReceived = event.getAmountReceived().subtract(amountSent.subtract(currencySubtractEvent.getAmount()));
        CurrencyAddEvent currencyAddEvent = new CurrencyAddEvent(amountReceived, event.getReceiver(), event.getWorld());
        if (!NameManager.isAdminShop(event.getReceiver())) {
            plugin.getServer().getPluginManager().callEvent(currencyAddEvent);
        } else {
            currencyAddEvent.setHandled(true);
        }

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

    public static class ProviderInfo {
        private final String name;
        private final String version;

        public ProviderInfo(String name, String version) {
            this.name = name;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }
    }
}
