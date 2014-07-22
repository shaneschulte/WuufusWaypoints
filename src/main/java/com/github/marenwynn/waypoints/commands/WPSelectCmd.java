package com.github.marenwynn.waypoints.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.marenwynn.waypoints.SelectionManager;
import com.github.marenwynn.waypoints.Util;
import com.github.marenwynn.waypoints.WaypointManager;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;

public class WPSelectCmd implements PluginCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        WaypointManager wm = WaypointManager.getManager();

        if (args.length < 2) {
            if (sender instanceof Player) {
                wm.openWaypointSelectionMenu((Player) sender);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(Msg.WP_LIST.toString());

                Waypoint[] waypoints = wm.getAllWaypoints().toArray(new Waypoint[wm.getAllWaypoints().size()]);
                Waypoint selectedWaypoint = SelectionManager.getManager().getSelectedWaypoint(sender);

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
        Waypoint wp = wm.getWaypoint(waypointName);

        if (wp == null) {
            Msg.WP_NOT_EXIST.sendTo(sender, waypointName);
            return true;
        }

        if (sender instanceof Player && !Util.hasAccess((Player) sender, wp, true)) {
            Msg.NO_PERMS.sendTo(sender);
            return true;
        }

        SelectionManager.getManager().setSelectedWaypoint(sender, wp);
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
