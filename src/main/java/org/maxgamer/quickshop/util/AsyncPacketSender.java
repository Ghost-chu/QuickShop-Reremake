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

public class AsyncPacketSender {

    private static AsyncSendingTask instance = null;
    private static boolean isUsingGlobal = false;
    private static volatile boolean enabled = false;

    public synchronized static void start(QuickShop plugin) {
        isUsingGlobal = plugin.getConfig().getBoolean("use-global-virtual-item-queue");
        if (isUsingGlobal) {
            createAndCancelExistingTask(plugin);
        }
        enabled = true;
    }

    private AsyncPacketSender() {
    }

    private synchronized static void createAndCancelExistingTask(QuickShop plugin) {
        if (instance != null) {
            instance.stop();
        }
        instance = new AsyncSendingTask();
        instance.start(plugin);
    }

    public static AsyncSendingTask create() {
        if (!enabled) {
            throw new IllegalStateException("Please start AsyncPacketSender first!");
        }
        if (isUsingGlobal) {
            return instance;
        } else {
            return new AsyncSendingTask();
        }
    }

    public synchronized static void stop() {
        if (isUsingGlobal) {
            instance.stop();
        }
    }

    public static class AsyncSendingTask {
        private final Queue<Runnable> asyncPacketSendQueue = new ConcurrentLinkedQueue<>();
        private BukkitTask asyncSendingTask;

        public void start(QuickShop plugin) {
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


        public void stop() {
            if (asyncSendingTask != null && !asyncSendingTask.isCancelled()) {
                asyncSendingTask.cancel();
            }
            asyncPacketSendQueue.clear();
        }

        public void offer(Runnable runnable) {
            asyncPacketSendQueue.offer(runnable);
        }
    }
}
