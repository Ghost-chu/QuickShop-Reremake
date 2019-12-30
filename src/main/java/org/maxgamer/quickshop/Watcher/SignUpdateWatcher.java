/*
 * This file is a part of project QuickShop, the name is SignUpdateWatcher.java
 * Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 * Copyright (C) Bukkit Commons Studio and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.maxgamer.quickshop.Watcher;

import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;

import java.util.LinkedList;
import java.util.Queue;

public class SignUpdateWatcher extends BukkitRunnable {
    private QuickShop plugin;
    private Queue<Shop> signUpdateQueue = new LinkedList<>();
    public SignUpdateWatcher(QuickShop plugin){
        this.plugin = plugin;
    }

    public void scheduleSignUpdate(@NotNull Shop shop){
        if(signUpdateQueue.contains(shop)){
            return; //Ignore
        }
        signUpdateQueue.add(shop);
    }

    @Override
    public void run() {
        Shop shop = signUpdateQueue.poll();
        while (shop !=null){
            shop.setSignText();
            shop = signUpdateQueue.poll();
        }
    }
}
