package com.github.marenwynn.waypoints.data;

import org.bukkit.Location;
import org.bukkit.Material;

public class Waypoint extends GridLocation {

    private static final long serialVersionUID = 3872196300104397877L;

    private String            name, description;
    private Material          icon;
    private short             durability;

    public Waypoint(String name, Location loc) {
        super(loc);
        setName(name);
        setDescription("");
        setIcon(Material.IRON_DOOR);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public short getDurability() {
        return durability;
    }

    public void setDurability(short durability) {
        this.durability = durability;
    }

}
