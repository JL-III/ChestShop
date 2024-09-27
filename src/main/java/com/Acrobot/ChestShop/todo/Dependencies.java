package com.Acrobot.ChestShop.todo;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Events.EventManager;
import com.Acrobot.ChestShop.Listeners.Economy.EconomyAdapter;
import com.Acrobot.ChestShop.Listeners.Economy.Plugins.VaultListener;
import com.Acrobot.ChestShop.Plugins.*;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Acrobot
 */
public class Dependencies implements Listener {
    private final EventManager eventManager;

    public Dependencies(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    private static final Map<String, String> versions = new HashMap<>();

    public static void initializePlugins() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        for (String dependency : com.Acrobot.ChestShop.ChestShop.getDependencies()) {
            Plugin plugin = pluginManager.getPlugin(dependency);

            if (plugin != null) {
                initializePlugin(dependency, plugin);
            }
        }
    }

    private static void initializePlugin(String name, Plugin plugin) { //Really messy, right? But it's short and fast :)
        Dependency dependency;

        try {
            dependency = Dependency.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return;
        }

        switch (dependency) {
            //Terrain protection plugins
            case WorldGuard:
                WorldGuardFlags.ENABLE_SHOP.getName();  // force the static code to run
                break;
        }

        PluginDescriptionFile description = plugin.getDescription();
        com.Acrobot.ChestShop.ChestShop.getBukkitLogger().info(description.getName() + " version " + description.getVersion() + " loaded.");
    }

    public boolean loadPlugins() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        for (Dependency dependency : Dependency.values()) {
            Plugin plugin = pluginManager.getPlugin(dependency.name());

            if (plugin != null && plugin.isEnabled()) {
                try {
                    loadPlugin(dependency.name(), plugin);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Unable to hook into " + plugin.getName() + " " + plugin.getDescription().getVersion(), e);
                }
            }
        }
        return loadEconomy();
    }

    private boolean loadEconomy() {
        String plugin = "none";

        EconomyAdapter economy = null;

        if(Bukkit.getPluginManager().getPlugin("Vault") != null) {
            plugin = "Vault";
            economy = VaultListener.initializeVault();
        }

        if (economy == null) {
            com.Acrobot.ChestShop.ChestShop.getBukkitLogger().severe("No Economy adapter found! You need to install either Vault or Reserve!");
            return false;
        }

        eventManager.registerEvent(economy);
        com.Acrobot.ChestShop.ChestShop.getBukkitLogger().info(plugin + " loaded!");
        return true;
    }

    private boolean loadPlugin(String name, Plugin plugin) { //Really messy, right? But it's short and fast :)
        Dependency dependency;

        try {
            dependency = Dependency.valueOf(name);

            if (dependency.author != null && !plugin.getDescription().getAuthors().contains(dependency.author)) {
                com.Acrobot.ChestShop.ChestShop.getBukkitLogger().info("You are not using the supported variant of " + name + " by " + dependency.author + "."
                        + " This variant of " + name + " seems to be made by " + plugin.getDescription().getAuthors().get(0) + " which isn't supported!");
                return false;
            }
        } catch (IllegalArgumentException exception) {
            return false;
        }

        Listener listener = null;

        switch (dependency) {
            //Protection plugins
            case LWC:
                listener = new LightweightChestProtection();
                break;

            //Terrain protection plugins
            case WorldGuard:
                boolean inUse = Properties.WORLDGUARD_USE_PROTECTION || Properties.WORLDGUARD_INTEGRATION;

                if (!inUse) {
                    return false;
                }

                if (Properties.WORLDGUARD_USE_PROTECTION) {
                    eventManager.registerEvent(new WorldGuardProtection(plugin));
                }

                if (Properties.WORLDGUARD_INTEGRATION) {
                    listener = new WorldGuardBuilding(plugin);
                }

                break;

            //Other plugins
            case ItemBridge:
                listener = new ItemBridgeListener();
                break;
            case ShowItem:
                MaterialUtil.Show.initialize(plugin);
                break;
        }

        if (listener != null) {
            eventManager.registerEvent(listener);
        }

        PluginDescriptionFile description = plugin.getDescription();
        versions.put(description.getName(), description.getVersion());
        ChestShop.getBukkitLogger().info(description.getName() + " version " + description.getVersion() + " hooked.");

        return true;
    }

    private enum Dependency {
        LWC,
        Lockette("Acru"),
        LockettePro,
        Deadbolt,
        SimpleChestLock,
        BlockLocker,
        Residence,

        WorldGuard,
        GriefPrevention,
        RedProtect,

        Heroes,

        ItemBridge,

        ShowItem;

        private final String author;

        Dependency() {
            this.author = null;
        }

        Dependency(String author) {
            this.author = author;
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnable(PluginEnableEvent event) {
        Plugin plugin = event.getPlugin();
        try {
            if (!loadPlugin(plugin.getName(), plugin)) {
                for (String pluginAlias : plugin.getDescription().getProvides()) {
                    if (loadPlugin(pluginAlias, plugin)) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Unable to hook into " + plugin.getName() + " " + plugin.getDescription().getVersion(), e);
        }
    }
}
