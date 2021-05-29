/*
 * This file is a part of project QuickShop, the name is AsyncPacketSender.java
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
package org.maxgamer.quickshop.util;

import org.bukkit.scheduler.BukkitTask;
import org.maxgamer.quickshop.QuickShop;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class AsyncPacketSender {

    private static final LinkedBlockingQueue<Runnable> asyncPacketSendQueue = new LinkedBlockingQueue<>();
    private static BukkitTask asyncSendingTask;

    private AsyncPacketSender() {
    }

    public static void start(QuickShop plugin) {
        //lazy initialize
        if (asyncSendingTask == null || asyncSendingTask.isCancelled()) {
            asyncSendingTask = plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                while (true) {
                    try {
                        // Add time out so we can enter next loop to check task cancelled.
                        Runnable nextTask = asyncPacketSendQueue.poll(1, TimeUnit.SECONDS);
                        if (asyncSendingTask.isCancelled()) {
                            break; // End loop task
                        }
                        if (nextTask != null) {
                            nextTask.run();
                        }
                    } catch (InterruptedException e) {
                        plugin.getLogger().log(Level.WARNING, "AsyncSendingTask quitting incorrectly", e);
                    }
                }
            });
        }
    }

    public static void offer(Runnable runnable) {
        asyncPacketSendQueue.offer(runnable);
    }

    public static void stop() {
        if (asyncSendingTask != null && !asyncSendingTask.isCancelled()) {
            asyncSendingTask.cancel();
        }
        asyncPacketSendQueue.clear();
    }
}
