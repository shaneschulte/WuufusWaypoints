package com.github.marenwynn.waypoints.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.marenwynn.waypoints.PluginMain;
import com.github.marenwynn.waypoints.data.Msg;

public class WPReloadCmd implements PluginCommand {

    private PluginMain pm;

    public WPReloadCmd(PluginMain pm) {
        this.pm = pm;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        pm.reloadConfig();
        pm.getData().loadConfig();

        for (Player p : pm.getServer().getOnlinePlayers())
            pm.clearSelectedWaypoint(p.getName());

        pm.clearSelectedWaypoint("CONSOLE");
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
