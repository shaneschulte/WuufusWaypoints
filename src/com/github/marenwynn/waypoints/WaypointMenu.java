package com.github.marenwynn.waypoints;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WaypointMenu implements Listener {

    private PluginMain  pm;

    private Player      p;
    private Waypoint    currentWaypoint;
    private boolean     select;

    private int         size;
    private String[]    optionNames;
    private ItemStack[] optionIcons;
    private Waypoint[]  optionWaypoints;

    public WaypointMenu(PluginMain pm, Player p, Waypoint currentWaypoint, List<Waypoint> accessList, boolean select) {
        this.pm = pm;

        this.p = p;
        this.currentWaypoint = currentWaypoint;
        this.select = select;

        this.size = (int) (Math.ceil((accessList.size()) / 9.0)) * 9;
        this.optionNames = new String[size];
        this.optionIcons = new ItemStack[size];
        this.optionWaypoints = new Waypoint[size];

        int index = 0;

        for (Waypoint wp : accessList) {
            setOption(index, wp, wp == currentWaypoint);
            index += 1;
        }

        pm.getServer().getPluginManager().registerEvents(this, pm);
    }

    public WaypointMenu setOption(int index, Waypoint wp, boolean selected) {
        Location loc = wp.getLocation();
        ArrayList<String> lore = new ArrayList<String>();

        lore.add(Util.color(String.format("&f&o(%s)", loc.getWorld().getName())));
        lore.add(Util.color(String.format("&aX: &f%s", loc.getBlockX())));
        lore.add(Util.color(String.format("&aY: &f%s", loc.getBlockY())));
        lore.add(Util.color(String.format("&aZ: &f%s", loc.getBlockZ())));

        if (!wp.getDescription().equals(""))
            lore.addAll(Arrays.asList(Util.getWrappedLore(wp.getDescription(), 25)));

        optionNames[index] = "&6" + wp.getName();

        if (selected)
            optionNames[index] = "&a* " + optionNames[index];

        ItemStack icon = new ItemStack(wp.getIcon(), 1);
        icon.setDurability(wp.getDurability());

        optionIcons[index] = setItemNameAndLore(icon, Util.color(optionNames[index]), lore);
        optionWaypoints[index] = wp;

        return this;
    }

    public void open() {
        Inventory waypointMenu = Bukkit.createInventory(p, size, Msg.MENU_NAME.toString());
        waypointMenu.setContents(optionIcons);
        p.openInventory(waypointMenu);
    }

    private void destroy() {
        HandlerList.unregisterAll(this);

        p = null;
        optionNames = null;
        optionIcons = null;
        optionWaypoints = null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onInventoryClick(InventoryClickEvent clickEvent) {
        if (clickEvent.getInventory().getTitle().equals(Msg.MENU_NAME.toString())
                && p == (Player) clickEvent.getWhoClicked()) {
            clickEvent.setCancelled(true);

            if (!clickEvent.isLeftClick())
                return;

            int slot = clickEvent.getRawSlot();

            if (slot < 0 || slot >= size || optionNames[slot] == null || currentWaypoint == optionWaypoints[slot])
                return;

            if (select)
                pm.setSelectedWaypoint(p, optionWaypoints[slot]);
            else
                pm.teleportPlayer(p, optionWaypoints[slot]);

            p.closeInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onInventoryClose(InventoryCloseEvent closeEvent) {
        if (closeEvent.getInventory().getTitle().equals(Msg.MENU_NAME.toString()) && p == closeEvent.getPlayer())
            destroy();
    }

    private ItemStack setItemNameAndLore(ItemStack item, String name, ArrayList<String> lore) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        im.setLore(lore);
        item.setItemMeta(im);
        return item;
    }

}