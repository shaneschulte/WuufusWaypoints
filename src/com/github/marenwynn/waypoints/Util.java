package com.github.marenwynn.waypoints;

import java.util.ArrayList;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

    public static ItemStack setItemNameAndLore(ItemStack item, String name, ArrayList<String> lore) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);

        if (lore != null)
            im.setLore(lore);

        item.setItemMeta(im);
        return item;
    }

}
