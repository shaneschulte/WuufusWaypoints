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
        return discovered.retainAll(discovered);
    }

    public Waypoint getWaypoint(String name) {
        String key = Util.getKey(name);

        for (Waypoint wp : homeWaypoints)
            if (Util.getKey(wp.getName()).equals(key))
                return wp;

        return null;
    }

    public Waypoint addWaypoint(Waypoint wp) {
        homeWaypoints.add(wp);

        if (homeWaypoints.size() > Data.MAX_HOME_WAYPOINTS + 1)
            // Leave one to show waypoints are being deleted
            homeWaypoints.retainAll(homeWaypoints.subList(homeWaypoints.size() - Data.MAX_HOME_WAYPOINTS - 1,
                    homeWaypoints.size()));

        return homeWaypoints.size() > Data.MAX_HOME_WAYPOINTS ? homeWaypoints.remove(0) : null;
    }

    public void removeWaypoint(Waypoint wp) {
        homeWaypoints.remove(wp);
    }

    public List<Waypoint> getAllWaypoints() {
        return homeWaypoints;
    }

    public Location getSpawnPoint() {
        if (spawnPoint != null)
            return spawnPoint.getLocation();

        return null;
    }

    public void setSpawnPoint(Location loc) {
        if (loc == null) {
            spawnPoint = null;
            return;
        }

        spawnPoint = new GridLocation(loc);
    }

}
