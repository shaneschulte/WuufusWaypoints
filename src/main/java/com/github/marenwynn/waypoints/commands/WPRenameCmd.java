package com.github.marenwynn.waypoints.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.marenwynn.waypoints.SelectionManager;
import com.github.marenwynn.waypoints.Util;
import com.github.marenwynn.waypoints.WaypointManager;
import com.github.marenwynn.waypoints.data.DataManager;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.PlayerData;
import com.github.marenwynn.waypoints.data.Waypoint;

public class WPRenameCmd implements PluginCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        WaypointManager wm = WaypointManager.getManager();
        Waypoint wp = SelectionManager.getManager().getSelectedWaypoint(sender);

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
        int maxLength = DataManager.getManager().WP_NAME_MAX_LENGTH;

        if (ChatColor.stripColor(waypointName).length() > maxLength) {
            Msg.MAX_LENGTH_EXCEEDED.sendTo(sender, maxLength);
            return true;
        }

        if (waypointName.equals("Bed") || waypointName.equals("Spawn")) {
            Msg.WP_DUPLICATE_NAME.sendTo(sender, waypointName);
            return true;
        }

        boolean serverDefined = wm.getAllWaypoints().contains(wp);

        if (serverDefined) {
            if (wm.getWaypoint(waypointName) != null) {
                Msg.WP_DUPLICATE_NAME.sendTo(sender, waypointName);
                return true;
            }

            wm.removeWaypoint(wp);
        } else {
            PlayerData pd = wm.getPlayerData(((Player) sender).getUniqueId());

            if (pd.getWaypoint(waypointName) != null) {
                Msg.WP_DUPLICATE_NAME.sendTo(sender, waypointName);
                return true;
            }

            pd.removeWaypoint(wp);
        }

        String oldName = wp.getName();
        wp.setName(waypointName);

        if (serverDefined)
            wm.addWaypoint(wp);
        else
            wm.getPlayerData(((Player) sender).getUniqueId()).addWaypoint(wp);

        DataManager.getManager().saveWaypoint(sender, wp);
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
