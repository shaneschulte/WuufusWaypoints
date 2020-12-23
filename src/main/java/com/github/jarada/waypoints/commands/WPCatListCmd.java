package com.github.jarada.waypoints.commands;

import com.github.jarada.waypoints.WaypointManager;
import com.github.jarada.waypoints.data.Category;
import com.github.jarada.waypoints.data.Msg;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class WPCatListCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        List<Category> categories = new ArrayList<>(WaypointManager.getManager().getCategories().values());

        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                // pass
            }
        }

        int pages = (int) Math.ceil((double)categories.size() / (double)9);
        sender.sendMessage("Page " + page + " of " + pages);

        int index = page > 1 ? (page - 1) * 9 : 0;
        if (categories.size() > index) {
            categories.subList(index, Math.min(index + 9, categories.size())).forEach(category -> {
                Msg.WP_CATEGORY_ORDER.sendTo(sender, Integer.toString(category.getOrder()), category.getName());
            });
        }
    }

    @Override
    public boolean isConsoleExecutable() {
        return true;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.category.list");
    }
}
