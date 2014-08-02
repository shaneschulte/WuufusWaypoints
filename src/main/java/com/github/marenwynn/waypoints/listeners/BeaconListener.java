package com.github.marenwynn.waypoints.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import com.github.marenwynn.waypoints.WaypointManager;
import com.github.marenwynn.waypoints.events.BeaconUseEvent;

public class BeaconListener implements Listener {

    private static BeaconListener listener;

    public static BeaconListener getListener() {
        if (listener == null)
            listener = new BeaconListener();

        return listener;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBeaconUse(BeaconUseEvent useEvent) {
        Player p = useEvent.getPlayer();
        Action a = useEvent.getAction();
        ItemStack is = p.getItemInHand();

        if (p.hasPermission("wp.beacon.use") && (a == Action.RIGHT_CLICK_BLOCK || a == Action.RIGHT_CLICK_AIR)) {
            if (!p.hasPermission("wp.beacon.unlimited")) {
                is.setAmount(is.getAmount() - 1);
                p.setItemInHand(is);
            }

            WaypointManager.getManager().openWaypointMenu(p, null, p.hasPermission("wp.beacon.server"), true, false);
        } else if (p.hasPermission("wp.select") && (a == Action.LEFT_CLICK_BLOCK || a == Action.LEFT_CLICK_AIR)) {
            WaypointManager.getManager().openWaypointSelectionMenu(p);
        }
    }

}
