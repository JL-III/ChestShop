package com.Acrobot.ChestShop.Listeners.Block;

import com.Acrobot.ChestShop.Configuration.Messages;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.NameManager;
import com.Acrobot.ChestShop.Utils.uBlock;
import com.Acrobot.ChestShop.todo.Permission;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 * @author Acrobot
 */
public class ChestBreak implements Listener {
    private final ChestShopSign chestShopSign;
    private final NameManager nameManager;

    public ChestBreak(ChestShopSign chestShopSign, NameManager nameManager) {
        this.chestShopSign = chestShopSign;
        this.nameManager = nameManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onChestBreak(BlockBreakEvent event) {
        if (!canBeBroken(event.getBlock(), event.getPlayer())) {
            event.setCancelled(true);
            Messages.ACCESS_DENIED.sendWithPrefix(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onExplosion(EntityExplodeEvent event) {
        if (event.blockList() == null || !Properties.USE_BUILT_IN_PROTECTION) {
            return;
        }

        for (Block block : event.blockList()) {
            if (!canBeBroken(block, null)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!canBeBroken(event.getBlock(), null)) {
            event.setCancelled(true);
        }
    }

    private boolean canBeBroken(Block block, Player breaker) {
        if (!uBlock.couldBeShopContainer(block) || !Properties.USE_BUILT_IN_PROTECTION) {
            return true;
        }

        Sign shopSign = uBlock.getConnectedSign(block);
        if (breaker != null) {
            return  chestShopSign.hasPermission(breaker, Permission.OTHER_NAME_DESTROY, shopSign, nameManager);
        }
        return shopSign == null;
    }
}
