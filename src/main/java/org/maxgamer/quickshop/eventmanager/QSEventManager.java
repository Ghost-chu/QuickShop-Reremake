/*
 * This file is a part of project QuickShop, the name is QSEventManager.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.eventmanager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class QSEventManager implements QuickEventManager, Listener {
    private final QuickShop plugin;
    private final List<ListenerContainer> ignoredListener = new ArrayList<>();

    public QSEventManager(QuickShop plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void serverReloaded(ServerLoadEvent event) {
        this.rescan();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void pluginDisable(PluginDisableEvent event) {
        this.rescan();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void pluginEnable(PluginEnableEvent event) {
        this.rescan();
    }

    private void rescan() {
        this.ignoredListener.clear();
        plugin
                .getConfig()
                .getStringList("shop.protection-checking-listener-blacklist")
                .forEach(
                        input -> {
                            if (StringUtils.isEmpty(input)) {
                                return;
                            }
                            try {
                                Class<?> clazz = Class.forName(input);
                                this.ignoredListener.add(new ListenerContainer(clazz, input));
                                Util.debugLog("Successfully added blacklist: " + clazz.getName());
                            } catch (ClassNotFoundException ignored) {
                                Util.debugLog("Failed to add blacklist (not found): " + input);
                            }
                        });
    }

    public void callEvent(Event event) {
        if (event.isAsynchronous()) {
            if (Thread.holdsLock(Bukkit.getPluginManager())) {
                throw new IllegalStateException(
                        event.getEventName()
                                + " cannot be triggered asynchronously from inside synchronized code.");
            }
            if (Bukkit.getServer().isPrimaryThread()) {
                throw new IllegalStateException(
                        event.getEventName()
                                + " cannot be triggered asynchronously from primary server thread.");
            }
        } else {
            if (!Bukkit.getServer().isPrimaryThread()) {
                throw new IllegalStateException(
                        event.getEventName() + " cannot be triggered asynchronously from another thread.");
            }
        }

        fireEvent(event);
    }

    private void fireEvent(Event event) {
        HandlerList handlers = event.getHandlers();
        RegisteredListener[] listeners = handlers.getRegisteredListeners();

        for (RegisteredListener registration : listeners) {
            if (!registration.getPlugin().isEnabled()) {
                continue;
            }

            for (ListenerContainer container : this.ignoredListener) {
                if (container.matches(registration.getClass())) {
                    Util.debugLog(
                            "Skipped "
                                    + registration.getPlugin().getName()
                                    + " : "
                                    + registration.getListener().getClass().toString()
                                    + " listener.");
                    continue;
                }
            }

            try {
                registration.callEvent(event);
            } catch (AuthorNagException ex) {
                Plugin plugin = registration.getPlugin();

                if (plugin.isNaggable()) {
                    plugin.setNaggable(false);

                    Bukkit.getServer()
                            .getLogger()
                            .log(
                                    Level.SEVERE,
                                    String.format(
                                            "Nag author(s): '%s' of '%s' about the following: %s",
                                            plugin.getDescription().getAuthors(),
                                            plugin.getDescription().getFullName(),
                                            ex.getMessage()));
                }
            } catch (Throwable ex) {
                Bukkit.getServer()
                        .getLogger()
                        .log(
                                Level.SEVERE,
                                "Could not pass event "
                                        + event.getEventName()
                                        + " to "
                                        + registration.getPlugin().getDescription().getFullName(),
                                ex);
            }
        }
    }
}

@Data
@AllArgsConstructor
@Builder
class ListenerContainer {
    @Nullable
    private Class<?> clazz;
    @NotNull
    private String clazzName;

    public boolean matches(Class<?> matching) {
        if (clazz != null) {
            if (matching.equals(clazz)) {
                return true;
            }
        }
        if (matching.getName().equalsIgnoreCase(clazzName)) {
            return true;
        }
        if (matching.getName().startsWith(clazzName)) {
            return true;
        }
        return matching.getName().matches(clazzName);
    }
}
