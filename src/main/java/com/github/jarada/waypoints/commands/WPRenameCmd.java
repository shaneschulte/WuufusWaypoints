package com.github.jarada.waypoints.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.jarada.waypoints.SelectionManager;
import com.github.jarada.waypoints.Util;
import com.github.jarada.waypoints.WaypointManager;
import com.github.jarada.waypoints.data.DataManager;
import com.github.jarada.waypoints.data.Msg;
import com.github.jarada.waypoints.data.PlayerData;
import com.github.jarada.waypoints.data.Waypoint;

public class WPRenameCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        WaypointManager wm = WaypointManager.getManager();
        Waypoint wp = SelectionManager.getManager().getSelectedWaypoint(sender);

        if (wp == null) {
            Msg.WP_NOT_SELECTED_ERROR.sendTo(sender);
            Msg.WP_NOT_SELECTED_ERROR_USAGE.sendTo(sender);
            return;
        }

        if (args.length == 0) {
            Msg.USAGE_WP_RENAME.sendTo(sender);
            return;
        }

        String newWaypointName = Util.color(Util.buildString(args, 0, ' '));
        int maxLength = DataManager.getManager().WP_NAME_MAX_LENGTH;

        if (ChatColor.stripColor(newWaypointName).length() > maxLength) {
            Msg.MAX_LENGTH_EXCEEDED.sendTo(sender, maxLength);
            return;
        }

        if (newWaypointName.equals("Bed") || newWaypointName.equals("Spawn")) {
            Msg.WP_DUPLICATE_NAME.sendTo(sender, newWaypointName);
            return;
        }

        boolean serverDefined = wm.isServerDefined(wp);

        if (serverDefined) {
            if (wm.getWaypoint(newWaypointName) != null) {
                Msg.WP_DUPLICATE_NAME.sendTo(sender, newWaypointName);
                return;
            }

            wm.removeWaypoint(wp);
        } else {
            PlayerData pd = wm.getPlayerData(((Player) sender).getUniqueId());

            if (pd.getWaypoint(newWaypointName) != null) {
                Msg.WP_DUPLICATE_NAME.sendTo(sender, newWaypointName);
                return;
            }

            pd.removeWaypoint(wp);
        }

        String oldName = wp.getName();
        wp.setName(newWaypointName);

        if (serverDefined)
            wm.addWaypoint(wp);
        else
            wm.getPlayerData(((Player) sender).getUniqueId()).addWaypoint(wp);

        DataManager.getManager().saveWaypoint(sender, wp);
        Msg.WP_RENAMED.sendTo(sender, oldName, newWaypointName);
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