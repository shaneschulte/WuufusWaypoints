package com.github.marenwynn.waypoints.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.marenwynn.waypoints.PluginMain;
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

        if (pm.getData().getAllWaypoints().containsValue(wp))
            pm.getData().removeWaypoint(wp);
        else
            pm.getData().removeWaypointForPlayer(((Player) sender).getUniqueId(), wp);

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
