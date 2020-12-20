package com.github.jarada.waypoints;

import com.github.jarada.waypoints.data.*;
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

import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class WaypointMenu implements Listener {

    private PluginMain            pm;

    private Player                p;
    private PlayerData            pd;
    private Waypoint              currentWaypoint;
    private List<WaypointHolder>  accessList;
    private boolean               select;
    private boolean               fromBeacon;

    private int                   page;
    private int                   size;
    private int                   dataSize;
    private MenuSize              menuSize;
    private String[]              optionNames;
    private ItemStack[]           optionIcons;
    private Waypoint[]            optionWaypoints;

    public WaypointMenu(Player p, PlayerData pd, Waypoint currentWaypoint, List<WaypointHolder> accessList, boolean select) {
        pm = PluginMain.getPluginInstance();

        this.p = p;
        this.pd = pd;
        this.select = select;
        this.currentWaypoint = currentWaypoint;
        this.accessList = accessList;
        this.fromBeacon = !select && currentWaypoint == null;
        this.menuSize = DataManager.getManager().MENU_SIZE;

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
            Optional<WaypointHolder> holder = accessList.stream()
                    .filter(x -> x.getWaypoint() == optionWaypoints[slot])
                    .findFirst();
            if (holder.isPresent() && holder.get().isDiscoverable())
                return;
            
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
        size = menuSize.getFullSize(accessList.size(), accessList.size() > MenuSize.STEP_SIZE || fromBeacon);
        dataSize = menuSize.getDataSize(accessList.size());
        optionNames = new String[size];
        optionIcons = new ItemStack[size];
        optionWaypoints = new Waypoint[size];

        for (int slot = 0; slot < dataSize; slot++) {
            int index = ((page - 1) * dataSize) + slot;

            if (index > accessList.size() - 1)
                break;

            WaypointHolder holder = accessList.get(index);
            Waypoint wp = holder.getWaypoint();
            setOption(slot, wp, holder.isDiscoverable(), wp == currentWaypoint);
        }

        if (page > 1) {
            ItemStack is = new ItemStack(Material.PAPER, 1);
            Util.setItemNameAndLore(is, Util.color(Msg.MENU_PAGE_PREVIOUS.toString()), null);
            setOption(dataSize + 3, "Previous", is);
        }

        if (accessList.size() > dataSize) {
            ItemStack is = new ItemStack(Material.BOOK, page);
            Util.setItemNameAndLore(is, Util.color(String.format(Msg.MENU_PAGE.toString() + ": &6%d", page)), null);
            setOption(dataSize + 4, "Page", is);
        }

        if (accessList.size() > page * dataSize) {
            ItemStack is = new ItemStack(Material.PAPER, 1);
            Util.setItemNameAndLore(is, Util.color(Msg.MENU_PAGE_NEXT.toString()), null);
            setOption(dataSize + 5, "Next", is);
        }

        if (fromBeacon) {
            ItemStack is = new ItemStack(Material.LEATHER_BOOTS, 1);

            List<String> lore = new ArrayList<>();
            lore.add(Util.color(pd.isSilentWaypoints() ? Msg.MENU_WALK_SILENCED.toString() : Msg.MENU_WALK_ACTIVE.toString()));

            Util.setItemNameAndLore(is, Util.color(Msg.MENU_WALK.toString()), lore);
            setOption(dataSize + 8, "Silence", is);
        }
    }

    public void setOption(int slot, String name, ItemStack icon) {
        optionNames[slot] = name;
        optionIcons[slot] = icon;
        optionWaypoints[slot] = null;
    }

    public void setOption(int slot, Waypoint wp, boolean discoverable, boolean selected) {
        Location loc = wp.getLocation();
        String displayName = "&6" + wp.getName();

        if (!wp.isEnabled() && WaypointManager.getManager().isServerDefined(wp))
            displayName += " &f[&c" + Msg.WORD_DISABLED.toString() + "&f]";

        List<String> lore = new ArrayList<String>();
        if (discoverable) {
            lore.add(Util.color(Msg.DISCOVERABLE_WAYPOINT.toString()));

            if (wp.getHint().length() > 0)
                lore.addAll(Arrays.asList(Util.getWrappedLore(wp.getHint(), 25)));
        } else {
            lore.add(Util.color(String.format("&f&o(%s)", loc.getWorld().getName())));
            lore.add(Util.color(String.format("&aX: &f%s", loc.getBlockX())));
            lore.add(Util.color(String.format("&aY: &f%s", loc.getBlockY())));
            lore.add(Util.color(String.format("&aZ: &f%s", loc.getBlockZ())));

            if (wp.getDescription().length() > 0)
                lore.addAll(Arrays.asList(Util.getWrappedLore(wp.getDescription(), 25)));
        }

        optionNames[slot] = Util.color(displayName);

        if (currentWaypoint != null && selected)
            optionNames[slot] = Util.color("&a* ") + optionNames[slot];

        ItemStack icon = new ItemStack((discoverable) ? Material.LIGHT_GRAY_STAINED_GLASS_PANE : wp.getIcon(), 1);
        ItemMeta meta = icon.getItemMeta();
        if (!discoverable && meta instanceof Damageable) {
            ((org.bukkit.inventory.meta.Damageable)meta).setDamage(wp.getDurability());
            icon.setItemMeta(meta);
        }

        optionIcons[slot] = Util.setItemNameAndLore(icon, optionNames[slot], lore);

        if ((wp.isEnabled() || p.hasPermission("wp.bypass")) || select)
            optionWaypoints[slot] = wp;
    }

}
