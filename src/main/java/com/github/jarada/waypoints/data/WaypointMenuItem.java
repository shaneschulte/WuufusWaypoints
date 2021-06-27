package com.github.jarada.waypoints.data;

import com.github.jarada.waypoints.Util;

import java.util.UUID;

public class WaypointMenuItem {

    private Waypoint waypoint;
    private Category category;
    private UUID player;
    private boolean discoverMode;

    public WaypointMenuItem(Waypoint waypoint) {
        this.waypoint = waypoint;
        this.discoverMode = false;
    }

    public WaypointMenuItem(Waypoint waypoint, boolean discoverMode) {
        this.waypoint = waypoint;
        this.discoverMode = discoverMode;
    }

    public WaypointMenuItem(Category category) {
        this.category = category;
    }

    public WaypointMenuItem(UUID player) {
        this.player = player;
    }

    public Waypoint getWaypoint() {
        return waypoint;
    }

    public String getWaypointKey() {
        return waypoint != null ? Util.getKey(waypoint.getName()) : null;
    }

    public Category getCategory() {
        return category;
    }

    public UUID getPlayerUUID() {
        return player;
    }

    public boolean isDiscoverMode() {
        return discoverMode;
    }

    public boolean isCategory() {
        return category != null;
    }

    public boolean isPlayerCategory() {
        return player != null;
    }
}
