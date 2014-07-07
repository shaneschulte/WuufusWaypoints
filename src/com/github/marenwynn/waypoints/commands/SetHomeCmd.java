package com.github.marenwynn.waypoints.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.marenwynn.waypoints.PluginMain;
import com.github.marenwynn.waypoints.Util;
import com.github.marenwynn.waypoints.data.Data;
import com.github.marenwynn.waypoints.data.Msg;

public class SetHomeCmd implements PluginCommand {

    private PluginMain pm;

    public SetHomeCmd(PluginMain pm) {
        this.pm = pm;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;

        if (args.length < 1) {
            Msg.USAGE_SETHOME.sendTo(p);
            return true;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);

            if (i < args.length - 1)
                sb.append(" ");
        }

        String waypointName = Util.color(sb.toString());

        if (ChatColor.stripColor(waypointName).length() > Data.WP_NAME_MAX_LENGTH) {
            Msg.MAX_LENGTH_EXCEEDED.sendTo(p, Data.WP_NAME_MAX_LENGTH);
            return true;
        }

        pm.setHome(p, waypointName);
        return true;
    }

    @Override
    public boolean isConsoleExecutable() {
        return false;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.sethome");
    }

}
