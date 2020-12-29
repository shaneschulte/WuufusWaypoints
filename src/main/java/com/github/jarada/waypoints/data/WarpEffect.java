package com.github.jarada.waypoints.data;

import com.github.jarada.waypoints.PluginMain;
import com.github.jarada.waypoints.Util;
import org.bukkit.*;

import java.util.Arrays;

public enum WarpEffect {

    BLAZE,
    BUBBLE,
    ENDER,
    NETHER,
    QUIET,
    THUNDER;

    public void playLoadingEffectAtLocation(Location location) {
        if (location == null)
            return;
        
        if (this == ENDER || this == NETHER || this == THUNDER)
            Util.playSound(location, Sound.BLOCK_PORTAL_TRIGGER);
    }

    public void playTickEffectAtLocation(Location location, int tick, boolean to) {
        if (location == null)
            return;

        if (!to) {
            if (this == BLAZE) {
                Util.playSound(location, Sound.ENTITY_BLAZE_BURN);
                Util.playEffect(location, Effect.MOBSPAWNER_FLAMES);
                if (Arrays.stream(Particle.values()).anyMatch(t -> t.name().equals("CAMPFIRE_SIGNAL_SMOKE"))) {
                    Util.playParticle(location, Particle.CAMPFIRE_SIGNAL_SMOKE, 50);
                }
                return;
            }

            if (this == BUBBLE) {
                Util.playSound(location, Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT);
                Util.playParticle(location, Particle.NAUTILUS, 50);
                return;
            }

            if (this == NETHER) {
                Util.playParticle(location, Particle.PORTAL, 50);
                return;
            }

            Util.playEffect(location, Effect.ENDER_SIGNAL);
        } else {
            if (tick == 4) {
                if (this == BUBBLE) {
                    Util.playParticle(location, Particle.NAUTILUS, 50);
                }
                if (this == NETHER) {
                    Util.playParticle(location, Particle.PORTAL, 50);
                }
            }
        }
    }

    public void playWarpingEffectAtLocation(Location location, boolean to) {
        if (location == null)
            return;
        
        switch (this) {
            case BLAZE:
                Util.playSound(location, Sound.ENTITY_BLAZE_AMBIENT);
                break;
            case BUBBLE:
                Util.playSound(location, Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP);
                break;
            case ENDER:
                Util.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT);
                break;
            case NETHER:
                Util.playSound(location, Sound.ENTITY_GHAST_SHOOT);
                break;
            case THUNDER:
                location.getWorld().strikeLightningEffect(location);
                break;
            default:
                break;
        }
    }

}
