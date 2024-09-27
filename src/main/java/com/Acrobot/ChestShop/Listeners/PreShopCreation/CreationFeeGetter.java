package com.Acrobot.ChestShop.Listeners.PreShopCreation;

import com.Acrobot.ChestShop.Configuration.Messages;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Economy.Economy;
import com.Acrobot.ChestShop.Events.Economy.CurrencyAddEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencySubtractEvent;
import com.Acrobot.ChestShop.Events.tobesorted.PreShopCreationEvent;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.NameManager;
import com.Acrobot.ChestShop.todo.Permission;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;

import static com.Acrobot.ChestShop.todo.Permission.NOFEE;

/**
 * @author Acrobot
 */
public class CreationFeeGetter implements Listener {
    private final Plugin plugin;
    private final Economy economy;

    public CreationFeeGetter(Plugin plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShopCreation(PreShopCreationEvent event) {
        BigDecimal shopCreationPrice = Properties.SHOP_CREATION_PRICE;

        if (shopCreationPrice.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        if (ChestShopSign.isAdminShop(event.getSignLines())) {
            return;
        }

        Player player = event.getPlayer();

        if (Permission.has(player, NOFEE)) {
            return;
        }

        CurrencySubtractEvent subtractionEvent = new CurrencySubtractEvent(shopCreationPrice, player);
        plugin.getServer().getPluginManager().callEvent(subtractionEvent);

        if (!subtractionEvent.wasHandled()) {
            event.setOutcome(PreShopCreationEvent.CreationOutcome.NOT_ENOUGH_MONEY);
            event.setSignLines(new String[4]);
            return;
        }

        if (NameManager.getServerEconomyAccount() != null) {
            CurrencyAddEvent currencyAddEvent = new CurrencyAddEvent(
                    shopCreationPrice,
                    NameManager.getServerEconomyAccount().getUuid(),
                    player.getWorld());
            plugin.getServer().getPluginManager().callEvent(currencyAddEvent);
        }

        Messages.SHOP_FEE_PAID.sendWithPrefix(player, "amount", economy.formatBalance(shopCreationPrice));
    }
}
