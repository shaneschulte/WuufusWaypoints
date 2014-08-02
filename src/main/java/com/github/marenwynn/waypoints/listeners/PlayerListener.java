package com.github.marenwynn.waypoints.listeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.github.marenwynn.waypoints.SelectionManager;
import com.github.marenwynn.waypoints.Util;
import com.github.marenwynn.waypoints.WaypointManager;
import com.github.marenwynn.waypoints.data.DataManager;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.PlayerData;
import com.github.marenwynn.waypoints.data.Waypoint;
import com.github.marenwynn.waypoints.events.BeaconUseEvent;
import com.github.marenwynn.waypoints.events.WaypointInteractEvent;

public class PlayerListener implements Listener {

    private DataManager     dm;
    private WaypointManager wm;

    public PlayerListener() {
        dm = DataManager.getManager();
        wm = WaypointManager.getManager();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        Action a = event.getAction();
        ItemStack is = event.getItem();

        if (p.hasMetadata("InMenu") || p.hasMetadata("Wayporting"))
            return;

        // Calls WaypointInteractEvent if sneaking and clicked block is a
        // waypoint
        if (p.isSneaking() && (a == Action.RIGHT_CLICK_BLOCK || a == Action.LEFT_CLICK_BLOCK)) {
            Block clicked = event.getClickedBlock();
            Location toCheck = clicked.getLocation().add(0, 1D, 0);
            Set<Waypoint> waypoints = new HashSet<Waypoint>(wm.getWaypoints().values());
            waypoints.addAll(wm.getPlayerData(p.getUniqueId()).getAllWaypoints());

            for (Waypoint wp : waypoints) {
                if (Util.isSameLoc(toCheck, wp.getLocation(), true)) {
                    Bukkit.getPluginManager().callEvent(
                            new WaypointInteractEvent(p, wp, a, is, clicked.isBlockPowered()));
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (is == null)
            return;

        // Creates a new home waypoint if player is sneaking and right-clicking
        // with a renamed clock
        if (p.isSneaking() && p.hasPermission("wp.player")
                && (a == Action.RIGHT_CLICK_BLOCK || a == Action.RIGHT_CLICK_AIR)) {
            if (is.hasItemMeta() && is.getItemMeta().hasDisplayName()) {
                if (!wm.setHome(p, Util.color(is.getItemMeta().getDisplayName())))
                    return;

                is.setAmount(is.getAmount() - 1);
                p.setItemInHand(is);

                Location strikeLoc = p.getLocation();
                strikeLoc.setY(strikeLoc.getBlockY() + 2);
                p.getWorld().strikeLightningEffect(strikeLoc);
                event.setCancelled(true);
                return;
            }
        }

        // Calls BeaconUseEvent on click if item in hand is a Waypoint Beacon
        if (dm.ENABLE_BEACON && is.isSimilar(dm.BEACON)) {
            if (a != Action.PHYSICAL)
                Bukkit.getPluginManager().callEvent(new BeaconUseEvent(p, a));

            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent moveEvent) {
        Player p = moveEvent.getPlayer();

        if (p.hasMetadata("InMenu") || p.hasMetadata("Wayporting"))
            return;

        Location from = moveEvent.getFrom();
        Location to = moveEvent.getTo();

        if (Util.isSameLoc(from, to, true))
            return;

        if (Util.isSameLoc(p.getWorld().getSpawnLocation(), to, true)) {
            Waypoint spawn = new Waypoint("Spawn", p.getWorld().getSpawnLocation());
            spawn.setIcon(Material.NETHER_STAR);
            wm.openWaypointMenu(p, spawn, true, true, false);
            return;
        }

        PlayerData pd = wm.getPlayerData(p.getUniqueId());

        for (Waypoint wp : wm.getWaypoints().values()) {
            if (Util.isSameLoc(wp.getLocation(), to, true) && (wp.isEnabled() || p.hasPermission("wp.bypass"))) {
                String perm = "wp.access." + Util.getKey(wp.getName());

                if (wp.isDiscoverable() != null && !pd.hasDiscovered(wp.getUUID())) {
                    pd.addDiscovery(wp.getUUID());
                    dm.savePlayerData(p.getUniqueId());
                    Msg.DISCOVERED_WAYPOINT.sendTo(p, wp.getName());
                }

                if (p.hasPermission(perm) || pd.hasDiscovered(wp.getUUID())) {
                    wm.openWaypointMenu(p, wp, true, true, false);
                    return;
                }
            }
        }

        for (Waypoint wp : pd.getAllWaypoints()) {
            if (Util.isSameLoc(wp.getLocation(), to, true)) {
                wm.openWaypointMenu(p, wp, false, true, false);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        PlayerData pd = dm.loadPlayerData(joinEvent.getPlayer().getUniqueId());

        // (v1.1.0) Note: For transition; remove later
        for (Waypoint wp : pd.getAllWaypoints())
            if (!wp.isEnabled())
                wp.setEnabled(true);

        // Cleans discoveries of deleted waypoints
        List<UUID> waypoints = new ArrayList<UUID>();

        for (Waypoint wp : wm.getWaypoints().values())
            waypoints.add(wp.getUUID());

        if (pd.retainDiscoveries(waypoints))
            dm.savePlayerData(joinEvent.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        SelectionManager.getManager().clearSelectedWaypoint(quitEvent.getPlayer());
        dm.unloadPlayerData(quitEvent.getPlayer().getUniqueId());
    }

    public Waypoint findHomeWaypoint(PlayerData pd, Block clicked) {
        if (clicked != null) {
            Location loc = clicked.getRelative(BlockFace.UP).getLocation();

            for (Waypoint wp : pd.getAllWaypoints())
                if (Util.isSameLoc(loc, wp.getLocation(), true))
                    return wp;
        }

        return null;
    }

}
