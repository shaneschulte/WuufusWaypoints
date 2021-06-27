package com.github.jarada.waypoints.data;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Location;

import com.github.jarada.waypoints.Util;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerData implements Serializable {

    private final UUID        playerUUID;
    private List<UUID>        discovered;
    private GridLocation      spawnPoint;
    private boolean           silentWaypoints;

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        discovered = new ArrayList<UUID>();
    }

    public PlayerData(YamlConfiguration config, String prefix) {
        playerUUID = Serializer.getUUID(config, prefix, "uuid");

        discovered = new ArrayList<UUID>();
        if (Serializer.isList(config, prefix, "discovered")) {
            for (Object obj : Serializer.getList(config, prefix, "discovered")) {
                discovered.add(UUID.fromString((String) obj));
            }
        }

        ConfigurationSection section = Serializer.getConfigurationSection(config, prefix, null);
        if (section != null) {
            if (section.getKeys(false).contains("spawn")) {
                spawnPoint = new GridLocation(config, Serializer.setupPrefix(prefix) + "spawn");
            }
        }

        silentWaypoints = Serializer.getBoolean(config, prefix, "silentWaypoints");
    }

    public void serialize(YamlConfiguration config, String prefix) {
        Serializer.set(config, prefix, "uuid", playerUUID.toString());
        Serializer.set(config, prefix, "discovered", discovered.stream().map(UUID::toString).collect(Collectors.toList()));
        if (spawnPoint != null) {
            spawnPoint.serialize(config, Serializer.setupPrefix(prefix) + "spawn");
        }
        Serializer.set(config, prefix, "silentWaypoints", silentWaypoints);
    }

    public UUID getUUID() {
        return playerUUID;
    }

    public boolean hasDiscovered(UUID uuid) {
        return discovered.contains(uuid);
    }

    public void addDiscovery(UUID uuid) {
        discovered.add(uuid);
    }

    public void removeDiscovery(UUID uuid) {
        discovered.remove(uuid);
    }

    public boolean retainDiscoveries(List<UUID> discovered) {
        return this.discovered.retainAll(discovered);
    }

    public boolean isSilentWaypoints() {
        return silentWaypoints;
    }

    public void setSilentWaypoints(boolean silentWaypoints) {
        this.silentWaypoints = silentWaypoints;
    }
}
