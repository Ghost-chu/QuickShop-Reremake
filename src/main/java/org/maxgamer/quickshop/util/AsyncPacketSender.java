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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class AsyncPacketSender {

    private static final Queue<Runnable> asyncPacketSendQueue = new ConcurrentLinkedQueue<>();
    private static BukkitTask asyncSendingTask;

    private AsyncPacketSender() {
    }

    public static void start(QuickShop plugin) {
        //lazy initialize
        if (asyncSendingTask == null || asyncSendingTask.isCancelled()) {
            asyncSendingTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                Runnable nextTask = asyncPacketSendQueue.poll();
                while (nextTask != null) {
                    nextTask.run();
                    nextTask = asyncPacketSendQueue.poll();
                }
            }, 0, 1);
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
