package com.github.jarada.waypoints.commands;

import com.github.jarada.waypoints.SelectionManager;
import com.github.jarada.waypoints.WaypointManager;
import com.github.jarada.waypoints.data.Category;
import com.github.jarada.waypoints.data.DataManager;
import com.github.jarada.waypoints.data.Msg;
import com.github.jarada.waypoints.data.Waypoint;
import org.bukkit.command.CommandSender;

public class WPCatUnsetCmd extends WPCatCmd {

    @Override
    public void execute(CommandSender sender, String[] args) {
        WaypointManager wm = WaypointManager.getManager();

        Waypoint wp = getSelectedWaypoint(wm, sender);
        if (wp == null)
            return;

        if (wp.getCategory() != null) {
            Category oldCategory = wm.getCategoryFromUUID(wp.getCategory());
            wm.unrecordWaypointCategory(wp);
            DataManager.getManager().saveWaypoint(sender, wp);
            Msg.WP_CATEGORY_UNSET.sendTo(sender, oldCategory.getName(), wp.getName());
        } else {
            Msg.WP_CATEGORY_EMPTY.sendTo(sender, wp.getName());
        }
    }

    @Override
    public boolean isConsoleExecutable() {
        return true;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.category.unset");
    }

}
