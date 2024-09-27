package com.Acrobot.ChestShop.Listeners.PreShopCreation;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Events.tobesorted.PreShopCreationEvent;
import com.Acrobot.ChestShop.Events.Protection.BuildPermissionEvent;
import com.Acrobot.ChestShop.todo.Security;
import com.Acrobot.ChestShop.Utils.uBlock;
import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import static com.Acrobot.ChestShop.Events.tobesorted.PreShopCreationEvent.CreationOutcome.NO_PERMISSION_FOR_TERRAIN;

/**
 * @author Acrobot
 */
public class TerrainChecker implements Listener {
    private final Plugin plugin;
    private final Security security;

    public TerrainChecker(Plugin plugin, Security security) {
        this.plugin = plugin;
        this.security = security;
    }

    @EventHandler
    public void onPreShopCreation(PreShopCreationEvent event) {
        Player player = event.getPlayer();

        if (!security.canPlaceSign(player, event.getSign())) {
            event.setOutcome(NO_PERMISSION_FOR_TERRAIN);
            return;
        }

        Container connectedContainer = uBlock.findConnectedContainer(event.getSign().getBlock());
        Location containerLocation = (connectedContainer != null ? connectedContainer.getLocation() : null);

        BuildPermissionEvent bEvent = new BuildPermissionEvent(player, containerLocation, event.getSign().getLocation());
        plugin.getServer().getPluginManager().callEvent(bEvent);

        if (!bEvent.isAllowed()) {
            event.setOutcome(NO_PERMISSION_FOR_TERRAIN);
        }

    }
}
