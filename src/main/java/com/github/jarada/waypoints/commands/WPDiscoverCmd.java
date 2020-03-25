package com.github.jarada.waypoints.commands;

import org.bukkit.command.CommandSender;

import com.github.jarada.waypoints.SelectionManager;
import com.github.jarada.waypoints.WaypointManager;
import com.github.jarada.waypoints.data.DataManager;
import com.github.jarada.waypoints.data.Msg;
import com.github.jarada.waypoints.data.Waypoint;

public class WPDiscoverCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        Waypoint wp = SelectionManager.getManager().getSelectedWaypoint(sender);

        if (wp == null) {
            Msg.WP_NOT_SELECTED_ERROR.sendTo(sender);
            Msg.WP_NOT_SELECTED_ERROR_USAGE.sendTo(sender);
            return;
        }

        if (WaypointManager.getManager().getWaypoint(wp.getName()) == null) {
            Msg.ONLY_SERVER_DEFINED.sendTo(sender);
            return;
        }

        if (wp.isDiscoverable() == null) {
            wp.setDiscoverable(true);
            Msg.DISCOVERY_MODE_ENABLED_SERVER.sendTo(sender, wp.getName());
        } else if (wp.isDiscoverable()) {
            wp.setDiscoverable(false);
            Msg.DISCOVERY_MODE_ENABLED_WORLD.sendTo(sender, wp.getName());
        } else {
            wp.setDiscoverable(null);
            Msg.DISCOVERY_MODE_DISABLED.sendTo(sender, wp.getName());
        }

        DataManager.getManager().saveWaypoints();
    }

    @Override
    public boolean isConsoleExecutable() {
        return true;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.discover");
    }

}
