package com.Acrobot.ChestShop.Listeners.PreShopCreation;

import com.Acrobot.Breeze.Utils.PriceUtil;
import com.Acrobot.ChestShop.Events.tobesorted.ItemParseEvent;
import com.Acrobot.ChestShop.Events.tobesorted.PreShopCreationEvent;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.NameManager;
import com.Acrobot.ChestShop.Utils.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

import static com.Acrobot.ChestShop.Events.tobesorted.PreShopCreationEvent.CreationOutcome.NO_PERMISSION;
import static com.Acrobot.ChestShop.Signs.ChestShopSign.NAME_LINE;
import static com.Acrobot.ChestShop.Utils.Permission.*;
import static org.bukkit.event.EventPriority.HIGH;

/**
 * @author Acrobot
 */
public class PermissionChecker implements Listener {
    private final NameManager nameManager;

    public PermissionChecker(NameManager nameManager) {
        this.nameManager = nameManager;
    }

    @EventHandler(priority = HIGH)
    public void onPreShopCreation(PreShopCreationEvent event) {
        Player player = event.getPlayer();

        if (event.getOwnerAccount() != null
                && !nameManager.canUseName(player, OTHER_NAME_CREATE, event.getOwnerAccount().getShortName())) {
            event.setSignLine(NAME_LINE, "");
            event.setOutcome(NO_PERMISSION);
            return;
        }

        String priceLine = ChestShopSign.getPrice(event.getSignLines());
        String itemLine = ChestShopSign.getItem(event.getSignLines());

        ItemParseEvent parseEvent = new ItemParseEvent(itemLine);
        Bukkit.getPluginManager().callEvent(parseEvent);
        ItemStack item = parseEvent.getItem();

        if (item == null) {
            if ((PriceUtil.hasBuyPrice(priceLine) && !Permission.has(player, SHOP_CREATION_BUY))
                    || (PriceUtil.hasSellPrice(priceLine) && !Permission.has(player, SHOP_CREATION_SELL))) {
                event.setOutcome(NO_PERMISSION);
            }
            return;
        }

        String matID = item.getType().toString().toLowerCase(Locale.ROOT);

        String[] parts = itemLine.split("#", 2);
        if (parts.length == 2 && Permission.hasPermissionSetFalse(player, SHOP_CREATION_ID + matID + "#" + parts[1])) {
            event.setOutcome(NO_PERMISSION);
            return;
        }

        if (PriceUtil.hasBuyPrice(priceLine)) {
            if (Permission.has(player, SHOP_CREATION_BUY_ID + matID)) {
                return;
            }
            if (Permission.has(player, SHOP_CREATION) || (Permission.has(player, SHOP_CREATION_ID + matID) && Permission.has(player, SHOP_CREATION_BUY))) {
                return;
            }
            event.setOutcome(NO_PERMISSION);
            return;
        }

        if (PriceUtil.hasSellPrice(priceLine)) {
            if (Permission.has(player, SHOP_CREATION_SELL_ID + matID)) {
                return;
            }
            if (Permission.has(player, SHOP_CREATION) || (Permission.has(player, SHOP_CREATION_ID + matID) && Permission.has(player, SHOP_CREATION_SELL))) {
                return;
            }
            event.setOutcome(NO_PERMISSION);
        }
    }
}
