package com.github.jarada.waypoints.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import com.github.jarada.waypoints.SelectionManager;
import com.github.jarada.waypoints.data.DataManager;
import com.github.jarada.waypoints.data.Msg;
import com.github.jarada.waypoints.data.Waypoint;

public class WPIconCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        Waypoint wp = SelectionManager.getManager().getSelectedWaypoint(sender);

        if (wp == null) {
            Msg.WP_NOT_SELECTED_ERROR.sendTo(sender);
            Msg.WP_NOT_SELECTED_ERROR_USAGE.sendTo(sender);
            return;
        }

        if (args.length == 0) {
            Msg.USAGE_SETICON.sendTo(sender);
            return;
        }

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
                Msg.INVALD_DURABILITY.sendTo(sender);
                return;
            }
        }

        wp.setIcon(icon);
        wp.setDurability(durability);
        DataManager.getManager().saveWaypoint(sender, wp);
        Msg.WP_SETICON.sendTo(sender, wp.getName(), icon.toString(), durability);
    }

    @Override
    public boolean isConsoleExecutable() {
        return true;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.icon");
    }

}
