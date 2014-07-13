package com.github.marenwynn.waypoints.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import com.github.marenwynn.waypoints.PluginMain;
import com.github.marenwynn.waypoints.Util;
import com.github.marenwynn.waypoints.WaypointManager;
import com.github.marenwynn.waypoints.listeners.RespawnListener;

public class Data {

    private static PluginMain       pm;
    private static RespawnListener  respawnListener;

    private static File             playerFolder, waypointDataFile;
    private static Map<Msg, String> messages;

    public static int               WP_NAME_MAX_LENGTH, WP_DESC_MAX_LENGTH;
    public static boolean           ENABLE_BEACON;
    public static ItemStack         BEACON;
    public static boolean           HANDLE_RESPAWNING;
    public static SpawnMode         SPAWN_MODE;
    public static String            CITY_WORLD_NAME;

    public static void init() {
        Data.pm = PluginMain.instance;

        playerFolder = new File(pm.getDataFolder().getPath() + File.separator + "players");
        waypointDataFile = new File(pm.getDataFolder().getPath() + File.separator + "waypoints.db");
        messages = new HashMap<Msg, String>();

        loadConfig();

        if (!playerFolder.exists())
            playerFolder.mkdir();

        if (ENABLE_BEACON) {
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(Util.color("&fBroadcasts signal to"));
            lore.add(Util.color("&fwaypoint directory for"));
            lore.add(Util.color("&fremote connection."));
            lore.add(Util.color("&8&oRight-click to use"));

            BEACON = Util.setItemNameAndLore(new ItemStack(Material.COMPASS, 1), "&aWaypoint Beacon", lore);

            ShapedRecipe sr = new ShapedRecipe(BEACON);
            sr.shape("RRR", "RCR", "RRR").setIngredient('R', Material.REDSTONE).setIngredient('C', Material.COMPASS);
            pm.getServer().addRecipe(sr);
        }

        if (HANDLE_RESPAWNING) {
            respawnListener = new RespawnListener();
            pm.getServer().getPluginManager().registerEvents(respawnListener, pm);
        }
    }

    public static void loadConfig() {
        pm.saveDefaultConfig();

        for (Msg msg : Msg.values())
            messages.put(msg, pm.getConfig().getString("Waypoints.Messages." + msg.name(), msg.getDefaultMsg()));

        WP_NAME_MAX_LENGTH = pm.getConfig().getInt("Waypoints.WP_NAME_MAX_LENGTH", 18);
        WP_DESC_MAX_LENGTH = pm.getConfig().getInt("Waypoints.WP_DESC_MAX_LENGTH", 100);
        ENABLE_BEACON = pm.getConfig().getBoolean("Waypoints.ENABLE_BEACON", true);
        HANDLE_RESPAWNING = pm.getConfig().getBoolean("Waypoints.HANDLE_RESPAWNING", true);
        SPAWN_MODE = SpawnMode.valueOf(pm.getConfig().getString("Waypoints.SPAWN_MODE", "HOME").toUpperCase());
        CITY_WORLD_NAME = pm.getConfig().getString("Waypoints.CITY_WORLD_NAME");

        pm.getConfig().options().copyDefaults(true);
        pm.saveConfig();
    }

    public static void kill() {
        if (ENABLE_BEACON) {
            Iterator<Recipe> recipes = pm.getServer().recipeIterator();
            Recipe recipe;

            while (recipes.hasNext()) {
                recipe = recipes.next();

                if (recipe != null && recipe.getResult().isSimilar(BEACON))
                    recipes.remove();
            }
        }

        if (respawnListener != null) {
            HandlerList.unregisterAll(respawnListener);
            respawnListener = null;
        }

        pm = null;
        playerFolder = null;
        waypointDataFile = null;
        messages = null;
        BEACON = null;
        SPAWN_MODE = null;
        CITY_WORLD_NAME = null;
    }

    public static String getMsg(Msg msg) {
        return messages.get(msg);
    }

    public static void loadWaypoints() {
        if (!waypointDataFile.exists())
            return;

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
                    WaypointManager.waypoints.put(Util.getKey(wp.getName()), wp);
                }
            }
        }
    }

    public static void saveWaypoints() {
        try {
            FileOutputStream fos = new FileOutputStream(waypointDataFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            try {
                oos.writeObject(Arrays.asList(WaypointManager.waypoints.values().toArray(
                        new Waypoint[WaypointManager.waypoints.size()])));
            } finally {
                oos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void unloadPlayerData(UUID player) {
        if (WaypointManager.players.containsKey(player))
            WaypointManager.players.remove(player);
    }

    public static PlayerData loadPlayerData(UUID playerUUID) {
        File playerFile = new File(playerFolder + File.separator + playerUUID);
        Map<UUID, PlayerData> players = WaypointManager.players;

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
                oos.writeObject(WaypointManager.players.get(playerUUID));
            } finally {
                oos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveWaypoint(CommandSender sender, Waypoint wp) {
        if (WaypointManager.waypoints.containsValue(wp))
            saveWaypoints();
        else
            savePlayerData(((Player) sender).getUniqueId());
    }

}
