package com.github.marenwynn.waypoints.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.github.marenwynn.waypoints.Selections;
import com.github.marenwynn.waypoints.WaypointManager;
import com.github.marenwynn.waypoints.data.Data;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;

public class WPToggleCmd implements PluginCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Waypoint wp = Selections.getSelectedWaypoint(sender);

        if (wp == null) {
            Msg.WP_NOT_SELECTED_ERROR.sendTo(sender);
            Msg.WP_NOT_SELECTED_ERROR_USAGE.sendTo(sender);
            return true;
        }

        if (WaypointManager.getWaypoint(wp.getName()) == null) {
            Msg.ONLY_SERVER_DEFINED.sendTo(sender);
            return true;
        }

        if (wp.isEnabled()) {
            wp.setEnabled(false);
            Msg.WAYPOINT_DISABLED.sendTo(sender, wp.getName());
        } else {
            wp.setEnabled(true);
            Msg.WAYPOINT_ENABLED.sendTo(sender, wp.getName());
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
        return sender.hasPermission("wp.toggle");
    }

}
