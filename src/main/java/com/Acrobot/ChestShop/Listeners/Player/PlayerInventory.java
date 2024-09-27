package com.Acrobot.ChestShop.Listeners.Player;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Configuration.Messages;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Events.tobesorted.ShopInfoEvent;
import com.Acrobot.ChestShop.todo.Permission;
import com.Acrobot.ChestShop.todo.Security;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.uBlock;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

import static com.Acrobot.Breeze.Utils.ImplementationAdapter.getHolder;

/**
 * @author Acrobot
 */
public class PlayerInventory implements Listener {
    private final ChestShop plugin;
    private final Security security;

    public PlayerInventory(ChestShop plugin, Security security) {
        this.plugin = plugin;
        this.security = security;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!Properties.TURN_OFF_DEFAULT_PROTECTION_WHEN_PROTECTED_EXTERNALLY) {
            return;
        }

        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        InventoryHolder holder = getHolder(event.getInventory(), false);
        if (!(holder instanceof BlockState) && !(holder instanceof DoubleChest)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        List<Block> containers = new ArrayList<>();

        if (holder instanceof DoubleChest) {
            InventoryHolder leftSide = ((DoubleChest) holder).getLeftSide();
            if (leftSide instanceof BlockState) {
                containers.add(((BlockState) leftSide).getBlock());
            }
            InventoryHolder rightSide = ((DoubleChest) holder).getRightSide();
            if (rightSide instanceof BlockState) {
                containers.add(((BlockState) rightSide).getBlock());
            }
        } else {
            containers.add(((BlockState) holder).getBlock());
        }

        boolean canAccess = false;
        for (Block container : containers) {
            if (ChestShopSign.isShopBlock(container)) {
                if (security.canView(player, container, false)) {
                    canAccess = true;
                }
            } else {
                canAccess = true;
            }
        }

        if (!canAccess) {
            if (Permission.has(player, Permission.SHOPINFO)) {
                for (Block container : containers) {
                    Sign sign = uBlock.getConnectedSign(container);
                    if (sign != null) {
                        plugin.getServer().getPluginManager().callEvent(new ShopInfoEvent((Player) event.getPlayer(), sign));
                    }
                }
            } else {
                Messages.ACCESS_DENIED.sendWithPrefix(event.getPlayer());
            }
            event.setCancelled(true);
        }
    }
}
