package com.github.marenwynn.waypoints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.FixedMetadataValue;

import com.github.marenwynn.waypoints.data.Data;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.PlayerData;
import com.github.marenwynn.waypoints.data.Waypoint;

public class WaypointManager {

    public static Map<UUID, PlayerData> players;
    public static Map<String, Waypoint> waypoints;

    public static void init() {
        players = new HashMap<UUID, PlayerData>();
        waypoints = new LinkedHashMap<String, Waypoint>();
    }

    public static void kill() {
        Data.saveWaypoints();

        for (Player p : Bukkit.getOnlinePlayers())
            Data.savePlayerData(p.getUniqueId());

        players = null;
        waypoints = null;
    }

    public static Waypoint getWaypoint(String name) {
        String key = Util.getKey(name);

        if (waypoints.containsKey(key))
            return waypoints.get(key);

        return null;
    }

    public static void addWaypoint(Waypoint wp) {
        waypoints.put(Util.getKey(wp.getName()), wp);
        sortWaypoints();
    }

    public static void removeWaypoint(Waypoint wp) {
        waypoints.remove(Util.getKey(wp.getName()));
        sortWaypoints();
    }

    public static Collection<Waypoint> getAllWaypoints() {
        return waypoints.values();
    }

    public static PlayerData getPlayerData(UUID player) {
        return players.get(player);
    }

    private static void sortWaypoints() {
        List<String> keys = new ArrayList<String>();
        keys.addAll(waypoints.keySet());
        Collections.sort(keys);

        Map<String, Waypoint> sortedWaypoints = new LinkedHashMap<String, Waypoint>();

        for (String key : keys)
            sortedWaypoints.put(key, waypoints.get(key));

        waypoints.clear();
        waypoints.putAll(sortedWaypoints);
    }

    public static void openWaypointMenu(Player p, Waypoint currentWaypoint, boolean addServerWaypoints,
            boolean addHomeWaypoints, boolean select) {
        if (p.hasMetadata("InMenu"))
            return;

        List<Waypoint> accessList = new ArrayList<Waypoint>();

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

        PlayerData pd = getPlayerData(p.getUniqueId());

        if (addServerWaypoints && (!select || p.hasPermission("wp.admin")))
            for (Waypoint wp : getAllWaypoints())
                if (Util.hasAccess(p, wp, select))
                    accessList.add(wp);

        if (addHomeWaypoints)
            for (Waypoint wp : pd.getAllWaypoints())
                accessList.add(wp);

        p.setMetadata("InMenu", new FixedMetadataValue(PluginMain.instance, true));
        new WaypointMenu(p, currentWaypoint, accessList, select).open();
    }

    public static void openWaypointSelectionMenu(Player p) {
        Waypoint selectedWaypoint = Selections.getSelectedWaypoint(p);

        if (selectedWaypoint != null)
            Msg.WP_SELECTED.sendTo(p, selectedWaypoint.getName());

        WaypointManager.openWaypointMenu(p, selectedWaypoint, true, true, true);
    }

    public static boolean setHome(Player p, String waypointName) {
        PlayerData pd = getPlayerData(p.getUniqueId());
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

}
