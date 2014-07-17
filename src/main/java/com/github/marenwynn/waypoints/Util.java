package com.github.marenwynn.waypoints;

import java.util.List;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.marenwynn.waypoints.data.Waypoint;

public class Util {

    public static String buildString(String[] args, int start, char filler) {
        StringBuilder sb = new StringBuilder();

        for (int i = start; i < args.length; i++) {
            sb.append(args[i]);

            if (i < args.length - 1)
                sb.append(filler);
        }

        return sb.toString();
    }

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String getKey(String string) {
        return ChatColor.stripColor(string.toLowerCase()).replaceAll(" ", "_");
    }

    public static String[] getWrappedLore(String description, int maxLineLength) {
        return WordUtils.wrap(description, maxLineLength, "\n", true).split("\\n");
    }

    public static boolean hasAccess(Player p, Waypoint wp, boolean select) {
        if (p.hasPermission("wp.access." + getKey(wp.getName())))
            return true;

        if (wp.isDiscoverable() != null && WaypointManager.getPlayerData(p.getUniqueId()).hasDiscovered(wp.getUUID())) {
            if (wp.isDiscoverable()) {
                return true;
            } else {
                if (select || p.getWorld().getName().equals(wp.getLocation().getWorld().getName()))
                    return true;
                else
                    return false;
            }
        }

        return false;
    }

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

    public static void playEffect(Location loc, Effect effect) {
        loc.getWorld().playEffect(loc, effect, 0);
    }

    public static void playSound(Location loc, Sound sound) {
        loc.getWorld().playSound(loc, sound, 10F, 1F);
    }

    public static ItemStack setItemNameAndLore(ItemStack item, String name, List<String> lore) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(color(name));

        if (lore != null)
            im.setLore(lore);

        item.setItemMeta(im);
        return item;
    }

}
