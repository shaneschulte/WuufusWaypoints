package com.github.marenwynn.waypoints.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.github.marenwynn.waypoints.SelectionManager;
import com.github.marenwynn.waypoints.data.DataManager;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;

public class WPIconCmd implements PluginCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Waypoint wp = SelectionManager.getManager().getSelectedWaypoint(sender);

        if (wp == null) {
            Msg.WP_NOT_SELECTED_ERROR.sendTo(sender);
            Msg.WP_NOT_SELECTED_ERROR_USAGE.sendTo(sender);
            return true;
        }

        if (args.length < 2) {
            Msg.USAGE_SETICON.sendTo(sender);
            return true;
        }

        short durability = (short) 0;
        String[] input = args[1].split(":");
        Material icon = Material.matchMaterial(input[0]);

        if (icon == null) {
            Msg.INVALID_MATERIAL.sendTo(sender);
            return true;
        }

        if (input.length > 1) {
            try {
                durability = Short.parseShort(input[1]);
            } catch (NumberFormatException e) {
                Msg.INVALD_DURABILITY.sendTo(sender);
                return true;
            }
        }

        wp.setIcon(icon);
        wp.setDurability(durability);
        DataManager.getManager().saveWaypoint(sender, wp);
        Msg.WP_SETICON.sendTo(sender, wp.getName(), icon.toString(), durability);
        return true;
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
