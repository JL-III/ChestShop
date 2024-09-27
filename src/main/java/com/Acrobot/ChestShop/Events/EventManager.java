package com.Acrobot.ChestShop.Events;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Economy.Economy;
import com.Acrobot.ChestShop.Listeners.Block.BlockPlace;
import com.Acrobot.ChestShop.Listeners.Block.ChestBreak;
import com.Acrobot.ChestShop.Listeners.Block.SignBreak;
import com.Acrobot.ChestShop.Listeners.Block.SignCreate;
import com.Acrobot.ChestShop.Listeners.Economy.ServerAccountCorrector;
import com.Acrobot.ChestShop.Listeners.Economy.TaxModule;
import com.Acrobot.ChestShop.Listeners.GarbageTextListener;
import com.Acrobot.ChestShop.Listeners.Item.ItemMoveListener;
import com.Acrobot.ChestShop.Listeners.Item.ItemStringListener;
import com.Acrobot.ChestShop.Listeners.ItemInfoListener;
import com.Acrobot.ChestShop.Listeners.Modules.*;
import com.Acrobot.ChestShop.Listeners.Player.*;
import com.Acrobot.ChestShop.Listeners.PostShopCreation.MessageSender;
import com.Acrobot.ChestShop.Listeners.PostShopCreation.ShopCreationLogger;
import com.Acrobot.ChestShop.Listeners.PostShopCreation.SignSticker;
import com.Acrobot.ChestShop.Listeners.PostTransaction.*;
import com.Acrobot.ChestShop.Listeners.PreShopCreation.*;
import com.Acrobot.ChestShop.Listeners.PreTransaction.*;
import com.Acrobot.ChestShop.Listeners.ShopInfoListener;
import com.Acrobot.ChestShop.Listeners.ShopRemoval.ShopRefundListener;
import com.Acrobot.ChestShop.Listeners.ShopRemoval.ShopRemovalLogger;
import com.Acrobot.ChestShop.Listeners.SignParseListener;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Signs.RestrictedSign;
import com.Acrobot.ChestShop.Utils.NameManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public class EventManager {
    private final PluginManager pluginManager;
    private final ChestShop plugin;
    private final ChestShopSign chestShopSign;
    private final NameManager nameManager;
    private final Economy economy;

    public EventManager(PluginManager pluginManager, ChestShop plugin, ChestShopSign chestShopSign, NameManager nameManager) {
        this.pluginManager = pluginManager;
        this.plugin = plugin;
        this.chestShopSign = chestShopSign;
        this.nameManager = nameManager;
        economy = new Economy(plugin);
        registerEvents();
    }

    private void registerEvents() {
        registerEvent(new NameManager(plugin));

        registerPreShopCreationEvents();
        registerPreTransactionEvents();
        registerPostShopCreationEvents();
        registerPostTransactionEvents();
        registerShopRemovalEvents();

        registerModules();
        SignBreak signBreak = new SignBreak(plugin, this);
        registerEvent(signBreak);
        registerEvent(new SignCreate(plugin, nameManager, signBreak));
        registerEvent(new ChestBreak(chestShopSign, nameManager));

        registerEvent(new BlockPlace(plugin.getSecurity()));
        registerEvent(new PlayerConnect());
        registerEvent(new PlayerInteract(plugin, plugin.getItemUtil(), chestShopSign, plugin.getSecurity(), nameManager));
        registerEvent(new PlayerInventory(plugin, plugin.getSecurity()));
        registerEvent(new PlayerLeave());
        registerEvent(new PlayerTeleport());

        registerEvent(new SignParseListener());
        registerEvent(new ItemStringListener());
        registerEvent(new ItemInfoListener(plugin, this, plugin.getItemInfo()));
        registerEvent(new ShopInfoListener(plugin, plugin.getItemUtil()));
        registerEvent(new GarbageTextListener());

        registerEvent(new RestrictedSign(chestShopSign, nameManager));

        if (!Properties.TURN_OFF_HOPPER_PROTECTION) {
            registerEvent(new ItemMoveListener());
        }
    }

    private void registerShopRemovalEvents() {
        registerEvent(new ShopRefundListener(plugin, economy));
        registerEvent(new ShopRemovalLogger());
    }

    private void registerPreShopCreationEvents() {
        if (Properties.BLOCK_SHOPS_WITH_SELL_PRICE_HIGHER_THAN_BUY_PRICE) {
            registerEvent(new PriceRatioChecker());
        }

        registerEvent(new ChestChecker(plugin.getSecurity()));
        registerEvent(new ItemChecker(plugin.getItemUtil()));
        registerEvent(new MoneyChecker(plugin));
        registerEvent(new NameChecker(plugin, nameManager));
        registerEvent(new com.Acrobot.ChestShop.Listeners.PreShopCreation.PermissionChecker(nameManager));
        registerEvent(new com.Acrobot.ChestShop.Listeners.PreShopCreation.ErrorMessageSender());
        registerEvent(new PriceChecker());
        registerEvent(new QuantityChecker());
        registerEvent(new TerrainChecker(plugin, plugin.getSecurity()));
    }

    private void registerPostShopCreationEvents() {
        registerEvent(new CreationFeeGetter(plugin, economy));
        registerEvent(new MessageSender());
        registerEvent(new SignSticker());
        registerEvent(new ShopCreationLogger());
    }

    private void registerPreTransactionEvents() {
        if (Properties.ALLOW_PARTIAL_TRANSACTIONS) {
            registerEvent(new PartialTransactionModule(plugin));
        } else {
            registerEvent(new AmountAndPriceChecker(plugin));
        }

        registerEvent(new InvalidNameIgnorer());
        registerEvent(new CreativeModeIgnorer());
        registerEvent(new com.Acrobot.ChestShop.Listeners.PreTransaction.ErrorMessageSender(plugin.getItemUtil(), economy));
        registerEvent(new com.Acrobot.ChestShop.Listeners.PreTransaction.PermissionChecker());
        registerEvent(new PriceValidator());
        registerEvent(new ShopValidator());
        registerEvent(new SpamClickProtector());
        registerEvent(new StockFittingChecker());
    }

    private void registerPostTransactionEvents() {
        registerEvent(new EconomicModule(plugin));
        registerEvent(new EmptyShopDeleter(plugin));
        registerEvent(new ItemManager());
        registerEvent(new TransactionLogger(plugin.getItemUtil()));
        registerEvent(new TransactionMessageSender(plugin.getItemUtil(), economy));
    }

    private void registerModules() {
        registerEvent(new ItemAliasModule());
        registerEvent(new DiscountModule());
        registerEvent(new MetricsModule());
        registerEvent(new PriceRestrictionModule(plugin.getItemUtil()));
        registerEvent(new StockCounterModule());

        registerEconomicalModules();
    }

    private void registerEconomicalModules() {
        registerEvent(new ServerAccountCorrector());
        registerEvent(new TaxModule(plugin));
    }

    public void registerEvent(Listener listener) {
        pluginManager.registerEvents(listener, plugin);
    }
}
