package com.github.marenwynn.waypoints.commands;

import org.bukkit.command.CommandSender;

import com.github.marenwynn.waypoints.SelectionManager;
import com.github.marenwynn.waypoints.WaypointManager;
import com.github.marenwynn.waypoints.data.DataManager;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;

public class WPToggleCmd implements PluginCommand {

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

        if (wp.isEnabled()) {
            wp.setEnabled(false);
            Msg.WAYPOINT_DISABLED.sendTo(sender, wp.getName());
        } else {
            wp.setEnabled(true);
            Msg.WAYPOINT_ENABLED.sendTo(sender, wp.getName());
        }

        DataManager.getManager().saveWaypoints();
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
