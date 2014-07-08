package com.github.marenwynn.waypoints.tasks;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.marenwynn.waypoints.PluginMain;
import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;

public class TeleportTask extends BukkitRunnable implements Listener {

    private PluginMain pm;

    private Player     p;
    private Waypoint   wp;

    private int        counter;
    private float      flySpeed, walkSpeed;

    public TeleportTask(PluginMain pm, Player p, Waypoint wp) {
        this.pm = pm;
        this.p = p;
        this.wp = wp;

        counter = 5;
        flySpeed = p.getFlySpeed();
        walkSpeed = p.getWalkSpeed();

        p.setFlySpeed(0);
        p.setWalkSpeed(0);
        p.setCanPickupItems(false);
        p.setMetadata("Wayporting", new FixedMetadataValue(pm, true));

        pm.getServer().getPluginManager().registerEvents(this, pm);
    }

    public void destroy() {
        if (p != null) {
            p.setFlySpeed(flySpeed);
            p.setWalkSpeed(walkSpeed);
            p.setCanPickupItems(true);
            p.removeMetadata("Wayporting", pm);
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
    void onPlayerDamage(EntityDamageEvent damageEvent) {
        if (p == null || !p.getUniqueId().equals(damageEvent.getEntity().getUniqueId()) || counter < 2)
            return;

        Msg.DAMAGE_CANCEL.sendTo(p);
        destroy();
    }

}
