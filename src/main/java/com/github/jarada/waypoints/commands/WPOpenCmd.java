package com.github.jarada.waypoints.commands;

import com.github.jarada.waypoints.WaypointManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WPOpenCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        WaypointManager.getManager().openWaypointMenu((Player) sender,
                null, true, true, false);
    }

    @Override
    public boolean isConsoleExecutable() {
        return false;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.open");
    }
}
