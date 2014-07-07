package com.github.marenwynn.waypoints.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.marenwynn.waypoints.PluginMain;
import com.github.marenwynn.waypoints.data.Data;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;

public class WPMoveCmd implements PluginCommand {

    private PluginMain pm;

    public WPMoveCmd(PluginMain pm) {
        this.pm = pm;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;
        Waypoint wp = pm.getSelectedWaypoint(p.getName());

        if (wp == null) {
            Msg.WP_NOT_SELECTED_ERROR.sendTo(p);
            Msg.WP_NOT_SELECTED_ERROR_USAGE.sendTo(p);
            return true;
        }

        wp.setLocation(p.getLocation());
        Data.saveWaypoint(sender, wp);
        Msg.WP_LOCATION_UPDATED.sendTo(p, wp.getName());
        return true;
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
