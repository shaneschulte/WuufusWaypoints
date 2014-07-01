package com.github.marenwynn.waypoints.tasks;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.marenwynn.waypoints.PluginMain;
import com.github.marenwynn.waypoints.Util;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;

public class TeleportTask extends BukkitRunnable implements Listener {

    private PluginMain pm;

    private Player     p;
    private Waypoint   wp;

    private int        counter;
    private float      flySpeed, walkingSpeed;

    public TeleportTask(PluginMain pm, Player p, Waypoint wp) {
        this.pm = pm;
        this.p = p;
        this.wp = wp;

        counter = 5;
        flySpeed = p.getFlySpeed();
        walkingSpeed = p.getWalkSpeed();

        p.setFlySpeed(0);
        p.setWalkSpeed(0);
        p.setCanPickupItems(false);

        pm.getServer().getPluginManager().registerEvents(this, pm);
    }

    public void destroy() {
        if (p != null) {
            p.setFlySpeed(flySpeed);
            p.setWalkSpeed(walkingSpeed);
            p.setCanPickupItems(true);
        }

        HandlerList.unregisterAll(this);

        counter = 0;
        p = null;
        wp = null;
    }

    @Override
    public void run() {
        switch (counter) {
            case 5:
                Msg.PORT_TASK_1.sendTo(p, wp.getName(), p.getName());
                break;
            case 4:
                Msg.PORT_TASK_2.sendTo(p);
                break;
            case 3:
                Msg.PORT_TASK_3.sendTo(p);
                break;
            case 2:
                break;
            case 1:
                Msg.PORT_TASK_4.sendTo(p);

                Location from = p.getLocation();
                from.setY(from.getY() + 2);

                Location to = wp.getLocation();
                to.setY(to.getY() + 2);

                from.getWorld().strikeLightningEffect(from);
                p.teleport(wp.getLocation(), TeleportCause.COMMAND);
                to.getWorld().strikeLightningEffect(to);
                break;
            default:
                destroy();
                break;
        }

        if (counter > 0) {
            p.getWorld().playEffect(p.getLocation(), Effect.ENDER_SIGNAL, 16);
            pm.getServer().getScheduler().runTaskLater(pm, this, 20L);
        }

        counter--;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerMove(PlayerMoveEvent moveEvent) {
        if (p == null || !p.equals(moveEvent.getPlayer()))
            return;

        if (!Util.isSameLoc(moveEvent.getFrom(), moveEvent.getTo(), false)) {
            Location from = moveEvent.getFrom();

            from.setX(from.getBlockX());
            from.setY(from.getBlockY());
            from.setZ(from.getBlockZ());

            p.teleport(from, TeleportCause.PLUGIN);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerDamage(EntityDamageEvent damageEvent) {
        if (p == null || !p.getUniqueId().equals(damageEvent.getEntity().getUniqueId()) || counter < 2)
            return;

        Msg.DAMAGE_CANCEL.sendTo(p);
        destroy();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onPlayerCommand(PlayerCommandPreprocessEvent cmdEvent) {
        if (p != null && p.equals(cmdEvent.getPlayer())) {
            Msg.COMMAND_CANCEL.sendTo(p);
            cmdEvent.setCancelled(true);
        }
    }

    @EventHandler
    void onItemDrop(PlayerDropItemEvent dropEvent) {
        if (p != null && p.equals(dropEvent.getPlayer()))
            dropEvent.setCancelled(true);
    }

}
