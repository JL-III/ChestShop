package com.Acrobot.ChestShop.Events;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Listeners.Block.BlockPlace;
import com.Acrobot.ChestShop.Listeners.Block.Break.ChestBreak;
import com.Acrobot.ChestShop.Listeners.Block.Break.SignBreak;
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
import com.Acrobot.ChestShop.Signs.RestrictedSign;
import com.Acrobot.ChestShop.Utils.NameManager;
import com.Acrobot.ChestShop.todo.Dependencies;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public class EventManager {
    private final PluginManager pluginManager;
    private final ChestShop plugin;

    public EventManager(PluginManager pluginManager, ChestShop plugin) {
        this.pluginManager = pluginManager;
        this.plugin = plugin;
        registerEvents();
    }

    private void registerEvents() {
        registerEvent(new com.Acrobot.ChestShop.Plugins.ChestShop()); //Chest protection

        registerEvent(new Dependencies(this));

        registerEvent(new NameManager());

        registerPreShopCreationEvents();
        registerPreTransactionEvents();
        registerPostShopCreationEvents();
        registerPostTransactionEvents();
        registerShopRemovalEvents();

        registerModules();

        registerEvent(new SignBreak(this));
        registerEvent(new SignCreate());
        registerEvent(new ChestBreak());

        registerEvent(new BlockPlace());
        registerEvent(new PlayerConnect());
        registerEvent(new PlayerInteract());
        registerEvent(new PlayerInventory());
        registerEvent(new PlayerLeave());
        registerEvent(new PlayerTeleport());

        registerEvent(new SignParseListener());
        registerEvent(new ItemStringListener());
        registerEvent(new ItemInfoListener(this));
        registerEvent(new ShopInfoListener());
        registerEvent(new GarbageTextListener());

        registerEvent(new RestrictedSign());

        if (!Properties.TURN_OFF_HOPPER_PROTECTION) {
            registerEvent(new ItemMoveListener());
        }
    }

    private void registerShopRemovalEvents() {
        registerEvent(new ShopRefundListener());
        registerEvent(new ShopRemovalLogger());
    }

    private void registerPreShopCreationEvents() {
        if (Properties.BLOCK_SHOPS_WITH_SELL_PRICE_HIGHER_THAN_BUY_PRICE) {
            registerEvent(new PriceRatioChecker());
        }

        registerEvent(new ChestChecker());
        registerEvent(new ItemChecker());
        registerEvent(new MoneyChecker());
        registerEvent(new NameChecker());
        registerEvent(new com.Acrobot.ChestShop.Listeners.PreShopCreation.PermissionChecker());
        registerEvent(new com.Acrobot.ChestShop.Listeners.PreShopCreation.ErrorMessageSender());
        registerEvent(new PriceChecker());
        registerEvent(new QuantityChecker());
        registerEvent(new TerrainChecker());
    }

    private void registerPostShopCreationEvents() {
        registerEvent(new CreationFeeGetter());
        registerEvent(new MessageSender());
        registerEvent(new SignSticker());
        registerEvent(new ShopCreationLogger());
    }

    private void registerPreTransactionEvents() {
        if (Properties.ALLOW_PARTIAL_TRANSACTIONS) {
            registerEvent(new PartialTransactionModule());
        } else {
            registerEvent(new AmountAndPriceChecker());
        }

        registerEvent(new InvalidNameIgnorer());
        registerEvent(new CreativeModeIgnorer());
        registerEvent(new com.Acrobot.ChestShop.Listeners.PreTransaction.ErrorMessageSender());
        registerEvent(new com.Acrobot.ChestShop.Listeners.PreTransaction.PermissionChecker());
        registerEvent(new PriceValidator());
        registerEvent(new ShopValidator());
        registerEvent(new SpamClickProtector());
        registerEvent(new StockFittingChecker());
    }

    private void registerPostTransactionEvents() {
        registerEvent(new EconomicModule());
        registerEvent(new EmptyShopDeleter());
        registerEvent(new ItemManager());
        registerEvent(new TransactionLogger());
        registerEvent(new TransactionMessageSender());
    }

    private void registerModules() {
        registerEvent(new ItemAliasModule());
        registerEvent(new DiscountModule());
        registerEvent(new MetricsModule());
        registerEvent(new PriceRestrictionModule());
        registerEvent(new StockCounterModule());

        registerEconomicalModules();
    }

    private void registerEconomicalModules() {
        registerEvent(new ServerAccountCorrector());
        registerEvent(new TaxModule());
    }

    public void registerEvent(Listener listener) {
        pluginManager.registerEvents(listener, plugin);
    }
}
