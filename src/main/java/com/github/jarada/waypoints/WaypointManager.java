package com.github.jarada.waypoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.jarada.waypoints.data.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.FixedMetadataValue;

public class WaypointManager {

    private static WaypointManager wm;

    private Map<UUID, PlayerData>  players;
    private Map<String, Waypoint>  waypoints;

    public WaypointManager() {
        players = new HashMap<UUID, PlayerData>();
        waypoints = new LinkedHashMap<String, Waypoint>();
    }

    public static WaypointManager getManager() {
        if (wm == null)
            wm = new WaypointManager();

        return wm;
    }

    public Waypoint getWaypoint(String name) {
        String key = Util.getKey(name);

        if (waypoints.containsKey(key))
            return waypoints.get(key);

        return null;
    }

    public boolean isServerDefined(Waypoint wp) {
        return waypoints.values().contains(wp);
    }

    public boolean isSystemName(String name) {
        return name.equals(Msg.WORD_BED.toString()) || name.equals(Msg.WORD_SPAWN.toString());
    }

    public void addWaypoint(Waypoint wp) {
        waypoints.put(Util.getKey(wp.getName()), wp);
        sortWaypoints();
    }

    public void removeWaypoint(Waypoint wp) {
        waypoints.remove(Util.getKey(wp.getName()));
        sortWaypoints();
    }

    public boolean renameWaypoint(Waypoint wp, Player p, String newWaypointName) {
        if (isSystemName(newWaypointName)) {
            Msg.WP_DUPLICATE_NAME.sendTo(p, newWaypointName);
            return false;
        }

        boolean serverDefined = isServerDefined(wp);

        if (serverDefined) {
            if (getWaypoint(newWaypointName) != null) {
                Msg.WP_DUPLICATE_NAME.sendTo(p, newWaypointName);
                return false;
            }

            removeWaypoint(wp);
        } else {
            PlayerData pd = getPlayerData(p.getUniqueId());

            if (pd.getWaypoint(newWaypointName) != null) {
                Msg.WP_DUPLICATE_NAME.sendTo(p, newWaypointName);
                return false;
            }

            pd.removeWaypoint(wp);
        }

        wp.setName(newWaypointName);

        if (serverDefined)
            wm.addWaypoint(wp);
        else
            getPlayerData(p.getUniqueId()).addWaypoint(wp);

        return true;
    }

    public Map<String, Waypoint> getWaypoints() {
        return waypoints;
    }

    private void sortWaypoints() {
        List<String> keys = new ArrayList<String>(waypoints.keySet());
        Collections.sort(keys);

        Map<String, Waypoint> sortedWaypoints = new LinkedHashMap<String, Waypoint>();

        for (String key : keys)
            sortedWaypoints.put(key, waypoints.get(key));

        waypoints.clear();
        waypoints.putAll(sortedWaypoints);
    }

    public PlayerData getPlayerData(UUID player) {
        return players.get(player);
    }

    public Map<UUID, PlayerData> getPlayers() {
        return players;
    }

    public void openWaypointMenu(Player p, Waypoint currentWaypoint, boolean addServerWaypoints,
            boolean addHomeWaypoints, boolean select) {
        List<WaypointHolder> accessList = new ArrayList<>();

        if (!select) {
            if (currentWaypoint != null) {
                Msg.OPEN_WP_MENU.sendTo(p, currentWaypoint.getName());
                p.teleport(Util.teleportLocation(currentWaypoint.getLocation()), TeleportCause.PLUGIN);
            } else {
                Msg.REMOTELY_ACCESSED.sendTo(p);
            }

            if (p.hasPermission("wp.access.spawn")) {
                if (currentWaypoint != null && currentWaypoint.getName().equals(Msg.WORD_SPAWN.toString())) {
                    accessList.add(new WaypointHolder(currentWaypoint));
                } else {
                    Waypoint spawn = new Waypoint(Msg.WORD_SPAWN.toString(), p.getWorld().getSpawnLocation());
                    spawn.setIcon(Material.NETHER_STAR);
                    accessList.add(new WaypointHolder(spawn));
                }
            }

            if (p.hasPermission("wp.access.bed") && p.getBedSpawnLocation() != null) {
                Waypoint bed = new Waypoint(Msg.WORD_BED.toString(), p.getBedSpawnLocation());
                bed.setIcon(Material.WHITE_BED);
                accessList.add(new WaypointHolder(bed));
            }
        }

        PlayerData pd = getPlayerData(p.getUniqueId());

        if (addServerWaypoints && (!select || p.hasPermission("wp.admin")))
            for (Waypoint wp : waypoints.values())
                if (Util.hasAccess(p, wp, select))
                    accessList.add(new WaypointHolder(wp));
                else if (DataManager.getManager().SHOW_DISCOVERABLE_WAYPOINTS && Util.canDiscover(p, wp))
                    accessList.add(new WaypointHolder(wp, true));

        if (addHomeWaypoints)
            accessList.addAll(pd.getAllWaypoints().stream()
                    .map(WaypointHolder::new)
                    .collect(Collectors.toList()));

        p.setMetadata("InMenu", new FixedMetadataValue(PluginMain.getPluginInstance(), true));
        new WaypointMenu(p, pd, currentWaypoint, accessList, select).open();
    }

    public void openWaypointSelectionMenu(Player p) {
        Waypoint selectedWaypoint = SelectionManager.getManager().getSelectedWaypoint(p);

        if (selectedWaypoint != null)
            Msg.WP_SELECTED.sendTo(p, selectedWaypoint.getName());

        openWaypointMenu(p, selectedWaypoint, true, true, true);
    }

    public boolean setHome(Player p, String waypointName) {
        PlayerData pd = getPlayerData(p.getUniqueId());
        Location playerLoc = p.getLocation();

        if (isSystemName(waypointName)) {
            Msg.WP_DUPLICATE_NAME.sendTo(p, waypointName);
            return false;
        }

        for (Waypoint wp : pd.getAllWaypoints()) {
            if (Util.isSameLoc(playerLoc, wp.getLocation(), true)) {
                Msg.HOME_WP_ALREADY_HERE.sendTo(p, wp.getName());
                return false;
            }

            if (waypointName.equals(wp.getName())) {
                wp.setLocation(p.getLocation());
                DataManager.getManager().savePlayerData(p.getUniqueId());
                Msg.HOME_WP_LOCATION_UPDATED.sendTo(p, waypointName);
                return true;
            }
        }

        Waypoint wp = new Waypoint(waypointName, playerLoc);
        wp.setDescription(Msg.SETHOME_DEFAULT_DESC.toString());

        Waypoint replaced = pd.addWaypoint(wp);
        DataManager.getManager().savePlayerData(p.getUniqueId());

        if (replaced != null)
            Msg.HOME_WP_REPLACED.sendTo(p, replaced.getName(), wp.getName());
        else
            Msg.HOME_WP_CREATED.sendTo(p, waypointName);

        updatePlayerOnHomeCount(p, pd);

        return true;
    }

    private void updatePlayerOnHomeCount(Player p, PlayerData pd) {
        int maxHomeWaypoints = DataManager.getManager().MAX_HOME_WAYPOINTS - pd.getAllWaypoints().size();

        if (maxHomeWaypoints > 0)
            Msg.HOME_WP_REMAINING.sendTo(p, maxHomeWaypoints);
        else
            Msg.HOME_WP_FULL.sendTo(p, maxHomeWaypoints);
    }

}
