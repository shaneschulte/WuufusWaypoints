package com.github.jarada.waypoints.commands;

import com.github.jarada.waypoints.SelectionManager;
import com.github.jarada.waypoints.WaypointManager;
import com.github.jarada.waypoints.data.Category;
import com.github.jarada.waypoints.data.DataManager;
import com.github.jarada.waypoints.data.Msg;
import com.github.jarada.waypoints.data.Waypoint;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

public class WPCatIconCmd extends WPCatCmd {

    @Override
    public void execute(CommandSender sender, String[] args) {
        WaypointManager wm = WaypointManager.getManager();
        Waypoint wp = getSelectedWaypoint(wm, sender);
        if (wp == null)
            return;

        if (args.length == 0) {
            Msg.USAGE_SETICON.sendTo(sender);
            return;
        }

        if (wp.getCategory() != null) {
            Category category = wm.getCategoryFromUUID(wp.getCategory());

            short durability = (short) 0;
            String[] input = args[0].split(":");
            Material icon = Material.matchMaterial(input[0]);

            if (icon == null) {
                Msg.INVALID_MATERIAL.sendTo(sender);
                return;
            }

            if (input.length > 1) {
                try {
                    durability = Short.parseShort(input[1]);
                } catch (NumberFormatException e) {
                    Msg.INVALID_DURABILITY.sendTo(sender);
                    return;
                }
            }

            category.setIcon(icon);
            category.setDurability(durability);
            DataManager.getManager().saveWaypoint(sender, wp);
            Msg.WP_CATEGORY_SETICON.sendTo(sender, wp.getName(), icon.toString(), durability);
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
        return sender.hasPermission("wp.category.icon");
    }

}
