package com.Acrobot.ChestShop.todo;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Configuration.Properties;
import com.Acrobot.ChestShop.Events.EventManager;
import com.Acrobot.ChestShop.Listeners.Economy.EconomyAdapter;
import com.Acrobot.ChestShop.Listeners.Economy.VaultListener;
import com.Acrobot.ChestShop.Plugins.LightweightChestProtection;
import com.Acrobot.ChestShop.Plugins.WorldGuardBuilding;
import com.Acrobot.ChestShop.Plugins.WorldGuardFlags;
import com.Acrobot.ChestShop.Plugins.WorldGuardProtection;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

import java.util.logging.Level;

/**
 * @author Acrobot
 */
public class Dependencies implements Listener {
    private final Plugin plugin;
    private final EventManager eventManager;
    private final VaultListener vaultListener;

    public Dependencies(Plugin plugin, EventManager eventManager, VaultListener vaultListener) {
        this.plugin = plugin;
        this.eventManager = eventManager;
        this.vaultListener = vaultListener;
    }

    public void initializePlugins() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        for (String dependency : plugin.getDescription().getPluginDependencies()) {
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
            economy = vaultListener.initializeVault();
        }

        if (economy == null) {
            com.Acrobot.ChestShop.ChestShop.getBukkitLogger().severe("No Economy adapter found! You need to install either Vault or Reserve!");
            return false;
        }

        eventManager.registerEvent(economy);
        com.Acrobot.ChestShop.ChestShop.getBukkitLogger().info(plugin + " loaded!");
        return true;
    }

    private boolean loadPlugin(String name, Plugin plugin) {
        Dependency dependency;

        try {
            dependency = Dependency.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return false;
        }

        Listener listener = null;

        switch (dependency) {
            //Protection plugins
            case LWC:
                listener = new LightweightChestProtection(new Security(plugin));
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
        }

        if (listener != null) {
            eventManager.registerEvent(listener);
        }

        PluginDescriptionFile description = plugin.getDescription();
        ChestShop.getBukkitLogger().info(description.getName() + " version " + description.getVersion() + " hooked.");

        return true;
    }

    private enum Dependency {
        LWC,
        WorldGuard,
        ItemBridge,
        ShowItem
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
