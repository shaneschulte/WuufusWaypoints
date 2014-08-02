package com.github.marenwynn.waypoints.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import com.github.marenwynn.waypoints.WaypointManager;
import com.github.marenwynn.waypoints.data.PlayerData;
import com.github.marenwynn.waypoints.data.Waypoint;

public class WaypointInteractEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean                  cancelled;

    private Player                   p;
    private Waypoint                 wp;
    private Action                   a;
    private ItemStack                item;
    private boolean                  powered;
    private boolean                  serverDefined;

    public WaypointInteractEvent(Player p, Waypoint wp, Action a, ItemStack item, boolean powered) {
        this.p = p;
        this.wp = wp;
        this.a = a;
        this.item = item;
        this.powered = powered;
        serverDefined = WaypointManager.getManager().isServerDefined(wp);
    }

    public Player getPlayer() {
        return p;
    }

    public PlayerData getPlayerData() {
        return WaypointManager.getManager().getPlayerData(p.getUniqueId());
    }

    public Waypoint getWaypoint() {
        return wp;
    }

    public Action getAction() {
        return a;
    }

    public ItemStack getItem() {
        return item;
    }

    public boolean isPowered() {
        return powered;
    }

    public boolean isServerDefined() {
        return serverDefined;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
