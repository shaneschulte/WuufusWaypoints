package com.github.marenwynn.waypoints.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.marenwynn.waypoints.PluginMain;
import com.github.marenwynn.waypoints.Selections;
import com.github.marenwynn.waypoints.data.Data;
import com.github.marenwynn.waypoints.data.Msg;

public class WPReloadCmd implements PluginCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        PluginMain.instance.reloadConfig();
        Data.kill();
        Data.init();

        for (Player p : Bukkit.getOnlinePlayers())
            Selections.clearSelectedWaypoint(p);

        Selections.clearSelectedWaypoint(Bukkit.getConsoleSender());
        Msg.RELOADED.sendTo(sender);
        return true;
    }

    @Override
    public boolean isConsoleExecutable() {
        return true;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.reload");
    }

}
