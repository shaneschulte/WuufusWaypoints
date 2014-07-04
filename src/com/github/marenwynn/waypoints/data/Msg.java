package com.github.marenwynn.waypoints.data;

import org.bukkit.command.CommandSender;

import com.github.marenwynn.waypoints.Util;

public enum Msg {

    BORDER("&2------------------------------"),
    CMD_NO_CONSOLE("&cError: &fCommand unavailable to CONSOLE."),
    COMMAND_CANCEL("&cUnable to act while undergoing quantization."),
    DAMAGE_CANCEL("&cTransmission interrupted by kinetic interference."),
    DISCOVERED_WAYPOINT("Discovered &6%s&f! Directory updated with new entry."),
    DISCOVERY_MODE_DISABLED("&6%s: &fDiscovery mode disabled."),
    DISCOVERY_MODE_ENABLED("&6%s: &fDiscovery mode enabled."),
    HOME_WP_ALREADY_HERE("&cError: &fHome waypoint &6%s &falready lies at this location."),
    HOME_WP_CREATED("&aSuccess: &fCoordinates received for &6%s&f."),
    HOME_WP_REPLACED("&aSuccess: &fOverflow; &6%s &freplaced with coordinates to &6%s&f."),
    INVALD_DURABILITY("&cError: &fInvalid durability value."),
    INVALID_MATERIAL("&cError: &fInvalid material type."),
    LIST_HOME_WAYPOINTS("&aHome Waypoints: &6%s"),
    LORE_LINE(" &5&o%s"),
    MAX_LENGTH_EXCEEDED("&cMax length (&f%s&c) exceeded."),
    MENU_NAME("Waypoint Directory"),
    NO_PERMS("&cError: &fAccess denied."),
    NO_WAYPOINTS("&fNone"),
    ONLY_SERVER_DEFINED("&cError: &fOnly server-defined waypoints can be discoverable."),
    OPEN_WP_MENU("&6%s: &fOpening waypoint directory."),
    PORT_TASK_1("&aPlotting course for &6%s&a. Scanning &6%s&a..."),
    PORT_TASK_2("&a ...scan complete. Course locked in."),
    PORT_TASK_3("&aSynchronizing qubits; standby for quantization."),
    PORT_TASK_4("&9Energizing..."),
    RELOADED("&aWaypoints &freloaded."),
    REMOTELY_ACCESSED("&aWaypoint Beacon: &fEstablished remote connection to waypoint directory."),
    SELECTED_1(" &6%s &f&o(%s)"),
    SELECTED_2(" &aX: &f%d  &aPitch: &f%d"),
    SELECTED_3(" &aY: &f%d  &aYaw: &f%d"),
    SELECTED_4(" &aZ: &f%d"),
    SETHOME_DEFAULT_DESC("User-defined"),
    SET_SPAWN("&fSpawn location updated for world &6%s&f."),
    USAGE_SETICON("&cUsage: &f/wp icon <Material>"),
    USAGE_SETHOME("&cUsage: &f/sethome <name ...>"),
    USAGE_WP_RENAME("&cUsage: &f/wp rename <name ...>"),
    USAGE_WP_ADD("&cUsage: &f/wp add <name ...>"),
    WP_ALREADY_HERE("&cError: &fCoordinates overlap with &6%s&f."),
    WP_DESC_CLEARED("&6%s: &fDescription cleared."),
    WP_DESC_UPDATED("&6%s: &fDescription updated."),
    WP_DESC_UPDATED_BOOK("&6%s: &fScanned &a%s&f. Directory data updated."),
    WP_DUPLICATE_NAME("&cError: &fA duplicate entry for \"%s\" was found."),
    WP_LIST("&aWaypoints: &6"),
    WP_LOCATION_UPDATED("&6%s: &fLocation updated."),
    WP_NO_DESC(" &oNo description."),
    WP_NOT_EXIST("&cError: &fWaypoint \"%s\" not found."),
    WP_NOT_SELECTED_ERROR("&cError: &fNo waypoint selected."),
    WP_NOT_SELECTED_ERROR_USAGE("&cUse \"&f/wp select <name>&c\" to select a waypoint."),
    WP_REMOVED("&6%s &fremoved from directory."),
    WP_RENAMED("&6%s &frenamed to &6%s&f."),
    WP_SELECTED("&fWaypoint &6%s &fselected."),
    WP_SETICON("&6%s: &fDirectory icon set to &a%s&f:&a%s&f.");

    private final String defaultMsg;

    Msg(String defaultMsg) {
        this.defaultMsg = defaultMsg;
    }

    public String getDefaultMsg() {
        return defaultMsg;
    }

    @Override
    public String toString() {
        return Database.getMsg(this);
    }

    public void sendTo(CommandSender sender) {
        sender.sendMessage(toString());
    }

    public void sendTo(CommandSender sender, Object... args) {
        sender.sendMessage(Util.color(String.format(toString(), args)));
    }

}
