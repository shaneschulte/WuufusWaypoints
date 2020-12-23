package com.github.jarada.waypoints.data;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import java.util.UUID;

public class Category {

    private UUID        uuid;
    private String      name;
    private Material    icon;
    private short       durability;
    private int         order;
    private boolean     orderSet;

    public Category(String name) {
        this.name = name;
        setIcon(Material.ENCHANTED_BOOK);
    }

    public Category(YamlConfiguration config, String prefix) {
        uuid = Serializer.getUUID(config, prefix, "uuid");
        setName(Serializer.getString(config, prefix, "name"));
        setDurability(Serializer.getShort(config, prefix, "icon_damage"));
        setOrder(Serializer.getInt(config, prefix, "order"));
        String input = Serializer.getString(config, prefix, "icon");
        if (input != null) {
            setIcon(Material.matchMaterial(input));
        }
    }

    public void serialize(YamlConfiguration config, String prefix) {
        Serializer.set(config, prefix, "uuid", getUUID().toString());
        Serializer.set(config, prefix, "name", getName());
        Serializer.set(config, prefix, "icon", getIcon().getKey().toString());
        Serializer.set(config, prefix, "icon_damage", getDurability());
        Serializer.set(config, prefix, "order", getOrder());
    }

    public UUID getUUID() {
        if (uuid == null)
            uuid = UUID.randomUUID();

        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public short getDurability() {
        return durability;
    }

    public void setDurability(short durability) {
        this.durability = durability;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isOrderSet() {
        return orderSet;
    }

    public void setOrderSet(boolean orderSet) {
        this.orderSet = orderSet;
    }

}
