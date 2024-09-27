package com.Acrobot.ChestShop;

import com.Acrobot.Breeze.Configuration.Configuration;
import com.Acrobot.ChestShop.Commands.*;
import com.Acrobot.ChestShop.Configuration.Messages;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Events.EventManager;
import com.Acrobot.ChestShop.Events.Protection.ProtectionCheckEvent;
import com.Acrobot.ChestShop.Events.tobesorted.ChestShopReloadEvent;
import com.Acrobot.ChestShop.Listeners.Economy.VaultListener;
import com.Acrobot.ChestShop.Logging.FileFormatter;
import com.Acrobot.ChestShop.Metadata.ItemDatabase;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.ItemUtil;
import com.Acrobot.ChestShop.Utils.NameManager;
import com.Acrobot.ChestShop.Utils.uBlock;
import com.Acrobot.ChestShop.Utils.Dependencies;
import com.Acrobot.ChestShop.Utils.Permission;
import com.Acrobot.ChestShop.Utils.Security;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import static com.Acrobot.Breeze.Utils.BlockUtil.isSign;
import static com.Acrobot.Breeze.Utils.ImplementationAdapter.getState;

/**
 * Main file of the plugin
 *
 * @author Acrobot
 */
public class ChestShop extends JavaPlugin implements Listener {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private final EventManager eventManager;
    private static BukkitAudiences audiences;
    private final NameManager nameManager;
    private final ChestShopSign chestShopSign;

    private static File dataFolder;
    private static ItemDatabase itemDatabase;

    private static Logger logger;
    private static Logger shopLogger;
    private FileHandler handler;
    private final Security security;
    private final ItemInfo itemInfo;
    private final ItemUtil itemUtil;
    private final Dependencies dependencies;

    private final List<PluginCommand> commands = new ArrayList<>();

    public ChestShop() {
        dataFolder = getDataFolder();
        logger = getLogger();
        shopLogger = Logger.getLogger("ChestShop Shops");
        shopLogger.setParent(logger);
        nameManager = new NameManager(this);
        chestShopSign = new ChestShopSign();
        eventManager = new EventManager(getServer().getPluginManager(), this, chestShopSign, nameManager);
        security = new Security(this);
        itemUtil = new ItemUtil();
        itemInfo = new ItemInfo(this, itemUtil);
        dependencies = new Dependencies(this, eventManager, new VaultListener(this));
    }

    @Override
    public void onLoad() {
        dependencies.initializePlugins();
    }

    @Override
    public void onEnable() {

        audiences = BukkitAudiences.create(this);
        turnOffDatabaseLogging();
        File versionFile = loadFile("version");
        registerCommand("iteminfo", itemInfo, Permission.ITEMINFO);
        registerCommand("shopinfo", new ShopInfo(this), Permission.SHOPINFO);
        registerCommand("csVersion", new Version(this), Permission.ADMIN);
        registerCommand("csMetrics", new com.Acrobot.ChestShop.Commands.Metrics(), Permission.ADMIN);
        registerCommand("csGive", new Give(itemUtil), Permission.ADMIN);
        registerCommand("cstoggle", new Toggle(), Permission.NOTIFY_TOGGLE);
        registerCommand("csaccess", new AccessToggle(), Permission.ACCESS_TOGGLE);

        loadConfig();

        itemDatabase = new ItemDatabase();

        if (!dependencies.loadEconomy()) {
            getServer().getPluginManager().disablePlugin(this);
        }
        eventManager.registerEvents();
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void registerCommand(String name, CommandExecutor executor, Permission permission) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
            command.setPermission(permission.toString());
            commands.add(command);
        }
    }

    public void loadConfig() {
        Configuration.pairFileAndClass(loadFile("config.yml"), Properties.class, getBukkitLogger());

        new Messages(this).load();

        NameManager.load();

        commands.forEach(c -> c.setPermissionMessage(Messages.ACCESS_DENIED.getTextWithPrefix(null)));

        if (handler != null) {
            shopLogger.removeHandler(handler);
        }

        if (Properties.LOG_TO_FILE) {
            if (handler == null) {
                File log = loadFile("ChestShop.log");

                handler = loadHandler(log.getAbsolutePath());
                handler.setFormatter(new FileFormatter());
            }
            shopLogger.addHandler(handler);
        }

        shopLogger.setUseParentHandlers(Properties.LOG_TO_CONSOLE);
    }

    private void turnOffDatabaseLogging() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        org.apache.logging.log4j.core.config.Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig("");

        loggerConfig.addFilter(new AbstractFilter() {
            @Override
            public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, String msg, Object... params) {
                return filter(logger.getName(), level);
            }

            @Override
            public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, Object msg, Throwable t) {
                return filter(logger.getName(), level);
            }

            @Override
            public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, Message msg, Throwable t) {
                return filter(logger.getName(), level);
            }

            @Override
            public Result filter(LogEvent event) {
                return filter(event.getLoggerName(), event.getLevel());
            }

            private Result filter(String classname, Level level) {
                if (level.intLevel() <= Level.ERROR.intLevel() && !classname.contains("SqliteDatabaseType")) {
                    return Result.NEUTRAL;
                }

                if (classname.contains("SqliteDatabaseType") || classname.contains("TableUtils")) {
                    return Result.DENY;
                } else {
                    return Result.NEUTRAL;
                }
            }
        });
    }

    public static File loadFile(String string) {
        File file = new File(dataFolder, string);

        return loadFile(file);
    }

    private static File loadFile(File file) {
        if (!file.exists()) {
            try {
                if (file.getParent() != null) {
                    file.getParentFile().mkdirs();
                }

                file.createNewFile();
            } catch (IOException e) {
                getBukkitLogger().log(java.util.logging.Level.SEVERE, "Unable to load file " + file.getName(), e);
            }
        }

        return file;
    }

    private static FileHandler loadHandler(String path) {
        FileHandler handler = null;

        try {
            handler = new FileHandler(path, true);
        } catch (IOException ex) {
            getBukkitLogger().log(java.util.logging.Level.SEVERE, "Unable to load handler " + path, ex);
        }

        return handler;
    }

    @Override
    public void onDisable() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(15, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}

        Toggle.clearToggledPlayers();

        if (handler != null) {
            handler.close();
            getLogger().removeHandler(handler);
        }
    }

    public static ItemDatabase getItemDatabase() {
        return itemDatabase;
    }

    public static File getFolder() {
        return dataFolder;
    }

    public static Logger getShopLogger() {
        return shopLogger;
    }

    public static Logger getBukkitLogger() {
        return logger;
    }

    public static void logDebug(String message) {
        if (Properties.DEBUG) {
            getBukkitLogger().info("[DEBUG] " + message);
        }
    }

    public static BukkitAudiences getAudiences() {
        return audiences;
    }

    public static void runInAsyncThread(Runnable runnable) {
        executorService.submit(runnable);
    }

    public ItemInfo getItemInfo() {
        return itemInfo;
    }

    public ItemUtil getItemUtil() {
        return itemUtil;
    }

    @EventHandler
    public void onReload(ChestShopReloadEvent event) {
        loadConfig();
    }

    @EventHandler
    public void onProtectionCheck(ProtectionCheckEvent event) {
        if (event.getResult() == Event.Result.DENY || event.isBuiltInProtectionIgnored()) {
            return;
        }

        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (!canAccess(player, block, chestShopSign, nameManager)) {
            event.setResult(Event.Result.DENY);
        }
    }

    public boolean canAccess(Player player, Block block, ChestShopSign chestShopSign, NameManager nameManager) {
        if (!canBeProtected(block)) {
            return true;
        }

        if (isSign(block)) {
            Sign sign = (Sign) getState(block, false);

            if (!ChestShopSign.isValid(sign)) {
                return true;
            }

            if (!isShopMember(player, sign, chestShopSign, nameManager)) {
                return false;
            }
        }

        if (uBlock.couldBeShopContainer(block)) {
            Sign sign = uBlock.getConnectedSign(block);

            if (sign != null && !isShopMember(player, sign, chestShopSign, nameManager)) {
                return false;
            }
        }

        return true;
    }

    private static boolean canBeProtected(Block block) {
        return isSign(block) || uBlock.couldBeShopContainer(block);
    }

    private boolean isShopMember(Player player, Sign sign, ChestShopSign chestShopSign, NameManager nameManager) {
        return chestShopSign.hasPermission(player, Permission.OTHER_NAME_ACCESS, sign, nameManager);
    }

    public Security getSecurity() {
        return security;
    }
}
