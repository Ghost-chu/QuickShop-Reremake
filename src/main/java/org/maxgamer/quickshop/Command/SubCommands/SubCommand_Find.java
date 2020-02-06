/*
 * This file is a part of project QuickShop, the name is SubCommand_Find.java
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

package org.maxgamer.quickshop.Command.SubCommands;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SubCommand_Find implements CommandProcesser {

    private final QuickShop plugin = QuickShop.instance;

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

    @Override
    public void onCommand(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MsgUtil.getMessage("Only player can run this command", sender));
            return;
        }

        if (cmdArg.length < 1) {
            sender.sendMessage(MsgUtil.getMessage("command.no-type-given", sender));
            return;
        }

        final StringBuilder sb = new StringBuilder(cmdArg[0]);

        for (int i = 1; i < cmdArg.length; i++) {
            sb.append(" ").append(cmdArg[i]);
        }

        final String lookFor = sb.toString().toLowerCase();
        final Player p = (Player) sender;
        final Location loc = p.getEyeLocation().clone();
        final double minDistance = plugin.getConfig().getInt("shop.find-distance");
        double minDistanceSquared = minDistance * minDistance;
        final int chunkRadius = (int) minDistance / 16 + 1;
        Shop closest = null;
        CompletableFuture<Chunk> future = new CompletableFuture<>();
        plugin.getBukkitAPIWrapper().getChunkAt(loc.getWorld(), loc, future);
        final Chunk c;
        try {
            c = future.get();
        } catch (Exception asyncErr) {
            sender.sendMessage("Cannot execute the command, see console for details.");
            plugin.getSentryErrorReporter().sendError(asyncErr, "Unknown errors");
            plugin.getSentryErrorReporter().ignoreThrow();
            asyncErr.printStackTrace();
            return;
        }
        for (int x = -chunkRadius + c.getX(); x < chunkRadius + c.getX(); x++) {
            for (int z = -chunkRadius + c.getZ(); z < chunkRadius + c.getZ(); z++) {
                final Chunk d = c.getWorld().getChunkAt(x, z);
                final HashMap<Location, Shop> inChunk = plugin.getShopManager().getShops(d);

                if (inChunk == null) {
                    continue;
                }

                for (Shop shop : inChunk.values()) {
                    if (!Util.getItemStackName(shop.getItem()).toLowerCase().contains(lookFor)) {
                        continue;
                    }

                    if (shop.getLocation().distanceSquared(loc) >= minDistanceSquared) {
                        continue;
                    }

                    closest = shop;
                    minDistanceSquared = shop.getLocation().distanceSquared(loc);
                }
            }
        }

        if (closest == null) {
            sender.sendMessage(MsgUtil.getMessage("no-nearby-shop", sender, cmdArg[0]));
            return;
        }

        final Location lookat = closest.getLocation().clone().add(0.5, 0.5, 0.5);
        // Hack fix to make /qs find not used by /back
        plugin
                .getBukkitAPIWrapper()
                .teleportEntity(
                        p,
                        Util.lookAt(loc, lookat).add(0, -1.62, 0),
                        PlayerTeleportEvent.TeleportCause.UNKNOWN);
        p.sendMessage(
                MsgUtil.getMessage(
                        "nearby-shop-this-way", sender, "" + (int) Math.floor(Math.sqrt(minDistanceSquared))));
    }
}
