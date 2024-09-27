package com.Acrobot.ChestShop.Listeners.Player;

import com.Acrobot.Breeze.Utils.BlockUtil;
import com.Acrobot.Breeze.Utils.InventoryUtil;
import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.PriceUtil;
import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Commands.AccessToggle;
import com.Acrobot.ChestShop.Configuration.Messages;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Database.Account;
import com.Acrobot.ChestShop.Events.Economy.AccountCheckEvent;
import com.Acrobot.ChestShop.Events.tobesorted.*;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.logging.Level;

import static com.Acrobot.Breeze.Utils.BlockUtil.isSign;
import static com.Acrobot.Breeze.Utils.ImplementationAdapter.getState;
import static com.Acrobot.ChestShop.Events.tobesorted.TransactionEvent.TransactionType;
import static com.Acrobot.ChestShop.Events.tobesorted.TransactionEvent.TransactionType.BUY;
import static com.Acrobot.ChestShop.Events.tobesorted.TransactionEvent.TransactionType.SELL;
import static com.Acrobot.ChestShop.Signs.ChestShopSign.AUTOFILL_CODE;
import static com.Acrobot.ChestShop.Signs.ChestShopSign.ITEM_LINE;
import static com.Acrobot.ChestShop.Utils.Permission.OTHER_NAME_CREATE;
import static org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

/**
 * @author Acrobot
 */
public class PlayerInteract implements Listener {
    private final ChestShop plugin;
    private final ItemUtil itemUtil;
    private final ChestShopSign chestShopSign;
    private final Security security;
    private final NameManager nameManager;

    public PlayerInteract(ChestShop plugin, ItemUtil itemUtil, ChestShopSign chestShopSign, Security security, NameManager nameManager) {
        this.plugin = plugin;
        this.itemUtil = itemUtil;
        this.chestShopSign = chestShopSign;
        this.security = security;
        this.nameManager = nameManager;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null)
            return;

        Action action = event.getAction();
        Player player = event.getPlayer();

        if (Properties.USE_BUILT_IN_PROTECTION && uBlock.couldBeShopContainer(block)) {
            Sign sign = uBlock.getConnectedSign(block);
            if (sign != null) {

                if (!security.canView(player, block, false)) {
                    if (Permission.has(player, Permission.SHOPINFO)) {
                        plugin.getServer().getPluginManager().callEvent(new ShopInfoEvent(player, sign));
                        event.setCancelled(true);
                    } else {
                        Messages.ACCESS_DENIED.send(player);
                        event.setCancelled(true);
                    }
                }
                return;
            }
        }

        if (!isSign(block))
            return;

        Sign sign = (Sign) getState(block, false);
        if (!ChestShopSign.isValid(sign)) {
            return;
        }

        if (Properties.ALLOW_AUTO_ITEM_FILL && ChatColor.stripColor(ChestShopSign.getItem(sign)).equals(AUTOFILL_CODE)) {
            if (chestShopSign.hasPermission(player, OTHER_NAME_CREATE, sign, nameManager)) {
                ItemStack item = player.getInventory().getItemInMainHand();
                if (!MaterialUtil.isEmpty(item)) {
                    event.setCancelled(true);
                    String itemCode;
                    try {
                        itemCode = itemUtil.getSignName(item);
                    } catch (IllegalArgumentException e) {
                        player.sendMessage(ChatColor.RED + "Error while generating shop sign item name. Please contact an admin or take a look at the console/log!");
                        plugin.getLogger().log(Level.SEVERE, "Error while generating shop sign item name", e);
                        return;
                    }
                    String[] lines = sign.getLines();
                    lines[ITEM_LINE] = itemCode;

                    SignChangeEvent changeEvent = new SignChangeEvent(block, player, lines);
                    plugin.getServer().getPluginManager().callEvent(changeEvent);
                    if (!changeEvent.isCancelled()) {
                        for (byte i = 0; i < changeEvent.getLines().length; ++i) {
                            String line = changeEvent.getLine(i);
                            sign.setLine(i, line != null ? line : "");
                        }
                        sign.update();
                    }
                } else {
                    Messages.NO_ITEM_IN_HAND.sendWithPrefix(player);
                }
            } else {
                Messages.ACCESS_DENIED.sendWithPrefix(player);
            }
            return;
        }

        if (!AccessToggle.isIgnoring(player) && chestShopSign.canAccess(player, sign, nameManager)) {
            if (Properties.IGNORE_ACCESS_PERMS || ChestShopSign.isOwner(player, sign)) {
                if (player.getInventory().getItemInMainHand().getType().name().contains("SIGN") && action == RIGHT_CLICK_BLOCK) {
                    // Allow editing of sign (if supported)
                    return;
                } else if ((player.getInventory().getItemInMainHand().getType().name().endsWith("DYE")
                        || player.getInventory().getItemInMainHand().getType().name().endsWith("INK_SAC"))
                        && action == RIGHT_CLICK_BLOCK) {
                    if (Properties.SIGN_DYING) {
                        return;
                    } else {
                        event.setCancelled(true);
                    }
                }
                if (Properties.ALLOW_SIGN_CHEST_OPEN && !(Properties.IGNORE_CREATIVE_MODE && player.getGameMode() == GameMode.CREATIVE)) {
                    if (player.isSneaking() || player.isInsideVehicle()
                            || (Properties.ALLOW_LEFT_CLICK_DESTROYING && action == LEFT_CLICK_BLOCK)) {
                        return;
                    }
                    event.setCancelled(true);
                    showChestGUI(player, block, sign);
                    return;
                }
                // don't allow owners or people with access to buy/sell at this shop
                Messages.TRADE_DENIED_ACCESS_PERMS.sendWithPrefix(player);
                if (action == RIGHT_CLICK_BLOCK) {
                    // don't allow editing
                    event.setCancelled(true);
                }
                return;
            }
        }

        if (action == RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
        } else if (action == LEFT_CLICK_BLOCK && !Properties.TURN_OFF_SIGN_PROTECTION && !chestShopSign.canAccess(player, sign, nameManager)) {
            event.setCancelled(true);
        }

        if (!security.canAccess(player, block, true)) {
            Messages.TRADE_DENIED.sendWithPrefix(player);
            return;
        }
        PreTransactionEvent pEvent = preparePreTransactionEvent(sign, player, action);
        if (pEvent == null)
            return;

        Bukkit.getPluginManager().callEvent(pEvent);
        if (pEvent.isCancelled())
            return;

        TransactionEvent tEvent = new TransactionEvent(pEvent, sign);
        Bukkit.getPluginManager().callEvent(tEvent);
    }

    private static PreTransactionEvent preparePreTransactionEvent(Sign sign, Player player, Action action) {
        String name = ChestShopSign.getOwner(sign);
        String prices = ChestShopSign.getPrice(sign);
        String material = ChestShopSign.getItem(sign);

        AccountQueryEvent accountQueryEvent = new AccountQueryEvent(name);
        Bukkit.getPluginManager().callEvent(accountQueryEvent);
        Account account = accountQueryEvent.getAccount();
        if (account == null) {
            Messages.PLAYER_NOT_FOUND.sendWithPrefix(player);
            return null;
        }

        AccountCheckEvent event = new AccountCheckEvent(account.getUuid(), player.getWorld());
        Bukkit.getPluginManager().callEvent(event);
        if(!event.hasAccount()) {
            Messages.NO_ECONOMY_ACCOUNT.sendWithPrefix(player);
            return null;
        }
        BigDecimal price = (action == RIGHT_CLICK_BLOCK ? PriceUtil.getExactBuyPrice(prices) : PriceUtil.getExactSellPrice(prices));

        Container shopBlock = uBlock.findConnectedContainer(sign);
        Inventory ownerInventory = shopBlock != null ? shopBlock.getInventory() : null;

        ItemParseEvent parseEvent = new ItemParseEvent(material);
        Bukkit.getPluginManager().callEvent(parseEvent);
        ItemStack item = parseEvent.getItem();
        if (item == null) {
            Messages.INVALID_SHOP_DETECTED.sendWithPrefix(player);
            return null;
        }

        int amount = -1;
        try {
            amount = ChestShopSign.getQuantity(sign);
        } catch (NumberFormatException ignored) {} // There is no quantity number on the sign

        if (amount < 1 || amount > Properties.MAX_SHOP_AMOUNT) {
            Messages.INVALID_SHOP_PRICE.sendWithPrefix(player);
            return null;
        }

        item.setAmount(amount);

        ItemStack[] items = InventoryUtil.getItemsStacked(item);

        TransactionType transactionType = (action == RIGHT_CLICK_BLOCK ? BUY : SELL);
        return new PreTransactionEvent(ownerInventory, player.getInventory(), items, price, player, account, sign, transactionType);
    }

    private void showChestGUI(Player player, Block signBlock, Sign sign) {
        Container container = uBlock.findConnectedContainer(sign);
        if (container == null) {
            Messages.NO_CHEST_DETECTED.sendWithPrefix(player);
            return;
        }
        if (!security.canAccess(player, signBlock)) {
            return;
        }
        if (!security.canAccess(player, container.getBlock())) {
            return;
        }
        BlockUtil.openBlockGUI(container, player);
    }
}
