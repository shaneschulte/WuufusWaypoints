package com.github.jarada.waypoints.data;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

public class Waypoint extends GridLocation {

    private UUID              uuid;
    private String            name, description, hint, category;
    private Material          icon;
    private short             durability;
    private Boolean           discoverable;
    private boolean           enabled;
    private UUID              creator;

    public Waypoint(String name, Location loc) {
        super(loc);
        setName(name);
        setDescription("");
        setHint("");
        setIcon(Material.IRON_DOOR);
        setEnabled(true);
        setDiscoverable(true);
    }

    public Waypoint(YamlConfiguration config, String prefix) {
        super(config, prefix);
        uuid = Serializer.getUUID(config, prefix, "uuid");
        setName(Serializer.getString(config, prefix, "name"));
        setDescription(Serializer.getString(config, prefix, "desc"));
        setHint(Serializer.getString(config, prefix, "hint"));
        if (hint == null)
            setHint("");
        setDurability(Serializer.getShort(config, prefix, "icon_damage"));
        setDiscoverable(Serializer.getBoolean(config, prefix, "discoverable"));
        setEnabled(Serializer.getBoolean(config, prefix, "enabled"));
        String input = Serializer.getString(config, prefix, "icon");
        if (input != null) {
            setIcon(Material.matchMaterial(input));
        }
        if (icon == null) {
            setIcon(Material.IRON_DOOR);
        }
        setCategory(Serializer.getString(config, prefix, "category"));
    }

    @Override
    public void serialize(YamlConfiguration config, String prefix) {
        super.serialize(config, prefix);
        Serializer.set(config, prefix, "uuid", getUUID().toString());
        Serializer.set(config, prefix, "name", getName());
        Serializer.set(config, prefix, "desc", getDescription());
        if (!hint.isEmpty())
            Serializer.set(config, prefix, "hint", getHint());
        Serializer.set(config, prefix, "icon", getIcon().getKey().toString());
        Serializer.set(config, prefix, "icon_damage", getDurability());
        Serializer.set(config, prefix, "discoverable", isDiscoverable());
        Serializer.set(config, prefix, "enabled", isEnabled());
        Serializer.set(config, prefix, "category", getCategory());
    }

    public UUID getUUID() {
        if (uuid == null)
            uuid = UUID.randomUUID();

        return uuid;
    }

    public UUID getCreator() {
        return creator;
    }

    public void setCreator(UUID creator) {
        this.creator = creator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return (description == null) ? "" : description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHint() {
        return (hint == null) ? "" : hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
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

    public Boolean isDiscoverable() {
        return discoverable;
    }

    public void setDiscoverable(Boolean discoverable) {
        this.discoverable = discoverable;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String categoryUUID) {
        this.category = categoryUUID;
    }

    public void setCategory(Category category) {
        this.category = category.getUUID().toString();
    }

    public void clearCategory() {
        this.category = null;
    }
}
