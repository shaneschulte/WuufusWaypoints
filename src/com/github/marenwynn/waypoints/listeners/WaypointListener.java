package com.github.marenwynn.waypoints.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.github.marenwynn.waypoints.Util;
import com.github.marenwynn.waypoints.data.Msg;

public class WaypointListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent moveEvent) {
        if (!moveEvent.getPlayer().hasMetadata("Wayporting"))
            return;

        if (!Util.isSameLoc(moveEvent.getFrom(), moveEvent.getTo(), false)) {
            Location from = moveEvent.getFrom();

            from.setX(from.getBlockX());
            from.setY(from.getBlockY());
            from.setZ(from.getBlockZ());

            moveEvent.getPlayer().teleport(from, TeleportCause.PLUGIN);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent cmdEvent) {
        if (cmdEvent.getPlayer().hasMetadata("Wayporting")) {
            Msg.COMMAND_CANCEL.sendTo(cmdEvent.getPlayer());
            cmdEvent.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer().hasMetadata("Wayporting"))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDrop(PlayerDropItemEvent dropEvent) {
        if (dropEvent.getPlayer().hasMetadata("Wayporting"))
            dropEvent.setCancelled(true);
    }

}
