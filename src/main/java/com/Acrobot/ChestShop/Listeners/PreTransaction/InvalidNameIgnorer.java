package com.Acrobot.ChestShop.Listeners.PreTransaction;

import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Events.tobesorted.PreTransactionEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.regex.Pattern;

public class InvalidNameIgnorer implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public static void onPreTransaction(PreTransactionEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Pattern USERNAME_PATTERN = Pattern.compile(Properties.VALID_PLAYER_NAME_REGEXP);
        String name = event.getClient().getName();
        if (!USERNAME_PATTERN.matcher(name).matches()) {
            event.setCancelled(PreTransactionEvent.TransactionOutcome.INVALID_CLIENT_NAME);
        }
    }
}
