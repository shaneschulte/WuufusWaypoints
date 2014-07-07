package com.github.marenwynn.waypoints.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import com.github.marenwynn.waypoints.PluginMain;
import com.github.marenwynn.waypoints.Util;

public class Data {

    private static PluginMain                      pm;

    private static File                            playerFolder, waypointDataFile;
    private static HashMap<UUID, PlayerData>       players;
    private static LinkedHashMap<String, Waypoint> waypoints;

    private static HashMap<Msg, String>            messages;

    public static boolean                          ENABLE_BEACON;
    public static int                              WP_NAME_MAX_LENGTH, WP_DESC_MAX_LENGTH;
    public static ItemStack                        BEACON;

    public static void init(PluginMain pm) {
        Data.pm = pm;

        waypoints = new LinkedHashMap<String, Waypoint>();
        players = new HashMap<UUID, PlayerData>();
        playerFolder = new File(pm.getDataFolder().getPath() + File.separator + "players");
        waypointDataFile = new File(pm.getDataFolder().getPath() + File.separator + "waypoints.db");
        messages = new HashMap<Msg, String>();

        loadConfig();

        if (!playerFolder.exists())
            playerFolder.mkdir();

        if (waypointDataFile.exists())
            loadWaypoints();

        for (Player p : pm.getServer().getOnlinePlayers())
            loadPlayerData(p.getUniqueId());

        if (ENABLE_BEACON)
            setupBeacon(true);
    }

    public static void kill() {
        saveWaypoints();

        for (Player p : pm.getServer().getOnlinePlayers())
            savePlayerData(p.getUniqueId());

        if (ENABLE_BEACON)
            setupBeacon(false);

        pm = null;
        playerFolder = null;
        waypointDataFile = null;
        players = null;
        waypoints = null;
        messages = null;
        BEACON = null;
    }

    public static void loadConfig() {
        pm.saveDefaultConfig();
        messages.clear();

        for (Msg msg : Msg.values())
            messages.put(msg, pm.getConfig().getString("Waypoints.Messages." + msg.name(), msg.getDefaultMsg()));

        WP_NAME_MAX_LENGTH = pm.getConfig().getInt("Waypoints.WP_NAME_MAX_LENGTH", 18);
        WP_DESC_MAX_LENGTH = pm.getConfig().getInt("Waypoints.WP_DESC_MAX_LENGTH", 100);
        ENABLE_BEACON = pm.getConfig().getBoolean("Waypoints.ENABLE_BEACON");
    }

    private static void setupBeacon(boolean enabled) {
        if (enabled) {
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(Util.color("&fBroadcasts signal to"));
            lore.add(Util.color("&fwaypoint directory for"));
            lore.add(Util.color("&fremote connection."));
            lore.add(Util.color("&8&oRight-click to use"));

            BEACON = Util.setItemNameAndLore(new ItemStack(Material.COMPASS, 1), Util.color("&aWaypoint Beacon"), lore);

            ShapedRecipe sr = new ShapedRecipe(BEACON);
            sr.shape("RRR", "RCR", "RRR").setIngredient('R', Material.REDSTONE).setIngredient('C', Material.COMPASS);
            pm.getServer().addRecipe(sr);
        } else {
            Iterator<Recipe> recipes = pm.getServer().recipeIterator();
            Recipe recipe;

            while (recipes.hasNext()) {
                recipe = recipes.next();

                if (recipe != null && recipe.getResult().isSimilar(BEACON))
                    recipes.remove();
            }
        }
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

    public static void sortWaypoints() {
        ArrayList<String> keys = new ArrayList<String>();
        keys.addAll(waypoints.keySet());
        Collections.sort(keys);

        LinkedHashMap<String, Waypoint> sortedWaypoints = new LinkedHashMap<String, Waypoint>();

        for (String key : keys)
            sortedWaypoints.put(key, waypoints.get(key));

        waypoints.clear();
        waypoints.putAll(sortedWaypoints);
    }

    public static void loadWaypoints() {
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

    public static void saveWaypoints() {
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

    public static PlayerData getPlayerData(UUID player) {
        return players.get(player);
    }

    public static void unloadPlayerData(UUID player) {
        if (players.containsKey(player))
            players.remove(player);
    }

    public static PlayerData loadPlayerData(UUID playerUUID) {
        File playerFile = new File(playerFolder + File.separator + playerUUID);

        if (!playerFile.exists()) {
            players.put(playerUUID, new PlayerData(playerUUID));
        } else {
            Object uncasted = null;

            try {
                FileInputStream fis = new FileInputStream(playerFile);
                ObjectInputStream ois = new ObjectInputStream(fis);

                try {
                    uncasted = ois.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    ois.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (uncasted != null) {
                if (uncasted instanceof PlayerData) {
                    players.put(playerUUID, (PlayerData) uncasted);
                } else if (uncasted instanceof ArrayList<?>) {
                    // (v1.1.0) Note: For transition; remove later
                    PlayerData pd = new PlayerData(playerUUID);

                    for (Object obj : (ArrayList<?>) uncasted)
                        if (obj instanceof Waypoint)
                            pd.addWaypoint((Waypoint) obj);

                    players.put(playerUUID, pd);
                }
            }
        }

        return players.get(playerUUID);
    }

    public static void savePlayerData(UUID playerUUID) {
        File playerFile = new File(playerFolder + File.separator + playerUUID);

        try {
            FileOutputStream fos = new FileOutputStream(playerFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            try {
                oos.writeObject(players.get(playerUUID));
            } finally {
                oos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveWaypoint(CommandSender sender, Waypoint wp) {
        if (waypoints.containsValue(wp))
            saveWaypoints();
        else
            savePlayerData(((Player) sender).getUniqueId());
    }

    public static String getMsg(Msg msg) {
        return Util.color(messages.get(msg));
    }

}
