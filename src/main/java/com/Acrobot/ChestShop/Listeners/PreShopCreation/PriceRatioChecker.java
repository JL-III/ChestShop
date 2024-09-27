package com.Acrobot.ChestShop.Listeners.PreShopCreation;

import com.Acrobot.Breeze.Utils.PriceUtil;
import com.Acrobot.ChestShop.Events.tobesorted.PreShopCreationEvent;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.math.BigDecimal;

import static com.Acrobot.Breeze.Utils.PriceUtil.hasBuyPrice;
import static com.Acrobot.Breeze.Utils.PriceUtil.hasSellPrice;
import static com.Acrobot.ChestShop.Events.tobesorted.PreShopCreationEvent.CreationOutcome.SELL_PRICE_HIGHER_THAN_BUY_PRICE;
import static org.bukkit.event.EventPriority.HIGH;

/**
 * @author Acrobot
 */
public class PriceRatioChecker implements Listener {

    @EventHandler(priority = HIGH)
    public void onPreShopCreation(PreShopCreationEvent event) {
        String priceLine = ChestShopSign.getPrice(event.getSignLines());

        if (!hasBuyPrice(priceLine) || !hasSellPrice(priceLine)) {
            return;
        }
        BigDecimal buyPrice = PriceUtil.getExactBuyPrice(priceLine);
        BigDecimal sellPrice = PriceUtil.getExactSellPrice(priceLine);
        if (sellPrice.compareTo(buyPrice) > 0) {
            event.setOutcome(SELL_PRICE_HIGHER_THAN_BUY_PRICE);
        }
    }
}
