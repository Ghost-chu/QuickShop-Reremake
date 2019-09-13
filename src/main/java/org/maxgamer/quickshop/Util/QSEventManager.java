package org.maxgamer.quickshop.Util;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * The EventManager for QuickShop protection caller.
 */
public class QSEventManager {
    private HashMap<String, Set<PluginEventFilterContainer>> filterSet = new HashMap<>();
    private QuickShop plugin;

    public QSEventManager(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        List<String> filterConfig = plugin.getConfig().getStringList("shop.protection-checking-filter");
        for (String filterString : filterConfig) {
            String[] filterArgs = filterString.split("##");
            if (filterArgs.length != 3) {
                plugin.getLogger().warning("Event filter: " + filterString + " is a invalid filter.");
                continue;
            }
            String cPlugin = filterArgs[0];
            String cListener = filterArgs[1];
            String cEvent = filterArgs[2];
            if (cPlugin == null || cPlugin.isEmpty()) {
                plugin.getLogger().warning("Event filter: " + filterString + " plugin invalid.");
                continue;
            }
            if (cListener == null || cListener.isEmpty()) {
                plugin.getLogger().warning("Event filter: " + filterString + " listener invalid.");
                continue;
            }
            if (cEvent == null || cEvent.isEmpty()) {
                plugin.getLogger().warning("Event filter: " + filterString + " plugin name invalid.");
                continue;
            }
            filterSet.putIfAbsent(cPlugin, new HashSet<>());
            Set<PluginEventFilterContainer> containers = filterSet.get(cPlugin);
            boolean result = containers.add(new PluginEventFilterContainer(cListener, cEvent));
            if (!result) {
                plugin.getLogger().warning("Event filter: " + filterString + " duplicated.");
            } else {
                filterSet.put(cPlugin, containers);
            }
        }
    }

    /**
     * Same in Bukkit plugin manager, just we use custom filter.
     *
     * @param event The event calling by us.
     */
    public @Nullable Plugin fireEvent(@NotNull Event event) {
        HandlerList handlers = event.getHandlers();
        RegisteredListener[] listeners = handlers.getRegisteredListeners();
        for (RegisteredListener registration : listeners) {
            if (!registration.getPlugin().isEnabled()) {
                Util.debugLog("Skipping " + registration.getPlugin().getName() + " cause plugin is unloaded.");
                continue;
            }
            Set<PluginEventFilterContainer> containers = filterSet.get(registration.getPlugin().getName());
            if (containers == null) {
                Util.debugLog("Container is null, direct call events...");
            } else {
                Util.debugLog("Detected the filter for this plugin, checking...");
                boolean cancelPluginCalling = false;
                for (PluginEventFilterContainer disabledContainer : containers) {
                    if (disabledContainer.getListener().equals(registration.getListener().getClass().getName())) {
                        if (disabledContainer.getEvent().equals(event.getClass().getName())) {
                            Util.debugLog("Skipping event " + event.getClass().getName() + " calling for plugin " + registration.getPlugin().getName() + "'s listener " + registration.getListener().getClass().getName());
                            cancelPluginCalling = true;
                            break;
                        }
                    }
                }
                if (cancelPluginCalling) {
                    continue; //Skip this listener calling+
                }
            }
            Util.debugLog("Calling event " + event.getClass().getName() + " calling for plugin " + registration.getPlugin().getName() + "'s listener " + registration.getListener().getClass().getName());
            try {
                registration.callEvent(event);
                if (event instanceof Cancellable) {
                    if (((Cancellable) event).isCancelled()) {
                        Util.debugLog("Plugin " + registration.getPlugin() + " cancelled the check event, stop checking.");
                        return registration.getPlugin();
                    }
                }
            } catch (AuthorNagException ex) {
                Plugin plugin = registration.getPlugin();
                if (plugin.isNaggable()) {
                    plugin.setNaggable(false);
                    Bukkit.getServer().getLogger().log(Level.SEVERE, String.format(
                            "Nag author(s): '%s' of '%s' about the following: %s",
                            plugin.getDescription().getAuthors(),
                            plugin.getDescription().getFullName(),
                            ex.getMessage()
                    ));
                }
            } catch (Throwable ex) {
                Bukkit.getServer().getLogger().log(Level.SEVERE, "Could not pass event " + event.getEventName() + " to " + registration.getPlugin().getDescription().getFullName(), ex);
                plugin.getLogger().info("A plugin throw an error when checking protection permission, you should check them, DO NOT REPORT THIS TO QUICKSHOP.");
            }
        }
        return null;
    }
}

@AllArgsConstructor
@Data
class PluginEventFilterContainer {
    private @NotNull String listener;
    private @NotNull String event;
}
