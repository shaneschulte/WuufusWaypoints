package com.github.jarada.waypoints.data;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class MovementData  {

    private float      walkSpeed, flySpeed;

    public MovementData(Player p) {
        walkSpeed = p.getWalkSpeed();
        flySpeed = p.getFlySpeed();
    }

    public MovementData(float walkSpeed, float flySpeed) {
        this.walkSpeed = walkSpeed;
        this.flySpeed = flySpeed;
    }

    public MovementData(YamlConfiguration config, String prefix) {
        this.walkSpeed = Serializer.getFloat(config, prefix, "walk_speed");
        this.flySpeed = Serializer.getFloat(config, prefix, "fly_speed");
    }

    public void serialize(YamlConfiguration config, String prefix) {
        Serializer.set(config, prefix, "walk_speed", this.walkSpeed);
        Serializer.set(config, prefix, "fly_speed", this.flySpeed);
    }

    public float getWalkSpeed() {
        return walkSpeed;
    }

    public float getFlySpeed() {
        return flySpeed;
    }

}
