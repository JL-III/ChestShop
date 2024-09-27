package com.Acrobot.ChestShop.Listeners.PreShopCreation;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Events.Economy.CurrencyCheckEvent;
import com.Acrobot.ChestShop.Events.tobesorted.PreShopCreationEvent;
import com.Acrobot.ChestShop.todo.Permission;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.units.qual.C;

import java.math.BigDecimal;

import static com.Acrobot.ChestShop.Events.tobesorted.PreShopCreationEvent.CreationOutcome.NOT_ENOUGH_MONEY;
import static com.Acrobot.ChestShop.todo.Permission.NOFEE;

/**
 * @author Acrobot
 */
public class MoneyChecker implements Listener {
    private final ChestShop plugin;

    public MoneyChecker(ChestShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPreShopCreation(PreShopCreationEvent event) {
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

        CurrencyCheckEvent currencyCheckEvent = new CurrencyCheckEvent(shopCreationPrice, player);
        plugin.getServer().getPluginManager().callEvent(currencyCheckEvent);

        if (!currencyCheckEvent.hasEnough()) {
            event.setOutcome(NOT_ENOUGH_MONEY);
        }
    }
}
