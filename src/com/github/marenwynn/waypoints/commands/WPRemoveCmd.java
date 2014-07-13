package com.github.marenwynn.waypoints.commands;

import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.marenwynn.waypoints.Selections;
import com.github.marenwynn.waypoints.data.Data;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;

public class WPRemoveCmd implements PluginCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Waypoint wp = Selections.getSelectedWaypoint(sender);

        if (wp == null) {
            Msg.WP_NOT_SELECTED_ERROR.sendTo(sender);
            Msg.WP_NOT_SELECTED_ERROR_USAGE.sendTo(sender);
            return true;
        }

        if (Data.getWaypoint(wp.getName()) != null) {
            Data.removeWaypoint(wp);
            Data.saveWaypoints();
        } else {
            UUID playerUUID = ((Player) sender).getUniqueId();
            Data.getPlayerData(playerUUID).removeWaypoint(wp);
            Data.savePlayerData(playerUUID);
        }

        Selections.clearSelectionsWith(wp);
        Msg.WP_REMOVED.sendTo(sender, wp.getName());
        return true;
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
