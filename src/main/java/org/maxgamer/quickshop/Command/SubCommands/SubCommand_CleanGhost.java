package org.maxgamer.quickshop.Command.SubCommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class SubCommand_CleanGhost implements CommandProcesser {
    private QuickShop plugin = QuickShop.instance;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        sender.sendMessage("Starting checking the shop be ghost, all does not exist shop will be removed...");
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Shop shop : plugin.getShopLoader().getShopsInDatabase()) {
                    sender.sendMessage(ChatColor.GRAY + "Checking the shop " + shop);
                    if (shop == null) {
                        continue; //WTF
                    }
                    //noinspection ConstantConditions
                    if (shop.getItem() == null) {
                        sender.sendMessage(ChatColor.YELLOW + "Shop " + shop + " removing cause item data is damaged.");
                        shop.delete();
                        continue;
                    }
                    if (shop.getItem().getType() == Material.AIR) {
                        sender.sendMessage(ChatColor.YELLOW + "Shop " + shop + " removing cause item data is damaged.");
                        shop.delete();
                        continue;
                    }
                    //noinspection ConstantConditions
                    if (shop.getLocation() == null) {
                        sender.sendMessage(ChatColor.YELLOW + "Shop " + shop + " removing cause location data is damaged.");
                        shop.delete();
                        continue;
                    }
                    if (shop.getLocation().getWorld() == null) {
                        sender.sendMessage(ChatColor.YELLOW + "Shop " + shop + " removing cause target world not loaded.");
                        shop.delete();
                        continue;
                    }
                    //noinspection ConstantConditions
                    if (shop.getOwner() == null) {
                        sender.sendMessage(ChatColor.YELLOW + "Shop " + shop + " removing cause owner data is damaged.");
                        shop.delete();
                        continue;
                    }
                    //Shop exist check
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (!Util.canBeShop(shop.getLocation().getBlock())) {
                            sender.sendMessage(ChatColor.YELLOW + "Shop " + shop + " removing cause target location nolonger is a shop or disallow create the shop.");
                            shop.delete();
                        }
                    }); //Post to server main thread to check.
                }
                sender.sendMessage(ChatColor.GREEN+"All shops completed checks.");
            }
        }.runTaskAsynchronously(plugin);
    }
}
