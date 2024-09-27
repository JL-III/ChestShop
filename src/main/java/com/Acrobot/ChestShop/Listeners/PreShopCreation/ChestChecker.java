package com.Acrobot.ChestShop.Listeners.PreShopCreation;

import com.Acrobot.ChestShop.Events.tobesorted.PreShopCreationEvent;
import com.Acrobot.ChestShop.Utils.Permission;
import com.Acrobot.ChestShop.Utils.Security;
import com.Acrobot.ChestShop.Utils.uBlock;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import static com.Acrobot.ChestShop.Events.tobesorted.PreShopCreationEvent.CreationOutcome.NO_CHEST;
import static com.Acrobot.ChestShop.Events.tobesorted.PreShopCreationEvent.CreationOutcome.NO_PERMISSION_FOR_CHEST;
import static com.Acrobot.ChestShop.Utils.Permission.ADMIN;

/**
 * @author Acrobot
 */
public class ChestChecker implements Listener {
    private final Security security;

    public ChestChecker(Security security) {
        this.security = security;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPreShopCreation(PreShopCreationEvent event) {

        Container connectedContainer = uBlock.findConnectedContainer(event.getSign().getBlock());
        if (connectedContainer == null) {
            event.setOutcome(NO_CHEST);
            return;
        }

        Player player = event.getPlayer();

        if (Permission.has(player, ADMIN)) {
            return;
        }

        if (!security.canAccess(player, connectedContainer.getBlock())) {
            event.setOutcome(NO_PERMISSION_FOR_CHEST);
        }
    }
}
