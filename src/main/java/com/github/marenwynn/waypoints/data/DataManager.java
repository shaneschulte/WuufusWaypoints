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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import com.github.marenwynn.waypoints.PluginMain;
import com.github.marenwynn.waypoints.Util;
import com.github.marenwynn.waypoints.WaypointManager;
import com.github.marenwynn.waypoints.listeners.RespawnListener;

public class DataManager {

    private static DataManager dm;
    private PluginMain         pm;
    private WaypointManager    wm;

    private File               playerFolder, waypointDataFile;
    private Map<Msg, String>   messages;

    public int                 MAX_HOME_WAYPOINTS;
    public int                 WP_NAME_MAX_LENGTH, WP_DESC_MAX_LENGTH;
    public boolean             ENABLE_BEACON;
    public ItemStack           BEACON;
    public boolean             HANDLE_RESPAWNING;
    public SpawnMode           SPAWN_MODE;
    public String              CITY_WORLD_NAME;

    public DataManager() {
        pm = PluginMain.getPluginInstance();
        wm = WaypointManager.getManager();

        playerFolder = new File(pm.getDataFolder().getPath() + File.separator + "players");
        waypointDataFile = new File(pm.getDataFolder().getPath() + File.separator + "waypoints.db");
        messages = new HashMap<Msg, String>();

        loadConfig();
    }

    public static DataManager getManager() {
        if (dm == null)
            dm = new DataManager();

        return dm;
    }

    public void loadConfig() {
        if (!playerFolder.exists())
            playerFolder.mkdirs();

        FileConfiguration config = pm.getConfig();

        config.addDefault("Waypoints.MAX_HOME_WAYPOINTS", 3);
        config.addDefault("Waypoints.WP_NAME_MAX_LENGTH", 18);
        config.addDefault("Waypoints.WP_DESC_MAX_LENGTH", 100);
        config.addDefault("Waypoints.ENABLE_BEACON", true);
        config.addDefault("Waypoints.HANDLE_RESPAWNING", true);
        config.addDefault("Waypoints.SPAWN_MODE", "home");
        config.addDefault("Waypoints.CITY_WORLD_NAME", "world");

        for (Msg msg : Msg.values()) {
            String path = "Waypoints.Messages." + msg.name();
            config.addDefault(path, msg.getDefaultMsg());
            messages.put(msg, config.getString(path));
        }

        MAX_HOME_WAYPOINTS = config.getInt("Waypoints.MAX_HOME_WAYPOINTS");
        WP_NAME_MAX_LENGTH = config.getInt("Waypoints.WP_NAME_MAX_LENGTH");
        WP_DESC_MAX_LENGTH = config.getInt("Waypoints.WP_DESC_MAX_LENGTH");
        ENABLE_BEACON = config.getBoolean("Waypoints.ENABLE_BEACON");
        HANDLE_RESPAWNING = config.getBoolean("Waypoints.HANDLE_RESPAWNING");
        SPAWN_MODE = SpawnMode.valueOf(config.getString("Waypoints.SPAWN_MODE").toUpperCase());
        CITY_WORLD_NAME = config.getString("Waypoints.CITY_WORLD_NAME");

        config.options().copyDefaults(true);
        pm.saveConfig();

        if (ENABLE_BEACON) {
            List<String> lore = new ArrayList<String>();
            lore.add(Util.color("&fBroadcasts signal to"));
            lore.add(Util.color("&fwaypoint directory for"));
            lore.add(Util.color("&fremote connection."));
            lore.add(Util.color("&8&oRight-click to use"));

            BEACON = Util.setItemNameAndLore(new ItemStack(Material.COMPASS, 1), "&aWaypoint Beacon", lore);

            ShapedRecipe sr = new ShapedRecipe(BEACON);
            sr.shape("RRR", "RCR", "RRR").setIngredient('R', Material.REDSTONE).setIngredient('C', Material.COMPASS);
            Bukkit.addRecipe(sr);
        }

        if (HANDLE_RESPAWNING)
            Bukkit.getPluginManager().registerEvents(RespawnListener.getListener(), pm);
    }

    public void reload() {
        if (ENABLE_BEACON) {
            Iterator<Recipe> recipes = Bukkit.recipeIterator();
            Recipe recipe;

            while (recipes.hasNext()) {
                recipe = recipes.next();

                if (recipe != null && recipe.getResult().isSimilar(BEACON))
                    recipes.remove();
            }
        }

        if (HANDLE_RESPAWNING)
            HandlerList.unregisterAll(RespawnListener.getListener());

        pm.reloadConfig();
        loadConfig();
    }

    public String getMsg(Msg msg) {
        return messages.get(msg);
    }

    public void loadWaypoints() {
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
                    wm.getWaypoints().put(Util.getKey(wp.getName()), wp);
                }
            }
        }
    }

    public void saveWaypoints() {
        try {
            FileOutputStream fos = new FileOutputStream(waypointDataFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            try {
                oos.writeObject(Arrays.asList(wm.getWaypoints().values()
                        .toArray(new Waypoint[wm.getWaypoints().size()])));
            } finally {
                oos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unloadPlayerData(UUID player) {
        if (wm.getPlayers().containsKey(player))
            wm.getPlayers().remove(player);
    }

    public PlayerData loadPlayerData(UUID playerUUID) {
        File playerFile = new File(playerFolder + File.separator + playerUUID);
        Map<UUID, PlayerData> players = wm.getPlayers();

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

    public void savePlayerData(UUID playerUUID) {
        File playerFile = new File(playerFolder + File.separator + playerUUID);

        try {
            FileOutputStream fos = new FileOutputStream(playerFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            try {
                oos.writeObject(wm.getPlayerData(playerUUID));
            } finally {
                oos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveWaypoint(CommandSender sender, Waypoint wp) {
        if (wm.getWaypoints().containsValue(wp))
            saveWaypoints();
        else
            savePlayerData(((Player) sender).getUniqueId());
    }

}
