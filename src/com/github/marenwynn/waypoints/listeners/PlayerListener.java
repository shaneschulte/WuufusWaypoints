package com.github.marenwynn.waypoints.listeners;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
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

import com.github.marenwynn.waypoints.PluginMain;
import com.github.marenwynn.waypoints.Util;
import com.github.marenwynn.waypoints.data.Data;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.PlayerData;
import com.github.marenwynn.waypoints.data.Waypoint;

public class PlayerListener implements Listener {

    private PluginMain pm;

    public PlayerListener(PluginMain pm) {
        this.pm = pm;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        Action a = event.getAction();

        if (p.hasMetadata("Wayporting"))
            return;

        ItemStack i = event.getItem();

        if (i == null)
            return;

        if (Data.ENABLE_BEACON && i.isSimilar(Data.BEACON) && p.hasPermission("wp.beacon.use")) {
            if (a == Action.RIGHT_CLICK_BLOCK || a == Action.RIGHT_CLICK_AIR) {
                event.setCancelled(true);

                if (!p.hasPermission("wp.beacon.unlimited")) {
                    i.setAmount(i.getAmount() - 1);
                    p.setItemInHand(i);
                }

                pm.openWaypointMenu(p, null, p.hasPermission("wp.beacon.server"), true, false);
                return;
            } else if (p.hasPermission("wp.select") && (a == Action.LEFT_CLICK_BLOCK || a == Action.LEFT_CLICK_AIR)) {
                event.setCancelled(true);
                pm.openWaypointMenu(p, pm.getSelectedWaypoint(p.getName()), p.hasPermission("wp.admin"), true, true);
                return;
            }
        }

        if (a != Action.RIGHT_CLICK_BLOCK && a != Action.RIGHT_CLICK_AIR || !p.hasPermission("wp.player")
                || !p.isSneaking())
            return;

        Waypoint home = null;

        if (event.getClickedBlock() != null) {
            for (Waypoint wp : Data.getPlayerData(event.getPlayer().getUniqueId()).getAllWaypoints()) {
                if (Util.isSameLoc(event.getClickedBlock().getRelative(BlockFace.UP).getLocation(), wp.getLocation(),
                        true)) {
                    home = wp;
                    break;
                }
            }
        }

        if (home != null) {
            Material m = i.getType();

            if (m == Material.WRITTEN_BOOK) {
                BookMeta b = (BookMeta) i.getItemMeta();
                String content = "";

                for (int page = 1; page <= b.getPageCount(); page++) {
                    content += b.getPage(page);

                    if (page != b.getPageCount())
                        content += " ";
                }

                if (content.length() > Data.WP_DESC_MAX_LENGTH)
                    content = content.substring(0, Data.WP_DESC_MAX_LENGTH);

                home.setDescription(content);
                p.closeInventory();
                Msg.WP_DESC_UPDATED_BOOK.sendTo(p, home.getName(), b.getTitle());
            } else {
                home.setIcon(m);
                home.setDurability(i.getDurability());
                Msg.WP_SETICON.sendTo(p, home.getName(), m.toString(), i.getDurability());
            }

            p.getWorld().playEffect(home.getLocation(), Effect.ENDER_SIGNAL, 16);
            Data.saveWaypoint(p, home);
        } else {
            if (i.getType() != Material.WATCH || !i.hasItemMeta() || !i.getItemMeta().hasDisplayName())
                return;

            if (!pm.setHome(p, Util.color(i.getItemMeta().getDisplayName())))
                return;

            Location strikeLoc = p.getLocation();
            strikeLoc.setY(strikeLoc.getBlockY() + 2);
            p.getWorld().strikeLightningEffect(strikeLoc);
        }

        i.setAmount(i.getAmount() - 1);
        p.setItemInHand(i);
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
            pm.openWaypointMenu(p, spawn, true, true, false);
            return;
        }

        PlayerData pd = Data.getPlayerData(p.getUniqueId());

        for (Waypoint wp : Data.getAllWaypoints()) {
            if (Util.isSameLoc(wp.getLocation(), to, true) && (wp.isEnabled() || p.hasPermission("wp.bypass"))) {
                String perm = "wp.access." + Util.getKey(wp.getName());

                if (wp.isDiscoverable() != null && !pd.hasDiscovered(wp.getUUID())) {
                    pd.addDiscovery(wp.getUUID());
                    Data.savePlayerData(p.getUniqueId());
                    Msg.DISCOVERED_WAYPOINT.sendTo(p, wp.getName());
                }

                if (p.hasPermission(perm) || pd.hasDiscovered(wp.getUUID())) {
                    pm.openWaypointMenu(p, wp, true, true, false);
                    return;
                }
            }
        }

        for (Waypoint wp : pd.getAllWaypoints()) {
            if (Util.isSameLoc(wp.getLocation(), to, true)) {
                pm.openWaypointMenu(p, wp, false, true, false);
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

        for (Waypoint wp : Data.getAllWaypoints())
            waypoints.add(wp.getUUID());

        if (pd.retainDiscoveries(waypoints))
            Data.savePlayerData(joinEvent.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        pm.clearSelectedWaypoint(quitEvent.getPlayer().getName());
        Data.unloadPlayerData(quitEvent.getPlayer().getUniqueId());
    }

}
