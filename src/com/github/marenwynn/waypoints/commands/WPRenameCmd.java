package com.github.marenwynn.waypoints.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.marenwynn.waypoints.PluginMain;
import com.github.marenwynn.waypoints.Util;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;

public class WPRenameCmd implements PluginCommand {

    private PluginMain pm;

    public WPRenameCmd(PluginMain pm) {
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

        if (args.length < 2) {
            Msg.USAGE_WP_RENAME.sendTo(sender);
            return true;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 1; i < args.length; i++) {
            sb.append(args[i]);

            if (i < args.length - 1)
                sb.append(" ");
        }

        String waypointName = Util.color(sb.toString());

        if (ChatColor.stripColor(waypointName).length() > pm.getData().WP_NAME_MAX_LENGTH) {
            Msg.MAX_LENGTH_EXCEEDED.sendTo(sender, pm.getData().WP_NAME_MAX_LENGTH);
            return true;
        }

        if (waypointName.equals("Bed") || waypointName.equals("Spawn")) {
            Msg.WP_DUPLICATE_NAME.sendTo(sender, waypointName);
            return true;
        }

        boolean serverDefined = pm.getData().getAllWaypoints().containsValue(wp);

        if (serverDefined) {
            if (pm.getData().getWaypoint(waypointName) != null) {
                Msg.WP_DUPLICATE_NAME.sendTo(sender, waypointName);
                return true;
            }

            pm.getData().removeWaypoint(wp);
        } else {
            for (Waypoint home : pm.getData().getWaypointsForPlayer(((Player) sender).getUniqueId())) {
                if (waypointName.equals(home.getName())) {
                    Msg.WP_DUPLICATE_NAME.sendTo(sender, waypointName);
                    return true;
                }
            }

            pm.getData().removeWaypointForPlayer(((Player) sender).getUniqueId(), wp);
        }

        String oldName = wp.getName();
        wp.setName(waypointName);

        if (serverDefined)
            pm.getData().addWaypoint(wp);
        else
            pm.getData().addWaypointForPlayer(((Player) sender).getUniqueId(), wp);

        Msg.WP_RENAMED.sendTo(sender, oldName, waypointName);
        return true;
    }

    @Override
    public boolean isConsoleExecutable() {
        return true;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.rename");
    }

}
