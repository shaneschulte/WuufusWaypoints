package com.github.marenwynn.waypoints;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.FixedMetadataValue;
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
import com.github.marenwynn.waypoints.data.Data;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.PlayerData;
import com.github.marenwynn.waypoints.data.Waypoint;
import com.github.marenwynn.waypoints.listeners.PlayerListener;
import com.github.marenwynn.waypoints.listeners.WaypointListener;
import com.github.marenwynn.waypoints.tasks.TeleportTask;

public class PluginMain extends JavaPlugin {

    private HashMap<String, PluginCommand> commands;

    @Override
    public void onEnable() {
        saveResource("CHANGELOG.txt", true);
        Data.init(this);
        Selections.init();

        commands = new HashMap<String, PluginCommand>();

        commands.put("sethome", new SetHomeCmd(this));
        commands.put("setspawn", new SetSpawnCmd());
        commands.put("add", new WPAddCmd());
        commands.put("desc", new WPDescCmd());
        commands.put("discover", new WPDiscoverCmd());
        commands.put("icon", new WPIconCmd());
        commands.put("move", new WPMoveCmd());
        commands.put("reload", new WPReloadCmd(this));
        commands.put("remove", new WPRemoveCmd());
        commands.put("rename", new WPRenameCmd());
        commands.put("select", new WPSelectCmd(this));
        commands.put("toggle", new WPToggleCmd());

        getCommand("wp").setExecutor(this);
        getCommand("sethome").setExecutor(this);

        getServer().getPluginManager().registerEvents(new WaypointListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        Data.kill();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();
        PluginCommand executor = null;

        if (cmd.equals("wp")) {
            if (args.length > 0) {
                String key = args[0].toLowerCase();

                if (commands.containsKey(key))
                    executor = commands.get(key);
            }

            if (executor == null) {
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    ArrayList<Waypoint> playerPoints = Data.getPlayerData(p.getUniqueId()).getAllWaypoints();
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
            executor = commands.get("sethome");
        } else if (cmd.equals("setspawn")) {
            executor = commands.get("setspawn");
        }

        if (!(sender instanceof Player) && !executor.isConsoleExecutable()) {
            Msg.CMD_NO_CONSOLE.sendTo(sender);
            return true;
        }

        if (!executor.hasRequiredPerm(sender)) {
            Msg.NO_PERMS.sendTo(sender);
            return true;
        }

        return executor.onCommand(sender, command, label, args);
    }

    public void teleportPlayer(Player p, Waypoint wp) {
        new TeleportTask(this, p, wp).runTask(this);
    }

    public void openWaypointMenu(Player p, Waypoint currentWaypoint, boolean addServerWaypoints,
            boolean addHomeWaypoints, boolean select) {
        if (p.hasMetadata("InMenu"))
            return;

        ArrayList<Waypoint> accessList = new ArrayList<Waypoint>();

        if (!select) {
            if (currentWaypoint != null) {
                Msg.OPEN_WP_MENU.sendTo(p, currentWaypoint.getName());
                p.teleport(currentWaypoint.getLocation(), TeleportCause.PLUGIN);
            } else {
                Msg.REMOTELY_ACCESSED.sendTo(p);
            }

            if (p.hasPermission("wp.access.spawn")) {
                if (currentWaypoint != null && currentWaypoint.getName().equals("Spawn")) {
                    accessList.add(currentWaypoint);
                } else {
                    Waypoint spawn = new Waypoint("Spawn", p.getWorld().getSpawnLocation());
                    spawn.setIcon(Material.NETHER_STAR);
                    accessList.add(spawn);
                }
            }

            if (p.hasPermission("wp.access.bed") && p.getBedSpawnLocation() != null) {
                Waypoint bed = new Waypoint("Bed", p.getBedSpawnLocation());
                bed.setIcon(Material.BED);
                accessList.add(bed);
            }
        }

        PlayerData pd = Data.getPlayerData(p.getUniqueId());

        if (addServerWaypoints && (!select || p.hasPermission("wp.admin")))
            for (Waypoint wp : Data.getAllWaypoints())
                if (hasAccess(p, wp, select))
                    accessList.add(wp);

        if (addHomeWaypoints)
            for (Waypoint wp : pd.getAllWaypoints())
                accessList.add(wp);

        p.setMetadata("InMenu", new FixedMetadataValue(this, true));
        new WaypointMenu(this, p, currentWaypoint, accessList, select).open();
    }

    public boolean setHome(Player p, String waypointName) {
        PlayerData pd = Data.getPlayerData(p.getUniqueId());
        Location playerLoc = p.getLocation();

        for (Waypoint wp : pd.getAllWaypoints()) {
            if (Util.isSameLoc(playerLoc, wp.getLocation(), true)) {
                Msg.HOME_WP_ALREADY_HERE.sendTo(p, wp.getName());
                return false;
            }

            if (waypointName.equals(wp.getName()) || waypointName.equals("Bed") || waypointName.equals("Spawn")) {
                Msg.WP_DUPLICATE_NAME.sendTo(p, waypointName);
                return false;
            }
        }

        Waypoint wp = new Waypoint(waypointName, playerLoc);
        wp.setDescription(Msg.SETHOME_DEFAULT_DESC.toString());

        Waypoint replaced = pd.addWaypoint(wp);
        Data.savePlayerData(p.getUniqueId());

        if (replaced != null)
            Msg.HOME_WP_REPLACED.sendTo(p, replaced.getName(), wp.getName());
        else
            Msg.HOME_WP_CREATED.sendTo(p, waypointName);

        return true;
    }

    public boolean hasAccess(Player p, Waypoint wp, boolean select) {
        if (p.hasPermission("wp.access." + Util.getKey(wp.getName())))
            return true;

        if (wp.isDiscoverable() != null && Data.getPlayerData(p.getUniqueId()).hasDiscovered(wp.getUUID()))
            return wp.isDiscoverable() ? true : (!select
                    && !p.getWorld().getName().equals(wp.getLocation().getWorld().getName()) ? false : true);

        return false;
    }

}
