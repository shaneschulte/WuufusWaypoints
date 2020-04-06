package com.github.jarada.waypoints.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Serializer {

    public static void set(YamlConfiguration config, String prefix, String name, Object value) {
        config.set(setupPrefix(prefix) + name, value);
    }

    public static boolean getBoolean(YamlConfiguration config, String prefix, String name) {
        return config.getBoolean(setupPrefix(prefix) + name);
    }

    public static double getDouble(YamlConfiguration config, String prefix, String name) {
        return config.getDouble(setupPrefix(prefix) + name);
    }

    public static float getFloat(YamlConfiguration config, String prefix, String name) {
        return (float) config.getDouble(setupPrefix(prefix) + name);
    }

    public static int getInt(YamlConfiguration config, String prefix, String name) {
        return config.getInt(setupPrefix(prefix) + name);
    }

    public static short getShort(YamlConfiguration config, String prefix, String name) {
        return (short) config.getDouble(setupPrefix(prefix) + name);
    }

    public static String getString(YamlConfiguration config, String prefix, String name) {
        return config.getString(setupPrefix(prefix) + name);
    }

    public static UUID getUUID(YamlConfiguration config, String prefix, String name) {
        try {
            return UUID.fromString(Objects.requireNonNull(getString(config, prefix, name)));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ConfigurationSection getConfigurationSection(YamlConfiguration config, String prefix, String name) {
        if (name == null)
            return config.getConfigurationSection(clearPrefix(prefix));
        return config.getConfigurationSection(setupPrefix(prefix) + name);
    }

    public static List<?> getList(YamlConfiguration config, String prefix, String name) {
        return config.getList(setupPrefix(prefix) + name);
    }

    public static boolean isList(YamlConfiguration config, String prefix, String name) {
        return config.isList(setupPrefix(prefix) + name);
    }

    public static String setupPrefix(String prefix) {
        if (!prefix.endsWith(".")) {
            return prefix + ".";
        }
        return prefix;
    }

    public static String clearPrefix(String prefix) {
        if (prefix.endsWith(".")) {
            return prefix + ".";
        }
        return prefix;
    }

}
