package com.github.marenwynn.waypoints.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.marenwynn.waypoints.Selections;
import com.github.marenwynn.waypoints.Util;
import com.github.marenwynn.waypoints.WaypointManager;
import com.github.marenwynn.waypoints.data.Data;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.PlayerData;
import com.github.marenwynn.waypoints.data.Waypoint;

public class WPRenameCmd implements PluginCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Waypoint wp = Selections.getSelectedWaypoint(sender);

        if (wp == null) {
            Msg.WP_NOT_SELECTED_ERROR.sendTo(sender);
            Msg.WP_NOT_SELECTED_ERROR_USAGE.sendTo(sender);
            return true;
        }

        if (args.length < 2) {
            Msg.USAGE_WP_RENAME.sendTo(sender);
            return true;
        }

        String waypointName = Util.color(Util.buildString(args, 1, ' '));

        if (ChatColor.stripColor(waypointName).length() > Data.WP_NAME_MAX_LENGTH) {
            Msg.MAX_LENGTH_EXCEEDED.sendTo(sender, Data.WP_NAME_MAX_LENGTH);
            return true;
        }

        if (waypointName.equals("Bed") || waypointName.equals("Spawn")) {
            Msg.WP_DUPLICATE_NAME.sendTo(sender, waypointName);
            return true;
        }

        boolean serverDefined = WaypointManager.getAllWaypoints().contains(wp);

        if (serverDefined) {
            if (WaypointManager.getWaypoint(waypointName) != null) {
                Msg.WP_DUPLICATE_NAME.sendTo(sender, waypointName);
                return true;
            }

            WaypointManager.removeWaypoint(wp);
        } else {
            PlayerData pd = WaypointManager.getPlayerData(((Player) sender).getUniqueId());

            if (pd.getWaypoint(waypointName) != null) {
                Msg.WP_DUPLICATE_NAME.sendTo(sender, waypointName);
                return true;
            }

            pd.removeWaypoint(wp);
        }

        String oldName = wp.getName();
        wp.setName(waypointName);

        if (serverDefined)
            WaypointManager.addWaypoint(wp);
        else
            WaypointManager.getPlayerData(((Player) sender).getUniqueId()).addWaypoint(wp);

        Data.saveWaypoint(sender, wp);
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
