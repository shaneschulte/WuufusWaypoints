package com.github.marenwynn.waypoints.listeners;

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
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.github.marenwynn.waypoints.PluginMain;
import com.github.marenwynn.waypoints.Util;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;

public class PlayerListener implements Listener {

    private PluginMain pm;

    public PlayerListener(PluginMain pm) {
        this.pm = pm;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR)
            return;

        Player p = event.getPlayer();

        if (!p.hasPermission("wp.player") || !p.isSneaking())
            return;

        ItemStack i = event.getItem();

        if (i == null)
            return;

        Waypoint home = null;

        if (event.getClickedBlock() != null) {
            for (Waypoint wp : pm.getData().getWaypointsForPlayer(event.getPlayer().getUniqueId())) {
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

                if (content.length() > pm.getData().WP_DESC_MAX_LENGTH)
                    content = content.substring(0, pm.getData().WP_DESC_MAX_LENGTH);

                home.setDescription(content);
                p.closeInventory();
                Msg.WP_DESC_UPDATED_BOOK.sendTo(p, home.getName(), b.getTitle());
            } else {
                home.setIcon(m);
                home.setDurability(i.getDurability());
                Msg.WP_SETICON.sendTo(p, home.getName(), m.toString(), i.getDurability());
            }

            p.getWorld().playEffect(home.getLocation(), Effect.ENDER_SIGNAL, 16);
            pm.getData().saveWaypoint(p, home);
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
        Location from = moveEvent.getFrom();
        Location to = moveEvent.getTo();

        if (Util.isSameLoc(from, to, true))
            return;

        Player p = moveEvent.getPlayer();

        if (Util.isSameLoc(p.getWorld().getSpawnLocation(), to, true)) {
            Waypoint spawn = new Waypoint("Spawn", p.getWorld().getSpawnLocation());
            spawn.setIcon(Material.NETHER_STAR);
            openWaypointMenu(p, spawn, true, true, false);
            return;
        }

        for (Waypoint wp : pm.getData().getAllWaypoints().values()) {
            if (p.hasPermission("wp.access." + Util.getKey(wp.getName())) && Util.isSameLoc(wp.getLocation(), to, true)) {
                openWaypointMenu(p, wp, true, true, false);
                return;
            }
        }

        for (Waypoint wp : pm.getData().getWaypointsForPlayer(p.getUniqueId())) {
            if (Util.isSameLoc(wp.getLocation(), to, true)) {
                openWaypointMenu(p, wp, false, true, false);
                return;
            }
        }
    }

    public void openWaypointMenu(Player p, Waypoint currentWaypoint, boolean addServerWaypoints,
            boolean addHomeWaypoints, boolean select) {
        Msg.OPEN_WP_MENU.sendTo(p, currentWaypoint.getName());
        p.teleport(currentWaypoint.getLocation(), TeleportCause.PLUGIN);
        pm.openWaypointMenu(p, currentWaypoint, addServerWaypoints, addHomeWaypoints, select);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        pm.getData().loadWaypointsForPlayer(joinEvent.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        pm.clearSelectedWaypoint(quitEvent.getPlayer().getName());
        pm.getData().unloadWaypointsForPlayer(quitEvent.getPlayer().getUniqueId());
    }

}
