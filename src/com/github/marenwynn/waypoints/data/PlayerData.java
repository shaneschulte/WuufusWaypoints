package com.github.marenwynn.waypoints.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Location;

import com.github.marenwynn.waypoints.Util;

public class PlayerData implements Serializable {

    private static final long   serialVersionUID = -8683386697368529683L;

    private UUID                playerUUID;
    private ArrayList<Waypoint> homeWaypoints;
    private ArrayList<UUID>     discovered;
    private GridLocation        spawnPoint;

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

    public boolean retainDiscoveries(ArrayList<UUID> discovered) {
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
        return homeWaypoints.size() > 3 ? homeWaypoints.remove(0) : null;
    }

    public void removeWaypoint(Waypoint wp) {
        homeWaypoints.remove(wp);
    }

    public ArrayList<Waypoint> getAllWaypoints() {
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
