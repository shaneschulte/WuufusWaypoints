package com.github.marenwynn.waypoints.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.marenwynn.waypoints.PluginMain;
import com.github.marenwynn.waypoints.Util;
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
                Waypoint selectedWaypoint = pm.getSelectedWaypoint(p.getName());

                if (selectedWaypoint != null)
                    Msg.WP_SELECTED.sendTo(p, selectedWaypoint.getName());
                else
                    selectedWaypoint = new Waypoint("Bed", p.getLocation());

                pm.openWaypointMenu(p, selectedWaypoint, true, true, true);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(Msg.WP_LIST.toString());

                Waypoint[] waypoints = pm.getData().getAllWaypoints().values()
                        .toArray(new Waypoint[pm.getData().getAllWaypoints().size()]);

                Waypoint selectedWaypoint = pm.getSelectedWaypoint(sender.getName());

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
        Waypoint wp = pm.getData().getWaypoint(waypointName);

        if (wp == null || !sender.hasPermission("waypoints.access." + Util.getKey(wp.getName()))) {
            Msg.WP_NOT_EXIST.sendTo(sender, waypointName);
            return true;
        }

        pm.setSelectedWaypoint(sender, wp);
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
