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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Async packet sender used for VirtualDisplayItem
 *
 * @author sandtechnology
 */
public class AsyncPacketSender {

    private volatile static AsyncSendingTask instance = null;
    private static boolean isUsingGlobal = false;
    private static volatile boolean enabled = false;

    private AsyncPacketSender() {
    }

    public synchronized static void start(QuickShop plugin) {
        isUsingGlobal = plugin.getConfig().getBoolean("use-global-virtual-item-queue");
        if (isUsingGlobal) {
            createAndCancelExistingTask(plugin);
        }
        enabled = true;
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
        private final Queue<Runnable> asyncPacketSendQueue = new ArrayBlockingQueue<>(100, true);
        private final AtomicBoolean taskDone = new AtomicBoolean(true);
        private volatile BukkitTask asyncSendingTask;

        public synchronized void start(QuickShop plugin) {
            //lazy initialize
            if (asyncSendingTask == null || asyncSendingTask.isCancelled()) {
                asyncSendingTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                    if (asyncSendingTask.isCancelled()) {
                        return;
                    }
                    if (!taskDone.get()) {
                        return;
                    }
                    taskDone.set(false);
                    Runnable nextTask = asyncPacketSendQueue.poll();
                    while (nextTask != null) {
                        nextTask.run();
                        nextTask = asyncPacketSendQueue.poll();
                    }
                    taskDone.set(true);
                }, 0, 1);
            }

        }


        public synchronized void stop() {
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
