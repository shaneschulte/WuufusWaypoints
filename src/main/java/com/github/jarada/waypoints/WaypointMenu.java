package com.github.jarada.waypoints;

import com.github.jarada.waypoints.data.DataManager;
import com.github.jarada.waypoints.data.PlayerData;
import com.github.jarada.waypoints.data.Waypoint;
import com.github.jarada.waypoints.tasks.TeleportTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.jarada.waypoints.data.Msg;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WaypointMenu implements Listener {

    private PluginMain     pm;

    private Player         p;
    private PlayerData     pd;
    private Waypoint currentWaypoint;
    private List<Waypoint> accessList;
    private boolean        select;
    private boolean        fromBeacon;

    private int            page;
    private int            size;
    private String[]       optionNames;
    private ItemStack[]    optionIcons;
    private Waypoint[]     optionWaypoints;

    public WaypointMenu(Player p, PlayerData pd, Waypoint currentWaypoint, List<Waypoint> accessList, boolean select) {
        pm = PluginMain.getPluginInstance();

        this.p = p;
        this.pd = pd;
        this.select = select;
        this.currentWaypoint = currentWaypoint;
        this.accessList = accessList;
        this.fromBeacon = !select && currentWaypoint == null;

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
        if (!Arrays.equals(clickEvent.getInventory().getContents(), optionIcons)
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
                        SelectionManager.getManager().setSelectedWaypoint(p, optionWaypoints[slot]);
                    else
                        Bukkit.getScheduler().runTask(pm, new TeleportTask(p, optionWaypoints[slot]));

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
            } else if (optionNames[slot].equals("Silence")) {
                pd.setSilentWaypoints(!pd.isSilentWaypoints());
                DataManager.getManager().savePlayerData(pd.getUUID());
            }

            buildMenu();
            clickEvent.getInventory().setContents(optionIcons);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onInventoryClose(InventoryCloseEvent closeEvent) {
        if (Arrays.equals(closeEvent.getInventory().getContents(), optionIcons) && p == closeEvent.getPlayer()) {
            Bukkit.getScheduler().runTask(pm, new Runnable() {

                @Override
                public void run() {
                    destroy();
                }

            });
        }
    }

    public void buildMenu() {
        size = accessList.size() > 9 || fromBeacon ? 18 : 9;
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

        if (fromBeacon) {
            ItemStack is = new ItemStack(Material.LEATHER_BOOTS, 1);

            List<String> lore = new ArrayList<>();
            lore.add(Util.color(pd.isSilentWaypoints() ? "&4Silenced" : "&2Active"));

            Util.setItemNameAndLore(is, Util.color("&aShow Waypoints on Walk"), lore);
            setOption(17, "Silence", is);
        }
    }

    public void setOption(int slot, String name, ItemStack icon) {
        optionNames[slot] = name;
        optionIcons[slot] = icon;
        optionWaypoints[slot] = null;
    }

    public void setOption(int slot, Waypoint wp, boolean selected) {
        Location loc = wp.getLocation();
        String displayName = "&6" + wp.getName();

        if (!wp.isEnabled() && WaypointManager.getManager().isServerDefined(wp))
            displayName += " &f[&cDisabled&f]";

        List<String> lore = new ArrayList<String>();
        lore.add(Util.color(String.format("&f&o(%s)", loc.getWorld().getName())));
        lore.add(Util.color(String.format("&aX: &f%s", loc.getBlockX())));
        lore.add(Util.color(String.format("&aY: &f%s", loc.getBlockY())));
        lore.add(Util.color(String.format("&aZ: &f%s", loc.getBlockZ())));

        if (wp.getDescription().length() > 0)
            lore.addAll(Arrays.asList(Util.getWrappedLore(wp.getDescription(), 25)));

        optionNames[slot] = Util.color(displayName);

        if (currentWaypoint != null && selected)
            optionNames[slot] = Util.color("&a* ") + optionNames[slot];

        ItemStack icon = new ItemStack(wp.getIcon(), 1);
        ItemMeta meta = icon.getItemMeta();
        if (meta instanceof Damageable) {
            ((org.bukkit.inventory.meta.Damageable)meta).setDamage(wp.getDurability());
            icon.setItemMeta(meta);
        }

        optionIcons[slot] = Util.setItemNameAndLore(icon, optionNames[slot], lore);

        if ((wp.isEnabled() || p.hasPermission("wp.bypass")) || select)
            optionWaypoints[slot] = wp;
    }

}
