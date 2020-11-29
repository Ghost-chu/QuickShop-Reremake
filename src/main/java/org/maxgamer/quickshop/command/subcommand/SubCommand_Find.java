/*
 * This file is a part of project QuickShop, the name is SubCommand_Find.java
 *  Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.command.subcommand;

import lombok.AllArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandProcesser;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public class SubCommand_Find implements CommandProcesser {

    private final QuickShop plugin;

    @Override
    public void onCommand(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (!(sender instanceof Player)) {
            MsgUtil.sendMessage(sender, "Only player can run this command");
            return;
        }

        if (cmdArg.length == 0) {
            MsgUtil.sendMessage(sender, MsgUtil.getMessage("command.no-type-given", sender));
            return;
        }

        final Player p = (Player) sender;
        final Location loc = p.getLocation().clone();

        //Testing for getting chunk
        CompletableFuture<Chunk> future = new CompletableFuture<>();
        plugin.getBukkitAPIWrapper().getChunkAt(loc.getWorld(), loc, future);
        final Chunk c;
        try {
            c = future.get();
        } catch (Exception asyncErr) {
            MsgUtil.sendMessage(sender, "Cannot execute the command, see console for details.");
            plugin.getSentryErrorReporter().sendError(asyncErr, "Unknown errors");
            plugin.getSentryErrorReporter().ignoreThrow();
            asyncErr.printStackTrace();
            return;
        }
        //End

        //Combing command args
        final StringBuilder sb = new StringBuilder(cmdArg[0]);
        for (int i = 1; i < cmdArg.length; i++) {
            sb.append("_").append(cmdArg[i]);
        }


        final String lookFor = sb.toString().toLowerCase();
        final double minDistance = plugin.getConfig().getInt("shop.finding.distance");
        double minDistanceSquared = minDistance * minDistance;
        final int chunkRadius = (int) (minDistance / 16) + 1;
        final boolean usingOldLogic = plugin.getConfig().getBoolean("shop.finding.oldLogic");
        final int limit = usingOldLogic ? 1 : plugin.getConfig().getInt("shop.finding.limit");

        List<Map.Entry<Shop, Double>> nearByShopList = new ArrayList<>();
        findingProcess:
        for (int x = -chunkRadius + c.getX(); x < chunkRadius + c.getX(); x++) {
            for (int z = -chunkRadius + c.getZ(); z < chunkRadius + c.getZ(); z++) {
                final Chunk d = c.getWorld().getChunkAt(x, z);
                final Map<Location, Shop> inChunk = plugin.getShopManager().getShops(d);

                if (inChunk == null) {
                    continue;
                }

                for (Shop shop : inChunk.values()) {
                    if (!Util.getItemStackName(shop.getItem()).toLowerCase().contains(lookFor)) {
                        if (!shop.getItem().getType().name().toLowerCase().contains(lookFor)) {
                            continue;
                        }
                    }

                    double distance = shop.getLocation().distanceSquared(loc);
                    if (distance >= minDistanceSquared) {
                        continue;
                    }
                    nearByShopList.add(new AbstractMap.SimpleEntry<>(shop, distance));
                    if (nearByShopList.size() == limit) {
                        break findingProcess;
                    }
                }
            }
        }


        if (nearByShopList.isEmpty()) {
            MsgUtil.sendMessage(sender, MsgUtil.getMessage("no-nearby-shop", sender, lookFor));
        } else {
            if (usingOldLogic) {
                final Map.Entry<Shop, Double> closestShopEntry = nearByShopList.get(0);
                final Location lookat = closestShopEntry.getKey().getLocation().clone().add(0.5, 0.5, 0.5);
                // Hack fix to make /qs find not used by /back
                plugin
                        .getBukkitAPIWrapper()
                        .teleportEntity(
                                p,
                                Util.lookAt(p.getEyeLocation(), lookat).add(0, -1.62, 0),
                                PlayerTeleportEvent.TeleportCause.UNKNOWN);
                MsgUtil.sendMessage(p, MsgUtil.getMessage("nearby-shop-this-way", sender, closestShopEntry.getValue().toString()));
            } else {
                nearByShopList.sort(Map.Entry.comparingByValue());
                //"nearby-shop-header": "&aNearby Shop matching &b{0}&a:"
                StringBuilder stringBuilder = new StringBuilder(MsgUtil.getMessage("nearby-shop-header", sender, lookFor)).append("\n");
                for (Map.Entry<Shop, Double> shopDoubleEntry : nearByShopList) {
                    Shop shop = shopDoubleEntry.getKey();
                    Location location = shop.getLocation();
                    //  "nearby-shop-entry": "&a- Info:{0} &aPrice:&b{1} &ax:&b{2} &ay:&b{3} &az:&b{4} &adistance: &b{5} &ablock(s)"
                    stringBuilder.append(MsgUtil.getMessage("nearby-shop-entry", sender, shop.getSignText()[1], shop.getSignText()[3], location.getBlockX(), location.getBlockY(), location.getBlockZ(), Math.floor(shopDoubleEntry.getValue()))).append("\n");
                }
                MsgUtil.sendMessage(sender, stringBuilder.toString());
            }
        }
    }

}
