package com.Acrobot.ChestShop.Listeners.PreTransaction;

import com.Acrobot.Breeze.Utils.InventoryUtil;
import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Events.Economy.CurrencyAmountEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyCheckEvent;
import com.Acrobot.ChestShop.Events.Economy.CurrencyHoldEvent;
import com.Acrobot.ChestShop.Events.tobesorted.PreTransactionEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static com.Acrobot.ChestShop.Events.tobesorted.PreTransactionEvent.TransactionOutcome.*;
import static com.Acrobot.ChestShop.Events.tobesorted.TransactionEvent.TransactionType.BUY;
import static com.Acrobot.ChestShop.Events.tobesorted.TransactionEvent.TransactionType.SELL;

/**
 * @author Acrobot
 */
public class PartialTransactionModule implements Listener {
    private final Plugin plugin;

    public PartialTransactionModule(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPreBuyTransaction(PreTransactionEvent event) {
        if (event.isCancelled() || event.getTransactionType() != BUY) {
            return;
        }

        Player client = event.getClient();

        BigDecimal pricePerItem = event.getExactPrice().divide(BigDecimal.valueOf(InventoryUtil.countItems(event.getStock())), MathContext.DECIMAL128);

        CurrencyAmountEvent currencyAmountEvent = new CurrencyAmountEvent(client);
        plugin.getServer().getPluginManager().callEvent(currencyAmountEvent);

        BigDecimal walletMoney = currencyAmountEvent.getAmount();

        CurrencyCheckEvent currencyCheckEvent = new CurrencyCheckEvent(event.getExactPrice(), client);
        plugin.getServer().getPluginManager().callEvent(currencyCheckEvent);

        if (!currencyCheckEvent.hasEnough()) {
            int amountAffordable = getAmountOfAffordableItems(walletMoney, pricePerItem);

            if (amountAffordable < 1) {
                event.setCancelled(CLIENT_DOES_NOT_HAVE_ENOUGH_MONEY);
                return;
            }

            BigDecimal pricePerItemScaled = pricePerItem.multiply(new BigDecimal(amountAffordable)).setScale(Properties.PRICE_PRECISION, BigDecimal.ROUND_HALF_UP);
            if (pricePerItem.compareTo(BigDecimal.ZERO) > 0 && pricePerItemScaled.compareTo(BigDecimal.ZERO) == 0) {
                event.setCancelled(CLIENT_DOES_NOT_HAVE_ENOUGH_MONEY);
                return;
            }

            event.setExactPrice(pricePerItemScaled);
            event.setStock(getCountedItemStack(event.getStock(), amountAffordable));
        }

        if (!InventoryUtil.hasItems(event.getStock(), event.getOwnerInventory())) {
            ItemStack[] itemsHad = getItems(event.getStock(), event.getOwnerInventory());
            int possessedItemCount = InventoryUtil.countItems(itemsHad);

            if (possessedItemCount <= 0) {
                event.setCancelled(NOT_ENOUGH_STOCK_IN_CHEST);
                return;
            }

            BigDecimal pricePerItemScaled = pricePerItem.multiply(new BigDecimal(possessedItemCount)).setScale(Properties.PRICE_PRECISION, BigDecimal.ROUND_HALF_UP);
            if (pricePerItem.compareTo(BigDecimal.ZERO) > 0 && pricePerItemScaled.compareTo(BigDecimal.ZERO) == 0) {
                event.setCancelled(NOT_ENOUGH_STOCK_IN_CHEST);
                return;
            }

            event.setExactPrice(pricePerItemScaled);
            event.setStock(itemsHad);
        }

        if (!InventoryUtil.fits(event.getStock(), event.getClientInventory())) {
            ItemStack[] itemsFit = getItemsThatFit(event.getStock(), event.getClientInventory());
            int possessedItemCount = InventoryUtil.countItems(itemsFit);
            if (possessedItemCount <= 0) {
                event.setCancelled(NOT_ENOUGH_SPACE_IN_INVENTORY);
                return;
            }

            BigDecimal pricePerItemScaled = pricePerItem.multiply(new BigDecimal(possessedItemCount)).setScale(Properties.PRICE_PRECISION, BigDecimal.ROUND_HALF_UP);
            if (pricePerItem.compareTo(BigDecimal.ZERO) > 0 && pricePerItemScaled.compareTo(BigDecimal.ZERO) == 0) {
                event.setCancelled(NOT_ENOUGH_SPACE_IN_INVENTORY);
                return;
            }

            event.setExactPrice(pricePerItemScaled);
            event.setStock(itemsFit);
        }

        UUID seller = event.getOwnerAccount().getUuid();

        CurrencyHoldEvent currencyHoldEvent = new CurrencyHoldEvent(event.getExactPrice(), seller, client.getWorld());
        plugin.getServer().getPluginManager().callEvent(currencyHoldEvent);

        if (!currencyHoldEvent.canHold()) {
            event.setCancelled(SHOP_DEPOSIT_FAILED);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPreSellTransaction(PreTransactionEvent event) {
        if (event.isCancelled() || event.getTransactionType() != SELL) {
            return;
        }

        Player client = event.getClient();
        BigDecimal pricePerItem = event.getExactPrice().divide(BigDecimal.valueOf(InventoryUtil.countItems(event.getStock())), MathContext.DECIMAL128);

        if (!InventoryUtil.hasItems(event.getStock(), event.getClientInventory())) {
            ItemStack[] itemsHad = getItems(event.getStock(), event.getClientInventory());
            int possessedItemCount = InventoryUtil.countItems(itemsHad);

            if (possessedItemCount <= 0) {
                event.setCancelled(NOT_ENOUGH_STOCK_IN_INVENTORY);
                return;
            }

            BigDecimal pricePerItemScaled = pricePerItem.multiply(new BigDecimal(possessedItemCount)).setScale(Properties.PRICE_PRECISION, BigDecimal.ROUND_HALF_UP);
            if (pricePerItem.compareTo(BigDecimal.ZERO) > 0 && pricePerItemScaled.compareTo(BigDecimal.ZERO) == 0) {
                event.setCancelled(NOT_ENOUGH_STOCK_IN_INVENTORY);
                return;
            }

            event.setExactPrice(pricePerItemScaled);
            event.setStock(itemsHad);
        }

        if (!InventoryUtil.fits(event.getStock(), event.getOwnerInventory())) {
            ItemStack[] itemsFit = getItemsThatFit(event.getStock(), event.getOwnerInventory());
            int possessedItemCount = InventoryUtil.countItems(itemsFit);
            if (possessedItemCount <= 0) {
                event.setCancelled(NOT_ENOUGH_SPACE_IN_CHEST);
                return;
            }

            BigDecimal pricePerItemScaled = pricePerItem.multiply(new BigDecimal(possessedItemCount)).setScale(Properties.PRICE_PRECISION, BigDecimal.ROUND_HALF_UP);
            if (pricePerItem.compareTo(BigDecimal.ZERO) > 0 && pricePerItemScaled.compareTo(BigDecimal.ZERO) == 0) {
                event.setCancelled(NOT_ENOUGH_SPACE_IN_CHEST);
                return;
            }

            event.setExactPrice(pricePerItemScaled);
            event.setStock(itemsFit);
        }

        CurrencyHoldEvent currencyHoldEvent = new CurrencyHoldEvent(event.getExactPrice(), client);
        plugin.getServer().getPluginManager().callEvent(currencyHoldEvent);

        if (!currencyHoldEvent.canHold()) {
            event.setCancelled(CLIENT_DEPOSIT_FAILED);
        }
    }

    private static int getAmountOfAffordableItems(BigDecimal walletMoney, BigDecimal pricePerItem) {
        return walletMoney.divide(pricePerItem, 0, RoundingMode.FLOOR).intValueExact();
    }

    private static ItemStack[] getItems(ItemStack[] stock, Inventory inventory) {
        List<ItemStack> toReturn = new LinkedList<>();

        for (ItemStack item : InventoryUtil.mergeSimilarStacks(stock)) {
            int amount = InventoryUtil.getAmount(item, inventory);

            Collections.addAll(toReturn, getCountedItemStack(new ItemStack[]{item},
                    Math.min(amount, item.getAmount())));
        }

        return toReturn.toArray(new ItemStack[0]);
    }

    private static ItemStack[] getCountedItemStack(ItemStack[] stock, int numberOfItems) {
        int left = numberOfItems;
        LinkedList<ItemStack> stacks = new LinkedList<>();

        for (ItemStack stack : stock) {
            int count = stack.getAmount();
            ItemStack toAdd;

            if (left > count) {
                toAdd = stack;
                left -= count;
            } else {
                ItemStack clone = stack.clone();

                clone.setAmount(left);
                toAdd = clone;
                left = 0;
            }

            boolean added = false;

            int maxStackSize = InventoryUtil.getMaxStackSize(stack);

            for (ItemStack iStack : stacks) {
                if (iStack.getAmount() < maxStackSize && MaterialUtil.equals(toAdd, iStack)) {
                    int newAmount = iStack.getAmount() + toAdd.getAmount();
                    if (newAmount > maxStackSize) {
                        iStack.setAmount(maxStackSize);
                        toAdd.setAmount(newAmount - maxStackSize);
                    } else {
                        iStack.setAmount(newAmount);
                        added = true;
                    }
                    break;
                }
            }

            if (!added) {
                Collections.addAll(stacks, InventoryUtil.getItemsStacked(toAdd));
            }

            if (left <= 0) {
                break;
            }
        }

        return stacks.toArray(new ItemStack[0]);
    }

    /**
     * Make an array of items fit into an inventory.
     *
     * @param stock     The items to fit in the inventory
     * @param inventory The inventory to fit it in
     * @return Whether the items fit into the inventory
     */
    private static ItemStack[] getItemsThatFit(ItemStack[] stock, Inventory inventory) {
        List<ItemStack> resultStock = new LinkedList<>();

        int emptySlots = InventoryUtil.countEmpty(inventory);

        for (ItemStack item : InventoryUtil.mergeSimilarStacks(stock)) {
            int maxStackSize = InventoryUtil.getMaxStackSize(item);
            int free = 0;
            for (ItemStack itemInInventory : inventory.getContents()) {
                if (MaterialUtil.equals(item, itemInInventory)) {
                    if (itemInInventory != null) {
                        free += (maxStackSize - itemInInventory.getAmount()) % maxStackSize;
                    }
                }
            }

            if (free == 0 && emptySlots == 0) {
                continue;
            }

            if (item.getAmount() > free) {
                if (emptySlots > 0) {
                    int requiredSlots = (int) Math.ceil(((double) item.getAmount() - free) / maxStackSize);
                    if (requiredSlots <= emptySlots) {
                        emptySlots = emptySlots - requiredSlots;
                    } else {
                        item.setAmount(free + maxStackSize * emptySlots);
                        emptySlots = 0;
                    }
                } else {
                    item.setAmount(free);
                }
            }
            Collections.addAll(resultStock, InventoryUtil.getItemsStacked(item));
        }

        return resultStock.toArray(new ItemStack[0]);
    }
}
