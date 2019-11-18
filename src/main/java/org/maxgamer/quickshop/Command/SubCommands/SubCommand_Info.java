package org.maxgamer.quickshop.Command.SubCommands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.ContainerShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopChunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubCommand_Info implements CommandProcesser {
    private QuickShop plugin = QuickShop.instance;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        int buying, selling, doubles, chunks, worlds, doubleschests;
        buying = selling = doubles = chunks = worlds = doubleschests = 0;
        int nostock = 0;
        for (HashMap<ShopChunk, HashMap<Location, Shop>> inWorld : plugin.getShopManager().getShops()
                .values()) {
            worlds++;
            for (HashMap<Location, Shop> inChunk : inWorld.values()) {
                chunks++;
                //noinspection unchecked
                for (Shop shop : (ArrayList<Shop>) new ArrayList<>(inChunk.values()).clone()) {
                    if (shop.isBuying()) {
                        buying++;
                    } else if (shop.isSelling()) {
                        selling++;
                    }
                    if (shop instanceof ContainerShop && ((ContainerShop) shop).isDoubleShop()) {
                        doubles++;
                    } else if (shop.isSelling() && shop.getRemainingStock() == 0) {
                        nostock++;
                    }
                    if(shop instanceof ContainerShop && ((ContainerShop) shop).isDoubleChestShop()){
                        doubleschests++;
                    }
                }
            }
        }
        sender.sendMessage(ChatColor.RED + "QuickShop Statistics...");
        sender.sendMessage(ChatColor.GREEN + "Server UniqueID: " + plugin.getServerUniqueID());
        sender.sendMessage(ChatColor.GREEN + "" + (buying + selling) + " shops in " + chunks
                + " chunks spread over " + worlds + " worlds.");
        sender.sendMessage(ChatColor.GREEN + "" + doubles + " double shops. ("+doubleschests+" shops create on double chest.)");
        sender.sendMessage(ChatColor.GREEN + "" + nostock
                + " nostock selling shops (excluding doubles) which will be removed by /qs clean.");
        sender.sendMessage(ChatColor.GREEN + "QuickShop " + QuickShop.getVersion());
    }
}
