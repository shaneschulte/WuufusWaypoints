package com.github.marenwynn.waypoints;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;

public class Selections {

    private static Map<UUID, Waypoint> selectedWaypoints;
    private static Waypoint            consoleSelection;

    public static void init() {
        selectedWaypoints = new HashMap<UUID, Waypoint>();
    }

    public static void kill() {
        selectedWaypoints = null;
        consoleSelection = null;
    }

    public static Waypoint getSelectedWaypoint(CommandSender sender) {
        if (sender instanceof Player) {
            UUID playerUUID = ((Player) sender).getUniqueId();

            if (selectedWaypoints.containsKey(playerUUID))
                return selectedWaypoints.get(playerUUID);

            return null;
        } else {
            return consoleSelection;
        }
    }

    public static void setSelectedWaypoint(CommandSender sender, Waypoint wp) {
        if (sender instanceof Player)
            selectedWaypoints.put(((Player) sender).getUniqueId(), wp);
        else
            consoleSelection = wp;

        sendSelectionInfo(sender, wp);
    }

    public static void clearSelectedWaypoint(CommandSender sender) {
        if (sender instanceof Player) {
            UUID playerUUID = ((Player) sender).getUniqueId();

            if (selectedWaypoints.containsKey(playerUUID))
                selectedWaypoints.remove(playerUUID);
        } else {
            consoleSelection = null;
        }
    }

    public static void clearSelectionsWith(Waypoint wp) {
        for (UUID playerUUID : selectedWaypoints.keySet()) {
            Waypoint selected = selectedWaypoints.get(playerUUID);

            if (selected != null && selected == wp)
                selectedWaypoints.remove(playerUUID);
        }

        if (consoleSelection != null && consoleSelection == wp)
            consoleSelection = null;
    }

    private static void sendSelectionInfo(CommandSender sender, Waypoint wp) {
        Location loc = wp.getLocation();
        String suffix = WaypointManager.getAllWaypoints().contains(wp) ? Util.color(wp.isEnabled() ? ""
                : " &f[&cDisabled&f]") : "";

        Msg.BORDER.sendTo(sender);
        Msg.SELECTED_1.sendTo(sender, wp.getName() + suffix, loc.getWorld().getName());
        Msg.BORDER.sendTo(sender);
        Msg.SELECTED_2.sendTo(sender, loc.getBlockX(), (int) loc.getPitch());
        Msg.SELECTED_3.sendTo(sender, loc.getBlockY(), (int) loc.getYaw());
        Msg.SELECTED_4.sendTo(sender, loc.getBlockZ());

        String discoveryMode = wp.isDiscoverable() == null ? "Disabled" : (wp.isDiscoverable() ? "Server-wide"
                : "World-specific");

        if (WaypointManager.getAllWaypoints().contains(wp)) {
            sender.sendMessage("");
            Msg.SELECTED_DISCOVER.sendTo(sender, discoveryMode);
        }

        Msg.BORDER.sendTo(sender);

        if (wp.getDescription().equals(""))
            Msg.WP_NO_DESC.sendTo(sender);
        else
            for (String line : Util.getWrappedLore(wp.getDescription(), 35))
                Msg.LORE_LINE.sendTo(sender, ChatColor.stripColor(Util.color(line)));

        Msg.BORDER.sendTo(sender);
    }

}
