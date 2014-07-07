package com.github.marenwynn.waypoints.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.github.marenwynn.waypoints.PluginMain;
import com.github.marenwynn.waypoints.data.Data;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;

public class WPDiscoverCmd implements PluginCommand {

    private PluginMain pm;

    public WPDiscoverCmd(PluginMain pm) {
        this.pm = pm;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Waypoint wp = pm.getSelectedWaypoint(sender.getName());

        if (wp == null) {
            Msg.WP_NOT_SELECTED_ERROR.sendTo(sender);
            Msg.WP_NOT_SELECTED_ERROR_USAGE.sendTo(sender);
            return true;
        }

        if (Data.getWaypoint(wp.getName()) == null) {
            Msg.ONLY_SERVER_DEFINED.sendTo(sender);
            return true;
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

        Data.saveWaypoints();
        return true;
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
