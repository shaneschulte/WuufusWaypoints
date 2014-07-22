package com.github.marenwynn.waypoints.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.marenwynn.waypoints.data.Msg;

public class SetSpawnCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        Location loc = p.getLocation();

        p.getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        Msg.SET_SPAWN.sendTo(p, p.getWorld().getName());
    }

    @Override
    public boolean isConsoleExecutable() {
        return false;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.setspawn");
    }

}
