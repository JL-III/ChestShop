package com.Acrobot.ChestShop.Economy;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Events.Economy.CurrencyAddEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyCheckEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyFormatEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencySubtractEvent;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.NameManager;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
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
     * Get the name of the server economy account
     * @return The username of te server economy account
     * @deprecated Use {@link NameManager#getServerEconomyAccount()} or {@link Properties#SERVER_ECONOMY_ACCOUNT}
     */
    @Deprecated
    public static String getServerAccountName() {
        return Properties.SERVER_ECONOMY_ACCOUNT;
    }

    public static boolean isOwnerEconomicallyActive(Inventory inventory) {
        return !ChestShopSign.isAdminShop(inventory) || NameManager.getServerEconomyAccount() != null;
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
