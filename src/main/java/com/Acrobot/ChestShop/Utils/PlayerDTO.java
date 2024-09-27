package com.Acrobot.ChestShop.Utils;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Data Transfer Object for Player objects
 * Since Bukkit API is not thread-safe, this should work
 * @author Andrzej Pomirski
 */
public class PlayerDTO {
    private final UUID uniqueId;
    private final String name;

    public PlayerDTO(UUID uuid, String name) {
        this.uniqueId = uuid;
        this.name = name;
    }

    public PlayerDTO(Player player) {
        this.uniqueId = player.getUniqueId();
        this.name = player.getName();
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }
}
