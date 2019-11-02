package org.maxgamer.quickshop.Command.SubCommands;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubCommand_Find implements CommandProcesser {
    private QuickShop plugin = QuickShop.instance;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (sender instanceof Player) {
            if (cmdArg.length < 1) {
                sender.sendMessage(MsgUtil.getMessage("command.no-type-given",sender));
                return;
            }
            StringBuilder sb = new StringBuilder(cmdArg[0]);
            for (int i = 1; i < cmdArg.length; i++) {
                sb.append(" ").append(cmdArg[i]);
            }
            String lookFor = sb.toString().toLowerCase();
            Player p = (Player) sender;
            Location loc = p.getEyeLocation().clone();
            double minDistance = plugin.getConfig().getInt("shop.find-distance");
            double minDistanceSquared = minDistance * minDistance;
            int chunkRadius = (int) minDistance / 16 + 1;
            Shop closest = null;
            Chunk c = loc.getChunk();
            for (int x = -chunkRadius + c.getX(); x < chunkRadius + c.getX(); x++) {
                for (int z = -chunkRadius + c.getZ(); z < chunkRadius + c.getZ(); z++) {
                    Chunk d = c.getWorld().getChunkAt(x, z);
                    HashMap<Location, Shop> inChunk = plugin.getShopManager().getShops(d);
                    if (inChunk == null) {
                        continue;
                    }
                    for (Shop shop : inChunk.values()) {
                        if (!Util.getItemStackName(shop.getItem()).toLowerCase().contains(lookFor)) {
                            continue;
                        }

                        if (!(shop.getLocation().distanceSquared(loc) < minDistanceSquared)) {
                            continue;
                        }
                        closest = shop;
                        minDistanceSquared = shop.getLocation().distanceSquared(loc);
                    }
                }
            }
            if (closest == null) {
                sender.sendMessage(MsgUtil.getMessage("no-nearby-shop",sender, cmdArg[0]));
                return;
            }
            Location lookat = closest.getLocation().clone().add(0.5, 0.5, 0.5);
            // Hack fix to make /qs find not used by /back
            p.teleport(Util.lookAt(loc, lookat).add(0, -1.62, 0), PlayerTeleportEvent.TeleportCause.UNKNOWN);
            p.sendMessage(
                    MsgUtil.getMessage("nearby-shop-this-way",sender, "" + (int) Math.floor(Math.sqrt(minDistanceSquared))));
        } else {
            sender.sendMessage(MsgUtil.getMessage("Only player can run this command",sender));
        }
        return;
    }
}
