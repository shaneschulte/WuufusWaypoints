package com.github.marenwynn.waypoints.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;

import com.github.marenwynn.waypoints.Util;

public class PlayerData implements Serializable {

    private static final long serialVersionUID = -8683386697368529683L;

    private UUID              playerUUID;
    private List<Waypoint>    homeWaypoints;
    private List<UUID>        discovered;
    private GridLocation      spawnPoint;

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        homeWaypoints = new ArrayList<Waypoint>();
        discovered = new ArrayList<UUID>();
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

    public Waypoint getWaypoint(String name) {
        String key = Util.getKey(name);

        for (Waypoint wp : homeWaypoints)
            if (Util.getKey(wp.getName()).equals(key))
                return wp;

        return null;
    }

    public Waypoint addWaypoint(Waypoint wp) {
        int maxHomes = DataManager.getManager().MAX_HOME_WAYPOINTS;
        homeWaypoints.add(wp);

        // Leave one to show waypoints are being deleted
        if (homeWaypoints.size() > maxHomes + 1)
            homeWaypoints.retainAll(homeWaypoints.subList(homeWaypoints.size() - maxHomes - 1, homeWaypoints.size()));

        return homeWaypoints.size() > maxHomes ? homeWaypoints.remove(0) : null;
    }

    public void removeWaypoint(Waypoint wp) {
        homeWaypoints.remove(wp);
    }

    public List<Waypoint> getAllWaypoints() {
        return homeWaypoints;
    }

    public Location getSpawnPoint() {
        return spawnPoint != null ? spawnPoint.getLocation() : null;
    }

    public void setSpawnPoint(Location loc) {
        spawnPoint = loc != null ? new GridLocation(loc) : null;
    }

}
