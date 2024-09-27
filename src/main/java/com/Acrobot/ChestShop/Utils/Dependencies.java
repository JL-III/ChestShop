package com.Acrobot.ChestShop.Utils;

import com.Acrobot.ChestShop.Events.EventManager;
import com.Acrobot.ChestShop.Listeners.Economy.EconomyAdapter;
import com.Acrobot.ChestShop.Listeners.Economy.VaultListener;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

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
                PluginDescriptionFile description = plugin.getDescription();
                plugin.getLogger().info(description.getName() + " version " + description.getVersion() + " loaded.");
            }
        }
    }

    public boolean loadEconomy() {
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
}
