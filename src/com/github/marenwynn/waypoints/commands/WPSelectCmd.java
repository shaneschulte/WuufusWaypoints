package com.github.marenwynn.waypoints.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.marenwynn.waypoints.PluginMain;
import com.github.marenwynn.waypoints.Selections;
import com.github.marenwynn.waypoints.Util;
import com.github.marenwynn.waypoints.data.Data;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;

public class WPSelectCmd implements PluginCommand {

    private PluginMain pm;

    public WPSelectCmd(PluginMain pm) {
        this.pm = pm;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                Waypoint selectedWaypoint = Selections.getSelectedWaypoint(p);

                if (selectedWaypoint != null)
                    Msg.WP_SELECTED.sendTo(p, selectedWaypoint.getName());
                else
                    selectedWaypoint = new Waypoint("Bed", p.getLocation());

                pm.openWaypointMenu(p, selectedWaypoint, true, true, true);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(Msg.WP_LIST.toString());

                Waypoint[] waypoints = Data.getAllWaypoints().toArray(new Waypoint[Data.getAllWaypoints().size()]);
                Waypoint selectedWaypoint = Selections.getSelectedWaypoint(sender);

                for (int i = 0; i < waypoints.length; i++) {
                    if (waypoints[i] == selectedWaypoint)
                        sb.append("&a* &6");

                    sb.append(waypoints[i].getName());

                    if (i < waypoints.length - 1)
                        sb.append("&6, ");
                }

                if (waypoints.length == 0)
                    sb.append(Msg.NO_WAYPOINTS.toString());

                sender.sendMessage(Util.color(sb.toString()));
            }

            return true;
        }

        if (!sender.hasPermission("wp.admin")) {
            Msg.NO_PERMS.sendTo(sender);
            return true;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 1; i < args.length; i++) {
            sb.append(args[i]);

            if (i < args.length - 1)
                sb.append(" ");
        }

        String waypointName = Util.color(sb.toString());
        Waypoint wp = Data.getWaypoint(waypointName);

        if (wp == null) {
            Msg.WP_NOT_EXIST.sendTo(sender, waypointName);
            return true;
        }

        if (sender instanceof Player && !pm.hasAccess((Player) sender, wp, true)) {
            Msg.NO_PERMS.sendTo(sender);
            return true;
        }

        Selections.setSelectedWaypoint(sender, wp);
        return true;
    }

    @Override
    public boolean isConsoleExecutable() {
        return true;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.select");
    }

}
