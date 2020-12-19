package com.github.jarada.waypoints.commands;

import com.github.jarada.waypoints.data.DataManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WPBeaconCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        DataManager dm = DataManager.getManager();
        Player p = (Player) sender;

        ItemStack beacon = dm.BEACON;
        p.getInventory().addItem(beacon);
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
