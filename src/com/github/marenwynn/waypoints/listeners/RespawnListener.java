package com.github.marenwynn.waypoints.listeners;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.github.marenwynn.waypoints.PluginMain;
import com.github.marenwynn.waypoints.Util;
import com.github.marenwynn.waypoints.data.Data;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.PlayerData;
import com.github.marenwynn.waypoints.data.SpawnMode;
import com.github.marenwynn.waypoints.data.Waypoint;

public class RespawnListener implements Listener {

    private PluginMain pm;

    public RespawnListener(PluginMain pm) {
        this.pm = pm;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent respawnEvent) {
        Player p = respawnEvent.getPlayer();
        PlayerData pd = Data.getPlayerData(p.getUniqueId());
        Location spawnLoc = null;

        if (Data.ENABLE_BEACON && p.hasPermission("wp.respawn") && pd.getSpawnPoint() != null) {
            Waypoint home = null;

            for (Waypoint wp : pd.getAllWaypoints()) {
                if (Util.isSameLoc(respawnEvent.getRespawnLocation(), wp.getLocation(), true)) {
                    home = wp;
                    break;
                }
            }

            if (home != null) {
                if (pd.getSpawnPoint().getBlock().getRelative(BlockFace.DOWN).isBlockPowered())
                    spawnLoc = pd.getSpawnPoint();
                else
                    Msg.RESPAWN_NO_POWER.sendTo(p, home.getName());
            } else {
                pd.setSpawnPoint(null);
                Data.savePlayerData(p.getUniqueId());
                Msg.RESPAWN_NOT_FOUND.sendTo(p);
            }
        }

        if (spawnLoc == null) {
            if (Data.SPAWN_MODE == SpawnMode.HOME) {
                HashMap<Double, Waypoint> distances = new HashMap<Double, Waypoint>();

                for (Waypoint wp : pd.getAllWaypoints())
                    if (p.getWorld().getName().equals(wp.getLocation().getWorld().getName()))
                        distances.put(p.getLocation().distanceSquared(wp.getLocation()), wp);

                Double key = null;

                for (Double dist : distances.keySet())
                    if (key == null || dist < key)
                        key = dist;

                if (key != null)
                    spawnLoc = distances.get(key).getLocation();
            }

            if (spawnLoc == null || Data.SPAWN_MODE == SpawnMode.BED)
                if (p.getBedSpawnLocation() != null)
                    spawnLoc = p.getBedSpawnLocation();

            if (spawnLoc == null || Data.SPAWN_MODE == SpawnMode.SPAWN)
                spawnLoc = p.getWorld().getSpawnLocation();

            if (Data.SPAWN_MODE == SpawnMode.CITY) {
                World w = pm.getServer().getWorld(Data.CITY_WORLD_NAME);

                if (w != null)
                    spawnLoc = w.getSpawnLocation();
            }
        }

        respawnEvent.setRespawnLocation(spawnLoc);
    }

}
