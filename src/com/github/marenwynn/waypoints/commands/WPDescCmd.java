package com.github.marenwynn.waypoints.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.github.marenwynn.waypoints.PluginMain;
import com.github.marenwynn.waypoints.data.Data;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;

public class WPDescCmd implements PluginCommand {

    private PluginMain pm;

    public WPDescCmd(PluginMain pm) {
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

        if (args.length < 2) {
            wp.setDescription("");
            Data.saveWaypoints();
            Msg.WP_DESC_CLEARED.sendTo(sender, wp.getName());
            return true;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 1; i < args.length; i++) {
            sb.append(args[i]);

            if (i < args.length - 1)
                sb.append(" ");
        }

        String desc = sb.toString();

        if (desc.length() > Data.WP_DESC_MAX_LENGTH) {
            Msg.MAX_LENGTH_EXCEEDED.sendTo(sender, Data.WP_DESC_MAX_LENGTH);
            return true;
        }

        wp.setDescription(desc);
        Data.saveWaypoint(sender, wp);
        Msg.WP_DESC_UPDATED.sendTo(sender, wp.getName());
        return true;
    }

    @Override
    public boolean isConsoleExecutable() {
        return true;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.desc");
    }

}
