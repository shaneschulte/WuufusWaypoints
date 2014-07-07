package com.github.marenwynn.waypoints.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.marenwynn.waypoints.PluginMain;
import com.github.marenwynn.waypoints.Util;
import com.github.marenwynn.waypoints.data.Data;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;

public class WPAddCmd implements PluginCommand {

    private PluginMain pm;

    public WPAddCmd(PluginMain pm) {
        this.pm = pm;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;

        if (args.length < 2) {
            Msg.USAGE_WP_ADD.sendTo(p);
            return true;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 1; i < args.length; i++) {
            sb.append(args[i]);

            if (i < args.length - 1)
                sb.append(" ");
        }

        String waypointName = Util.color(sb.toString());

        if (ChatColor.stripColor(waypointName).length() > Data.WP_NAME_MAX_LENGTH) {
            Msg.MAX_LENGTH_EXCEEDED.sendTo(p, Data.WP_NAME_MAX_LENGTH);
            return true;
        }

        if (Data.getWaypoint(waypointName) != null || waypointName.equals("Bed") || waypointName.equals("Spawn")) {
            Msg.WP_DUPLICATE_NAME.sendTo(p, waypointName);
            return true;
        }

        Location playerLoc = p.getLocation();

        for (Waypoint wp : Data.getAllWaypoints()) {
            Location waypointLoc = wp.getLocation();

            if (Util.isSameLoc(playerLoc, waypointLoc, true)) {
                Msg.WP_ALREADY_HERE.sendTo(p, wp.getName());
                return true;
            }
        }

        Waypoint wp = new Waypoint(waypointName, playerLoc);

        Data.addWaypoint(wp);
        Data.saveWaypoints();
        pm.setSelectedWaypoint(sender, wp);
        return true;
    }

    @Override
    public boolean isConsoleExecutable() {
        return false;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.add");
    }

}
