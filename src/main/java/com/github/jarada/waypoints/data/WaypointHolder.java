package com.github.jarada.waypoints.data;

public class WaypointHolder {

    private final Waypoint waypoint;
    private final boolean discoverable;

    public WaypointHolder(Waypoint waypoint) {
        this.waypoint = waypoint;
        this.discoverable = false;
    }

    public WaypointHolder(Waypoint waypoint, boolean discoverable) {
        this.waypoint = waypoint;
        this.discoverable = discoverable;
    }

    public Waypoint getWaypoint() {
        return waypoint;
    }

    public boolean isDiscoverable() {
        return discoverable;
    }
}
