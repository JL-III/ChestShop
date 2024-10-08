package com.Acrobot.ChestShop.Listeners.PreShopCreation;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Database.Account;
import com.Acrobot.ChestShop.Events.tobesorted.AccountQueryEvent;
import com.Acrobot.ChestShop.Events.tobesorted.PreShopCreationEvent;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.NameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.logging.Level;

import static com.Acrobot.ChestShop.Utils.Permission.OTHER_NAME_CREATE;
import static com.Acrobot.ChestShop.Signs.ChestShopSign.NAME_LINE;
import static com.Acrobot.ChestShop.Events.tobesorted.PreShopCreationEvent.CreationOutcome.UNKNOWN_PLAYER;

/**
 * @author Acrobot
 */
public class NameChecker implements Listener {
    private final ChestShop plugin;
    private final NameManager nameManager;

    public NameChecker(ChestShop plugin, NameManager nameManager) {
        this.plugin = plugin;
        this.nameManager = nameManager;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPreShopCreation(PreShopCreationEvent event) {
        handleEvent(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreShopCreationHighest(PreShopCreationEvent event) {
        handleEvent(event);
    }

    private void handleEvent(PreShopCreationEvent event) {
        String name = ChestShopSign.getOwner(event.getSignLines());
        Player player = event.getPlayer();

        Account account = event.getOwnerAccount();
        if (account == null || !account.getShortName().equalsIgnoreCase(name)) {
            account = null;
            try {
                if (name.isEmpty() || !nameManager.canUseName(player, OTHER_NAME_CREATE, name)) {
                    account = NameManager.getOrCreateAccount(player);
                } else {
                    AccountQueryEvent accountQueryEvent = new AccountQueryEvent(name);
                    plugin.getServer().getPluginManager().callEvent(accountQueryEvent);
                    account = accountQueryEvent.getAccount();
                    if (account == null) {
                        Player otherPlayer = plugin.getServer().getPlayer(name);
                        try {
                            if (otherPlayer != null) {
                                account = NameManager.getOrCreateAccount(otherPlayer);
                            } else {
                                account = NameManager.getOrCreateAccount(plugin.getServer().getOfflinePlayer(name));
                            }
                        } catch (IllegalArgumentException e) {
                            event.getPlayer().sendMessage(e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                ChestShop.getBukkitLogger().log(Level.SEVERE, "Error while trying to check account for name " + name + " with player " + player.getName(), e);
            }
        }
        event.setOwnerAccount(account);
        if (account != null) {
            event.setSignLine(NAME_LINE, account.getShortName());
        } else {
            event.setSignLine(NAME_LINE, "");
            event.setOutcome(UNKNOWN_PLAYER);
        }
    }
}
