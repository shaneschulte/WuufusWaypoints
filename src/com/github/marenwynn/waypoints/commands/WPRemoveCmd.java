package com.github.marenwynn.waypoints.commands;

import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.marenwynn.waypoints.PluginMain;
import com.github.marenwynn.waypoints.data.Data;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;

public class WPRemoveCmd implements PluginCommand {

    private PluginMain pm;

    public WPRemoveCmd(PluginMain pm) {
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

        if (Data.getWaypoint(wp.getName()) != null) {
            Data.removeWaypoint(wp);
            Data.saveWaypoints();
        } else {
            UUID playerUUID = ((Player) sender).getUniqueId();
            Data.getPlayerData(playerUUID).removeWaypoint(wp);
            Data.savePlayerData(playerUUID);
        }

        for (Player p : pm.getServer().getOnlinePlayers()) {
            Waypoint selectedWaypoint = pm.getSelectedWaypoint(p.getName());

            if (selectedWaypoint != null && selectedWaypoint == wp)
                pm.clearSelectedWaypoint(p.getName());
        }

        if (pm.getSelectedWaypoint("CONSOLE") != null && pm.getSelectedWaypoint("CONSOLE") == wp)
            pm.clearSelectedWaypoint("CONSOLE");

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
