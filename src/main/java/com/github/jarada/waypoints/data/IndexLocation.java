package com.github.jarada.waypoints.data;

import org.bukkit.Location;

import java.util.Objects;

public class IndexLocation extends Object {
    private int x, y, z;

    public IndexLocation(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public IndexLocation(Location l) {
        this.x = l.getBlockX();
        this.y = l.getBlockY();
        this.z = l.getBlockZ();
    }

    @Override
    public String toString() {
        return "IndexLocation{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexLocation that = (IndexLocation) o;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
