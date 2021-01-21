package com.github.jarada.waypoints.commands;

import com.github.jarada.waypoints.data.DataManager;
import com.github.jarada.waypoints.data.Msg;
import com.github.jarada.waypoints.listeners.BeaconListener;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WPBeaconPermanentCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        DataManager dm = DataManager.getManager();
        Player p = (Player) sender;

        if (dm.ENABLE_BEACON && dm.BEACON_UNLIMITED_PERMANENT &&
                p.hasPermission("wp.beacon.unlimited")) {
            BeaconListener.getListener().assignBeacon(p);
        }
    }

    @Override
    public boolean isConsoleExecutable() {
        return false;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.spawn.beacon");
    }
    
}
