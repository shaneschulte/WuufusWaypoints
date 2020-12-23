package com.github.jarada.waypoints.commands;

import com.github.jarada.waypoints.SelectionManager;
import com.github.jarada.waypoints.WaypointManager;
import com.github.jarada.waypoints.data.Category;
import com.github.jarada.waypoints.data.DataManager;
import com.github.jarada.waypoints.data.Msg;
import com.github.jarada.waypoints.data.Waypoint;
import org.bukkit.command.CommandSender;

import java.util.List;

public class WPCatOrderCmd extends WPCatCmd {

    @Override
    public void execute(CommandSender sender, String[] args) {
        WaypointManager wm = WaypointManager.getManager();

        Waypoint wp = getSelectedWaypoint(wm, sender);
        if (wp == null)
            return;

        if (args.length == 0) {
            Msg.USAGE_WP_CATORDER.sendTo(sender);
            return;
        }
        if (wp.getCategory() != null) {
            Category category = wm.getCategoryFromUUID(wp.getCategory());
            try {
                int order = Integer.parseInt(args[0]);
                category.setOrder(order);
                category.setOrderSet(true);
                wm.sortCategories();
                DataManager.getManager().saveWaypoints();
                Msg.CAT_REORDERED.sendTo(sender, category.getName(), category.getOrder());
            } catch (NumberFormatException e) {
                Msg.USAGE_WP_CATORDER.sendTo(sender);
            }
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
        return sender.hasPermission("wp.category.order");
    }
}
