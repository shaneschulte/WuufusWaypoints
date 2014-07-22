package com.github.marenwynn.waypoints.commands;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.marenwynn.waypoints.SelectionManager;
import com.github.marenwynn.waypoints.WaypointManager;
import com.github.marenwynn.waypoints.data.DataManager;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;

public class WPRemoveCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        WaypointManager wm = WaypointManager.getManager();
        Waypoint wp = SelectionManager.getManager().getSelectedWaypoint(sender);

        if (wp == null) {
            Msg.WP_NOT_SELECTED_ERROR.sendTo(sender);
            Msg.WP_NOT_SELECTED_ERROR_USAGE.sendTo(sender);
            return;
        }

        if (wm.getWaypoint(wp.getName()) != null) {
            wm.removeWaypoint(wp);
            DataManager.getManager().saveWaypoints();
        } else {
            UUID playerUUID = ((Player) sender).getUniqueId();
            wm.getPlayerData(playerUUID).removeWaypoint(wp);
            DataManager.getManager().savePlayerData(playerUUID);
        }

        SelectionManager.getManager().clearSelectionsWith(wp);
        Msg.WP_REMOVED.sendTo(sender, wp.getName());
    }

    @Override
    public boolean isConsoleExecutable() {
        return true;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.remove");
    }

}
