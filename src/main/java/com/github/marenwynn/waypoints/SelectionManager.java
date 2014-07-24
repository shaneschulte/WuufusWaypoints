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

public class SelectionManager {

    private static SelectionManager sm;

    private Map<UUID, Waypoint>     selectedWaypoints;
    private Waypoint                consoleSelection;

    public SelectionManager() {
        selectedWaypoints = new HashMap<UUID, Waypoint>();
    }

    public static SelectionManager getManager() {
        if (sm == null)
            sm = new SelectionManager();

        return sm;
    }

    public Waypoint getSelectedWaypoint(CommandSender sender) {
        if (sender instanceof Player) {
            UUID playerUUID = ((Player) sender).getUniqueId();

            if (selectedWaypoints.containsKey(playerUUID))
                return selectedWaypoints.get(playerUUID);

            return null;
        } else {
            return consoleSelection;
        }
    }

    public void setSelectedWaypoint(CommandSender sender, Waypoint wp) {
        if (sender instanceof Player)
            selectedWaypoints.put(((Player) sender).getUniqueId(), wp);
        else
            consoleSelection = wp;

        sendSelectionInfo(sender, wp);
    }

    public void clearSelectedWaypoint(CommandSender sender) {
        if (sender instanceof Player) {
            UUID playerUUID = ((Player) sender).getUniqueId();

            if (selectedWaypoints.containsKey(playerUUID))
                selectedWaypoints.remove(playerUUID);
        } else {
            consoleSelection = null;
        }
    }

    public void clearSelectionsWith(Waypoint wp) {
        for (UUID playerUUID : selectedWaypoints.keySet()) {
            Waypoint selected = selectedWaypoints.get(playerUUID);

            if (selected != null && selected == wp)
                selectedWaypoints.remove(playerUUID);
        }

        if (consoleSelection != null && consoleSelection == wp)
            consoleSelection = null;
    }

    private void sendSelectionInfo(CommandSender sender, Waypoint wp) {
        boolean serverDefined = WaypointManager.getManager().isServerDefined(wp);
        Location loc = wp.getLocation();
        String displayName = wp.getName();

        if (serverDefined && !wp.isEnabled())
            displayName += Util.color(" &f[&cDisabled&f]");

        Msg.BORDER.sendTo(sender);
        Msg.SELECTED_1.sendTo(sender, displayName, loc.getWorld().getName());
        Msg.BORDER.sendTo(sender);
        Msg.SELECTED_2.sendTo(sender, loc.getBlockX(), (int) loc.getPitch());
        Msg.SELECTED_3.sendTo(sender, loc.getBlockY(), (int) loc.getYaw());
        Msg.SELECTED_4.sendTo(sender, loc.getBlockZ());

        if (serverDefined) {
            String discoveryMode = wp.isDiscoverable() == null ? "Disabled" : (wp.isDiscoverable() ? "Server-wide"
                    : "World-specific");
            sender.sendMessage("");
            Msg.SELECTED_DISCOVER.sendTo(sender, discoveryMode);
        }

        Msg.BORDER.sendTo(sender);

        if (wp.getDescription().length() == 0)
            Msg.WP_NO_DESC.sendTo(sender);
        else
            for (String line : Util.getWrappedLore(wp.getDescription(), 35))
                Msg.LORE_LINE.sendTo(sender, ChatColor.stripColor(Util.color(line)));

        Msg.BORDER.sendTo(sender);
    }

}
