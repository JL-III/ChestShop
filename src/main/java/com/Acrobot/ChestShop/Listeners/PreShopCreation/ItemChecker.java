package com.Acrobot.ChestShop.Listeners.PreShopCreation;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.StringUtil;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Events.tobesorted.ItemParseEvent;
import com.Acrobot.ChestShop.Events.tobesorted.PreShopCreationEvent;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.ItemUtil;
import com.Acrobot.ChestShop.Utils.uBlock;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import static com.Acrobot.Breeze.Utils.MaterialUtil.MAXIMUM_SIGN_WIDTH;
import static com.Acrobot.ChestShop.Events.tobesorted.PreShopCreationEvent.CreationOutcome.INVALID_ITEM;
import static com.Acrobot.ChestShop.Events.tobesorted.PreShopCreationEvent.CreationOutcome.ITEM_AUTOFILL;
import static com.Acrobot.ChestShop.Signs.ChestShopSign.AUTOFILL_CODE;
import static com.Acrobot.ChestShop.Signs.ChestShopSign.ITEM_LINE;

/**
 * @author Acrobot
 */
public class ItemChecker implements Listener {
    private final ItemUtil itemUtil;

    public ItemChecker(ItemUtil itemUtil) {
        this.itemUtil = itemUtil;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreShopCreation(PreShopCreationEvent event) {
        String itemCode = ChestShopSign.getItem(event.getSignLines());

        ItemParseEvent parseEvent = new ItemParseEvent(itemCode);
        Bukkit.getPluginManager().callEvent(parseEvent);
        ItemStack item = parseEvent.getItem();

        if (item == null) {
            if (Properties.ALLOW_AUTO_ITEM_FILL && itemCode.equals(AUTOFILL_CODE)) {
                Container container = uBlock.findConnectedContainer(event.getSign());
                if (container != null) {
                    for (ItemStack stack : container.getInventory().getContents()) {
                        if (!MaterialUtil.isEmpty(stack)) {
                            item = stack;
                            break;
                        }
                    }
                }

                if (item == null) {
                    event.setSignLine(ITEM_LINE, ChatColor.BOLD + ChestShopSign.AUTOFILL_CODE);
                    event.setOutcome(ITEM_AUTOFILL);
                    return;
                }
            } else {
                event.setOutcome(INVALID_ITEM);
                return;
            }
        }

        itemCode = itemUtil.getSignName(item);

        if (StringUtil.getMinecraftStringWidth(itemCode) > MAXIMUM_SIGN_WIDTH) {
            event.setOutcome(INVALID_ITEM);
            return;
        }

        event.setSignLine(ITEM_LINE, itemCode);
    }
}
