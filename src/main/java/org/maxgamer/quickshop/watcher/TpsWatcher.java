/*
 * This file is a part of project QuickShop, the name is TpsWatcher.java
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

package org.maxgamer.quickshop.watcher;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;

/**
 * @author EssentialsX
 * https://github.com/EssentialsX/Essentials/blob/90e4845627551a02a5868141544396bf50ac51a9/Essentials/src/main/java/com/earth2me/essentials/EssentialsTimer.java#L13
 */
public class TpsWatcher extends BukkitRunnable {
    private final LinkedList<Double> history = new LinkedList<>();
    @SuppressWarnings("FieldCanBeLocal")
    private final static long TICK_INTERVAL = 50;
    private transient long lastPoll = System.nanoTime();

    public TpsWatcher() {
        history.add(20d);
    }

    @Override
    public void run() {
        final long startTime = System.nanoTime();
        long timeSpent = (startTime - lastPoll) / 1000;
        if (timeSpent == 0) {
            timeSpent = 1;
        }
        if (history.size() > 10) {
            history.remove();
        }
        final double tps = TICK_INTERVAL * 1000000.0 / timeSpent;
        if (tps <= 21) {
            history.add(tps);
        }
        lastPoll = startTime;
    }

    public double getAverageTPS() {
        double avg = 0;
        for (final Double f : history) {
            if (f != null) {
                avg += f;
            }
        }
        return avg / history.size();
    }
}
