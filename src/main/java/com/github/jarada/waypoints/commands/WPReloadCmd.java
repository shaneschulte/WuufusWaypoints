package com.github.jarada.waypoints.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.jarada.waypoints.SelectionManager;
import com.github.jarada.waypoints.data.DataManager;
import com.github.jarada.waypoints.data.Msg;

public class WPReloadCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        DataManager.getManager().reload();

        for (Player p : Bukkit.getOnlinePlayers())
            SelectionManager.getManager().clearSelectedWaypoint(p);

        SelectionManager.getManager().clearSelectedWaypoint(Bukkit.getConsoleSender());
        Msg.RELOADED.sendTo(sender);
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
