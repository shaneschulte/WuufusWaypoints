package com.github.marenwynn.waypoints;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.marenwynn.waypoints.commands.PluginCommand;
import com.github.marenwynn.waypoints.commands.SetHomeCmd;
import com.github.marenwynn.waypoints.commands.SetSpawnCmd;
import com.github.marenwynn.waypoints.commands.WPAddCmd;
import com.github.marenwynn.waypoints.commands.WPDescCmd;
import com.github.marenwynn.waypoints.commands.WPDiscoverCmd;
import com.github.marenwynn.waypoints.commands.WPIconCmd;
import com.github.marenwynn.waypoints.commands.WPMoveCmd;
import com.github.marenwynn.waypoints.commands.WPReloadCmd;
import com.github.marenwynn.waypoints.commands.WPRemoveCmd;
import com.github.marenwynn.waypoints.commands.WPRenameCmd;
import com.github.marenwynn.waypoints.commands.WPSelectCmd;
import com.github.marenwynn.waypoints.commands.WPToggleCmd;
import com.github.marenwynn.waypoints.data.DataManager;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;
import com.github.marenwynn.waypoints.listeners.PlayerListener;
import com.github.marenwynn.waypoints.listeners.WaypointListener;

public class PluginMain extends JavaPlugin {

    private static PluginMain          instance;
    private Map<String, PluginCommand> commands;

    public static PluginMain getPluginInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveResource("CHANGELOG.txt", true);
        DataManager.getManager().loadWaypoints();

        commands = new HashMap<String, PluginCommand>();
        commands.put("sethome", new SetHomeCmd());
        commands.put("setspawn", new SetSpawnCmd());
        commands.put("add", new WPAddCmd());
        commands.put("desc", new WPDescCmd());
        commands.put("discover", new WPDiscoverCmd());
        commands.put("icon", new WPIconCmd());
        commands.put("move", new WPMoveCmd());
        commands.put("reload", new WPReloadCmd());
        commands.put("remove", new WPRemoveCmd());
        commands.put("rename", new WPRenameCmd());
        commands.put("select", new WPSelectCmd());
        commands.put("toggle", new WPToggleCmd());

        getCommand("wp").setExecutor(this);
        getCommand("sethome").setExecutor(this);
        getCommand("setspawn").setExecutor(this);
        getServer().getPluginManager().registerEvents(new WaypointListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        DataManager.getManager().saveWaypoints();

        for (Player p : getServer().getOnlinePlayers())
            DataManager.getManager().savePlayerData(p.getUniqueId());

        instance = null;
        commands = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();
        PluginCommand pluginCmd = null;
        String[] param = null;

        if (cmd.equals("wp")) {
            param = args.length > 0 ? Arrays.copyOfRange(args, 1, args.length) : null;

            if (args.length > 0) {
                String key = args[0].toLowerCase();

                if (commands.containsKey(key))
                    pluginCmd = commands.get(key);
            }

            if (pluginCmd == null) {
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    List<Waypoint> playerPoints = WaypointManager.getManager().getPlayerData(p.getUniqueId())
                            .getAllWaypoints();
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < playerPoints.size(); i++) {
                        sb.append(playerPoints.get(i).getName());

                        if (i < playerPoints.size() - 1)
                            sb.append("&6, ");
                    }

                    if (sb.length() > 0)
                        Msg.LIST_HOME_WAYPOINTS.sendTo(sender, sb.toString());
                }

                sender.sendMessage("Maren's Waypoints v" + getDescription().getVersion() + " by Marenwynn.");
                return false;
            }
        } else if (cmd.equals("sethome")) {
            pluginCmd = commands.get("sethome");
            param = args;
        } else if (cmd.equals("setspawn")) {
            pluginCmd = commands.get("setspawn");
        }

        if (!(sender instanceof Player) && !pluginCmd.isConsoleExecutable()) {
            Msg.CMD_NO_CONSOLE.sendTo(sender);
            return true;
        }

        if (!pluginCmd.hasRequiredPerm(sender)) {
            Msg.NO_PERMS.sendTo(sender);
            return true;
        }

        pluginCmd.execute(sender, param);
        return true;
    }

}
