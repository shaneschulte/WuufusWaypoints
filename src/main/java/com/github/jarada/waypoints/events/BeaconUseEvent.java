package com.github.jarada.waypoints.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;

import com.github.jarada.waypoints.WaypointManager;
import com.github.jarada.waypoints.data.PlayerData;

public class BeaconUseEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean                  cancelled;

    private Player                   p;
    private Action                   a;

    public BeaconUseEvent(Player p, Action a) {
        this.p = p;
        this.a = a;
    }

    public Player getPlayer() {
        return p;
    }

    public PlayerData getPlayerData() {
        return WaypointManager.getManager().getPlayerData(p.getUniqueId());
    }

    public Action getAction() {
        return a;
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
