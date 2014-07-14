package com.github.marenwynn.waypoints.listeners;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.inventory.meta.BookMeta;

import com.github.marenwynn.waypoints.Selections;
import com.github.marenwynn.waypoints.Util;
import com.github.marenwynn.waypoints.WaypointManager;
import com.github.marenwynn.waypoints.data.Data;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.PlayerData;
import com.github.marenwynn.waypoints.data.Waypoint;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        Action a = event.getAction();

        if (p.hasMetadata("Wayporting"))
            return;

        ItemStack i = event.getItem();

        if (i == null)
            return;

        Block b = event.getClickedBlock();

        if (Data.ENABLE_BEACON && i.isSimilar(Data.BEACON)) {
            if (a == Action.RIGHT_CLICK_BLOCK && p.isSneaking() && p.hasPermission("wp.respawn")) {
                Waypoint home = findHomeWaypoint(p, b);

                if (home != null) {
                    if (b.isBlockPowered()) {
                        PlayerData pd = WaypointManager.getPlayerData(p.getUniqueId());

                        useItem(p, i, true);
                        pd.setSpawnPoint(home.getLocation());
                        Data.savePlayerData(p.getUniqueId());
                        Msg.SET_PLAYER_SPAWN.sendTo(p, home.getName());
                    } else {
                        Msg.INSUFFICIENT_POWER.sendTo(p);
                    }

                    event.setCancelled(true);
                    return;
                }
            }

            if (p.hasPermission("wp.beacon.use")) {
                if (a == Action.RIGHT_CLICK_BLOCK || a == Action.RIGHT_CLICK_AIR) {
                    useItem(p, i, true);
                    WaypointManager.openWaypointMenu(p, null, p.hasPermission("wp.beacon.server"), true, false);
                    event.setCancelled(true);
                    return;
                } else if ((a == Action.LEFT_CLICK_BLOCK || a == Action.LEFT_CLICK_AIR) && p.hasPermission("wp.select")) {
                    WaypointManager.openWaypointSelectionMenu(p);
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (a != Action.RIGHT_CLICK_BLOCK && a != Action.RIGHT_CLICK_AIR || !p.hasPermission("wp.player")
                || !p.isSneaking())
            return;

        Waypoint home = findHomeWaypoint(p, b);

        if (home != null) {
            Material m = i.getType();

            if (m == Material.WRITTEN_BOOK) {
                BookMeta bm = (BookMeta) i.getItemMeta();
                String content = "";

                for (int page = 1; page <= bm.getPageCount(); page++) {
                    content += bm.getPage(page);

                    if (page != bm.getPageCount())
                        content += " ";
                }

                if (content.length() > Data.WP_DESC_MAX_LENGTH)
                    content = content.substring(0, Data.WP_DESC_MAX_LENGTH);

                p.closeInventory();
                home.setDescription(content);
                Msg.WP_DESC_UPDATED_BOOK.sendTo(p, home.getName(), bm.getTitle());
            } else {
                home.setIcon(m);
                home.setDurability(i.getDurability());
                Msg.WP_SETICON.sendTo(p, home.getName(), m.toString(), i.getDurability());
            }

            Util.playSound(home.getLocation(), Sound.ORB_PICKUP);
            Util.playEffect(home.getLocation(), Effect.ENDER_SIGNAL);
            Data.saveWaypoint(p, home);
        } else {
            if (i.getType() != Material.WATCH || !i.hasItemMeta() || !i.getItemMeta().hasDisplayName())
                return;

            if (!WaypointManager.setHome(p, Util.color(i.getItemMeta().getDisplayName())))
                return;

            Location strikeLoc = p.getLocation();
            strikeLoc.setY(strikeLoc.getBlockY() + 2);
            p.getWorld().strikeLightningEffect(strikeLoc);
        }

        useItem(p, i, false);
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent moveEvent) {
        Player p = moveEvent.getPlayer();

        if (p.hasMetadata("Wayporting"))
            return;

        Location from = moveEvent.getFrom();
        Location to = moveEvent.getTo();

        if (Util.isSameLoc(from, to, true))
            return;

        if (Util.isSameLoc(p.getWorld().getSpawnLocation(), to, true)) {
            Waypoint spawn = new Waypoint("Spawn", p.getWorld().getSpawnLocation());
            spawn.setIcon(Material.NETHER_STAR);
            WaypointManager.openWaypointMenu(p, spawn, true, true, false);
            return;
        }

        PlayerData pd = WaypointManager.getPlayerData(p.getUniqueId());

        for (Waypoint wp : WaypointManager.getAllWaypoints()) {
            if (Util.isSameLoc(wp.getLocation(), to, true) && (wp.isEnabled() || p.hasPermission("wp.bypass"))) {
                String perm = "wp.access." + Util.getKey(wp.getName());

                if (wp.isDiscoverable() != null && !pd.hasDiscovered(wp.getUUID())) {
                    pd.addDiscovery(wp.getUUID());
                    Data.savePlayerData(p.getUniqueId());
                    Msg.DISCOVERED_WAYPOINT.sendTo(p, wp.getName());
                }

                if (p.hasPermission(perm) || pd.hasDiscovered(wp.getUUID())) {
                    WaypointManager.openWaypointMenu(p, wp, true, true, false);
                    return;
                }
            }
        }

        for (Waypoint wp : pd.getAllWaypoints()) {
            if (Util.isSameLoc(wp.getLocation(), to, true)) {
                WaypointManager.openWaypointMenu(p, wp, false, true, false);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        PlayerData pd = Data.loadPlayerData(joinEvent.getPlayer().getUniqueId());

        // (v1.1.0) Note: For transition; remove later
        for (Waypoint wp : pd.getAllWaypoints())
            if (!wp.isEnabled())
                wp.setEnabled(true);

        // Cleans discoveries of deleted waypoints
        ArrayList<UUID> waypoints = new ArrayList<UUID>();

        for (Waypoint wp : WaypointManager.getAllWaypoints())
            waypoints.add(wp.getUUID());

        if (pd.retainDiscoveries(waypoints))
            Data.savePlayerData(joinEvent.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        Selections.clearSelectedWaypoint(quitEvent.getPlayer());
        Data.unloadPlayerData(quitEvent.getPlayer().getUniqueId());
    }

    public Waypoint findHomeWaypoint(Player p, Block clicked) {
        if (clicked != null) {
            Location loc = clicked.getRelative(BlockFace.UP).getLocation();

            for (Waypoint wp : WaypointManager.getPlayerData(p.getUniqueId()).getAllWaypoints())
                if (Util.isSameLoc(loc, wp.getLocation(), true))
                    return wp;
        }

        return null;
    }

    public void useItem(Player p, ItemStack i, boolean beacon) {
        if (!beacon || (beacon && !p.hasPermission("wp.beacon.unlimited"))) {
            i.setAmount(i.getAmount() - 1);
            p.setItemInHand(i);
        }
    }

}
