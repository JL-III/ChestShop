package com.Acrobot.ChestShop.Listeners.ShopRemoval;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Configuration.Messages;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Database.Account;
import com.Acrobot.ChestShop.Economy.Economy;
import com.Acrobot.ChestShop.Events.tobesorted.AccountQueryEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyAddEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencySubtractEvent;
import com.Acrobot.ChestShop.Events.tobesorted.ShopDestroyedEvent;
import com.Acrobot.ChestShop.todo.Permission;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.NameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.math.BigDecimal;

import static com.Acrobot.ChestShop.todo.Permission.NOFEE;
import static com.Acrobot.ChestShop.Signs.ChestShopSign.AUTOFILL_CODE;

/**
 * @author Acrobot
 */
public class ShopRefundListener implements Listener {
    private final ChestShop plugin;
    private final Economy economy;

    public ShopRefundListener(ChestShop plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onShopDestroy(ShopDestroyedEvent event) {
        BigDecimal refundPrice = Properties.SHOP_REFUND_PRICE;

        if (event.getDestroyer() == null || Permission.has(event.getDestroyer(), NOFEE) || refundPrice.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        if (ChatColor.stripColor(ChestShopSign.getItem(event.getSign())).equals(AUTOFILL_CODE)) {
            return;
        }

        AccountQueryEvent accountQueryEvent = new AccountQueryEvent(ChestShopSign.getOwner(event.getSign()));
        Bukkit.getPluginManager().callEvent(accountQueryEvent);
        Account account = accountQueryEvent.getAccount();
        if (account == null) {
            return;
        }

        CurrencyAddEvent currencyEvent = new CurrencyAddEvent(refundPrice, account.getUuid(), event.getSign().getWorld());
        plugin.getServer().getPluginManager().callEvent(currencyEvent);

        if (NameManager.getServerEconomyAccount() != null) {
            CurrencySubtractEvent currencySubtractEvent = new CurrencySubtractEvent(
                    refundPrice,
                    NameManager.getServerEconomyAccount().getUuid(),
                    event.getSign().getWorld());
            plugin.getServer().getPluginManager().callEvent(currencySubtractEvent);
        }

        Messages.SHOP_REFUNDED.sendWithPrefix(event.getDestroyer(), "amount", economy.formatBalance(refundPrice));
    }
}
