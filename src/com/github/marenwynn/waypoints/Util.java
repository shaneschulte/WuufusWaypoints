package com.github.marenwynn.waypoints;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class Util {

    public static boolean isSameLoc(Location a, Location b, boolean useGrid) {
        if (useGrid) {
            if (a.getBlockX() == b.getBlockX() && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b.getBlockZ())
                return true;
        } else {
            if (a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() == b.getZ())
                return true;
        }

        return false;
    }

    public static String getKey(String string) {
        return ChatColor.stripColor(string.toLowerCase()).replaceAll(" ", "_");
    }

    public static String[] getWrappedLore(String description, int maxLineLength) {
        return WordUtils.wrap(description, maxLineLength, "\n", true).split("\\n");
    }

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

}
