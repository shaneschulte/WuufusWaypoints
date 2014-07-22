package com.github.marenwynn.waypoints.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.marenwynn.waypoints.SelectionManager;
import com.github.marenwynn.waypoints.data.DataManager;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;

public class WPMoveCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        Waypoint wp = SelectionManager.getManager().getSelectedWaypoint(p);

        if (wp == null) {
            Msg.WP_NOT_SELECTED_ERROR.sendTo(p);
            Msg.WP_NOT_SELECTED_ERROR_USAGE.sendTo(p);
            return;
        }

        wp.setLocation(p.getLocation());
        DataManager.getManager().saveWaypoint(sender, wp);
        Msg.WP_LOCATION_UPDATED.sendTo(p, wp.getName());
    }

    @Override
    public boolean isConsoleExecutable() {
        return false;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.move");
    }

}
