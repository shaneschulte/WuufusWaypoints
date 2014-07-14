package com.github.marenwynn.waypoints;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.marenwynn.waypoints.data.Msg;
import com.github.marenwynn.waypoints.data.Waypoint;
import com.github.marenwynn.waypoints.tasks.TeleportTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WaypointMenu implements Listener {

    private PluginMain     pm;

    private Player         p;
    private Waypoint       currentWaypoint;
    private List<Waypoint> accessList;
    private boolean        select;

    private int            page;
    private int            size;
    private String[]       optionNames;
    private ItemStack[]    optionIcons;
    private Waypoint[]     optionWaypoints;

    public WaypointMenu(Player p, Waypoint currentWaypoint, List<Waypoint> accessList, boolean select) {
        pm = PluginMain.instance;

        this.p = p;
        this.select = select;
        this.currentWaypoint = currentWaypoint;
        this.accessList = accessList;

        page = 1;
        buildMenu();
        Bukkit.getPluginManager().registerEvents(this, pm);
    }

    public void open() {
        Inventory waypointMenu = Bukkit.createInventory(p, size, Msg.MENU_NAME.toString());
        waypointMenu.setContents(optionIcons);
        p.openInventory(waypointMenu);
    }

    private void destroy() {
        p.removeMetadata("InMenu", pm);
        HandlerList.unregisterAll(this);

        pm = null;
        p = null;
        currentWaypoint = null;
        accessList = null;
        optionNames = null;
        optionIcons = null;
        optionWaypoints = null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onInventoryClick(InventoryClickEvent clickEvent) {
        if (!clickEvent.getInventory().getTitle().equals(Msg.MENU_NAME.toString())
                || p != (Player) clickEvent.getWhoClicked())
            return;

        clickEvent.setCancelled(true);

        if (!clickEvent.isLeftClick())
            return;

        final int slot = clickEvent.getRawSlot();

        if (slot < 0 || slot >= size || optionNames[slot] == null
                || (currentWaypoint != null && currentWaypoint == optionWaypoints[slot]))
            return;

        if (optionWaypoints[slot] != null) {
            Bukkit.getScheduler().runTask(pm, new Runnable() {

                @Override
                public void run() {
                    if (select)
                        Selections.setSelectedWaypoint(p, optionWaypoints[slot]);
                    else
                        new TeleportTask(p, optionWaypoints[slot]).runTask(pm);

                    p.closeInventory();
                }

            });
        } else {
            if (optionNames[slot].equals("Previous")) {
                page--;
            } else if (optionNames[slot].equals("Page") && page != 1) {
                page = 1;
            } else if (optionNames[slot].equals("Next")) {
                page++;
            }

            buildMenu();
            clickEvent.getInventory().setContents(optionIcons);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onInventoryClose(InventoryCloseEvent closeEvent) {
        if (closeEvent.getInventory().getTitle().equals(Msg.MENU_NAME.toString()) && p == closeEvent.getPlayer()) {
            Bukkit.getScheduler().runTask(pm, new Runnable() {

                @Override
                public void run() {
                    destroy();
                }

            });
        }
    }

    public void buildMenu() {
        size = accessList.size() > 9 ? 18 : 9;
        optionNames = new String[size];
        optionIcons = new ItemStack[size];
        optionWaypoints = new Waypoint[size];

        for (int slot = 0; slot < 9; slot++) {
            int index = ((page - 1) * 9) + slot;

            if (index > accessList.size() - 1)
                break;

            Waypoint wp = accessList.get(index);
            setOption(slot, wp, wp == currentWaypoint);
        }

        if (page > 1) {
            ItemStack is = new ItemStack(Material.PAPER, 1);
            Util.setItemNameAndLore(is, Util.color("&aPrevious Page"), null);
            setOption(12, "Previous", is);
        }

        if (accessList.size() > 9) {
            ItemStack is = new ItemStack(Material.BOOK, page);
            Util.setItemNameAndLore(is, Util.color(String.format("&aPage: &6%d", page)), null);
            setOption(13, "Page", is);
        }

        if (accessList.size() > page * 9) {
            ItemStack is = new ItemStack(Material.PAPER, 1);
            Util.setItemNameAndLore(is, Util.color("&aNext Page"), null);
            setOption(14, "Next", is);
        }
    }

    public void setOption(int slot, String name, ItemStack icon) {
        optionNames[slot] = name;
        optionIcons[slot] = icon;
        optionWaypoints[slot] = null;
    }

    public void setOption(int slot, Waypoint wp, boolean selected) {
        Location loc = wp.getLocation();
        List<String> lore = new ArrayList<String>();
        String enabled = WaypointManager.getAllWaypoints().contains(wp) ? (wp.isEnabled() ? "" : Util
                .color(" &f[&cDisabled&f]")) : "";

        lore.add(Util.color(String.format("&f&o(%s)", loc.getWorld().getName())));
        lore.add(Util.color(String.format("&aX: &f%s", loc.getBlockX())));
        lore.add(Util.color(String.format("&aY: &f%s", loc.getBlockY())));
        lore.add(Util.color(String.format("&aZ: &f%s", loc.getBlockZ())));

        if (!wp.getDescription().equals(""))
            lore.addAll(Arrays.asList(Util.getWrappedLore(wp.getDescription(), 25)));

        optionNames[slot] = "&6" + wp.getName() + enabled;

        if (currentWaypoint != null && selected)
            optionNames[slot] = "&a* " + optionNames[slot];

        ItemStack icon = new ItemStack(wp.getIcon(), 1);
        icon.setDurability(wp.getDurability());

        optionIcons[slot] = Util.setItemNameAndLore(icon, Util.color(optionNames[slot]), lore);

        if ((wp.isEnabled() || p.hasPermission("wp.bypass")) || select)
            optionWaypoints[slot] = wp;
    }

}
