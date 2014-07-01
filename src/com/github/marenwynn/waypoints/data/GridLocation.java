package com.github.marenwynn.waypoints.data;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class GridLocation implements Serializable {

    private static final long serialVersionUID = 3704833816570970873L;

    private String            worldName;
    private int               x, y, z;
    private float             pitch, yaw;

    public GridLocation(Location loc) {
        setLocation(loc);
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    public void setLocation(Location loc) {
        worldName = loc.getWorld().getName();
        x = loc.getBlockX();
        y = loc.getBlockY();
        z = loc.getBlockZ();
        pitch = loc.getPitch();
        yaw = loc.getYaw();
    }

}
