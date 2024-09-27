package com.Acrobot.ChestShop.Economy;

import com.Acrobot.ChestShop.Events.Economy.CurrencyAddEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyCheckEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyFormatEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencySubtractEvent;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @author Acrobot
 *         Economy management
 */
public class Economy {
    private final Plugin plugin;

    public Economy(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * @deprecated Directly call the {@link CurrencyAddEvent}
     */
    @Deprecated
    public boolean add(UUID name, World world, double amount) {
        CurrencyAddEvent event = new CurrencyAddEvent(BigDecimal.valueOf(amount), name, world);
        plugin.getServer().getPluginManager().callEvent(event);

        return event.wasHandled();
    }

    /**
     * @deprecated Directly call the {@link CurrencySubtractEvent}
     */
    @Deprecated
    public boolean subtract(UUID name, World world, double amount) {
        CurrencySubtractEvent event = new CurrencySubtractEvent(BigDecimal.valueOf(amount), name, world);
        plugin.getServer().getPluginManager().callEvent(event);

        return event.wasHandled();
    }

    /**
     * @deprecated Directly call the {@link CurrencyCheckEvent}
     */
    @Deprecated
    public boolean hasEnough(UUID name, World world, double amount) {
        CurrencyCheckEvent event = new CurrencyCheckEvent(BigDecimal.valueOf(amount), name, world);
        plugin.getServer().getPluginManager().callEvent(event);

        return event.hasEnough();
    }

    public String formatBalance(BigDecimal amount) {
        CurrencyFormatEvent event = new CurrencyFormatEvent(amount);
        plugin.getServer().getPluginManager().callEvent(event);

        return event.getFormattedAmount();
    }

    /**
     * @deprecated Use {@link #formatBalance(BigDecimal)}
     */
    @Deprecated
    public String formatBalance(double amount) {
        return formatBalance(BigDecimal.valueOf(amount));
    }
}
