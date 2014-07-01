package com.github.marenwynn.waypoints.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.marenwynn.waypoints.PluginMain;
import com.github.marenwynn.waypoints.Util;

public class Database {

    private PluginMain                         pm;

    private File                               playerFolder, waypointDataFile;
    private LinkedHashMap<String, Waypoint>    waypoints;
    private HashMap<UUID, ArrayList<Waypoint>> playerWaypoints;

    private static HashMap<Msg, String>        messages;
    public int                                 WP_NAME_MAX_LENGTH, WP_DESC_MAX_LENGTH;

    public Database(PluginMain pm) {
        this.pm = pm;

        messages = new HashMap<Msg, String>();
        loadConfig();

        waypoints = new LinkedHashMap<String, Waypoint>();
        playerWaypoints = new HashMap<UUID, ArrayList<Waypoint>>();
        playerFolder = new File(pm.getDataFolder().getPath() + File.separator + "players");
        waypointDataFile = new File(pm.getDataFolder().getPath() + File.separator + "waypoints.db");

        if (!playerFolder.exists())
            playerFolder.mkdir();

        if (waypointDataFile.exists())
            loadData();

    }

    public void loadConfig() {
        pm.saveDefaultConfig();
        messages.clear();

        for (Msg msg : Msg.values())
            messages.put(msg, pm.getConfig().getString("Waypoints.Messages." + msg.name(), msg.getDefaultMsg()));

        WP_NAME_MAX_LENGTH = pm.getConfig().getInt("Waypoints.WP_NAME_MAX_LENGTH", 18);
        WP_DESC_MAX_LENGTH = pm.getConfig().getInt("Waypoints.WP_DESC_MAX_LENGTH", 100);
    }

    public Waypoint getWaypoint(String name) {
        String key = Util.getKey(name);

        if (waypoints.containsKey(key))
            return waypoints.get(key);

        return null;
    }

    public void addWaypoint(Waypoint wp) {
        waypoints.put(Util.getKey(wp.getName()), wp);
        sortWaypoints();
        saveData();
    }

    public void removeWaypoint(Waypoint wp) {
        waypoints.remove(Util.getKey(wp.getName()));
        sortWaypoints();
        saveData();
    }

    public LinkedHashMap<String, Waypoint> getAllWaypoints() {
        return waypoints;
    }

    public void sortWaypoints() {
        ArrayList<String> keys = new ArrayList<String>();
        keys.addAll(waypoints.keySet());
        Collections.sort(keys);

        LinkedHashMap<String, Waypoint> sortedWaypoints = new LinkedHashMap<String, Waypoint>();

        for (String key : keys)
            sortedWaypoints.put(key, waypoints.get(key));

        waypoints.clear();
        waypoints.putAll(sortedWaypoints);
    }

    public void loadData() {
        List<?> uncasted = null;

        try {
            FileInputStream fis = new FileInputStream(waypointDataFile);
            ObjectInputStream ois = new ObjectInputStream(fis);

            try {
                uncasted = (List<?>) ois.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                ois.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (uncasted != null) {
            for (Object obj : uncasted) {
                if (obj instanceof Waypoint) {
                    Waypoint wp = (Waypoint) obj;
                    waypoints.put(Util.getKey(wp.getName()), wp);
                }
            }
        }
    }

    public void saveData() {
        try {
            FileOutputStream fos = new FileOutputStream(waypointDataFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            try {
                oos.writeObject(Arrays.asList(waypoints.values().toArray(new Waypoint[waypoints.size()])));
            } finally {
                oos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Waypoint addWaypointForPlayer(UUID player, Waypoint wp) {
        ArrayList<Waypoint> playerPoints = getWaypointsForPlayer(player);
        Waypoint replaced = null;

        if (playerPoints.size() >= 3)
            replaced = playerPoints.remove(0);

        playerPoints.add(wp);
        saveWaypointsForPlayer(player);
        return replaced;
    }

    public void removeWaypointForPlayer(UUID player, Waypoint wp) {
        getWaypointsForPlayer(player).remove(wp);
        saveWaypointsForPlayer(player);
    }

    public ArrayList<Waypoint> getWaypointsForPlayer(UUID player) {
        return playerWaypoints.get(player);
    }

    public void unloadWaypointsForPlayer(UUID player) {
        if (playerWaypoints.containsKey(player))
            playerWaypoints.remove(player);
    }

    public void loadWaypointsForPlayer(UUID player) {
        File playerFile = new File(playerFolder + File.separator + player);

        if (!playerFile.exists()) {
            playerWaypoints.put(player, new ArrayList<Waypoint>());
            return;
        }

        ArrayList<?> uncasted = null;

        try {
            FileInputStream fis = new FileInputStream(playerFile);
            ObjectInputStream ois = new ObjectInputStream(fis);

            try {
                uncasted = (ArrayList<?>) ois.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                ois.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (uncasted != null) {
            ArrayList<Waypoint> playerPoints = new ArrayList<Waypoint>();

            for (Object obj : uncasted)
                if (obj instanceof Waypoint)
                    playerPoints.add((Waypoint) obj);

            playerWaypoints.put(player, playerPoints);
        }
    }

    public void saveWaypointsForPlayer(UUID player) {
        ArrayList<Waypoint> playerPoints = getWaypointsForPlayer(player);
        File playerFile = new File(playerFolder + File.separator + player);

        try {
            FileOutputStream fos = new FileOutputStream(playerFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            try {
                oos.writeObject(playerPoints);
            } finally {
                oos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveWaypoint(CommandSender sender, Waypoint wp) {
        if (waypoints.containsValue(wp))
            saveData();
        else
            saveWaypointsForPlayer(((Player) sender).getUniqueId());
    }

    public static String getMsg(Msg msg) {
        return Util.color(messages.get(msg));
    }

}
