package org.maxgamer.quickshop.Command.SubCommands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class SubCommand_CleanGhost implements CommandProcesser {

    private final QuickShop plugin = QuickShop.instance;

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if(cmdArg.length < 1){
            sender.sendMessage(ChatColor.YELLOW+"This command will purge all data damaged shop, create in disallow world shop, create disallow sell items shop and IN NOT LOADED WORLD SHOPS, make sure you have backup your shops data, and use /qs cleanghost confirm to continue.");
            return;
        }

        if(!"confirm".equalsIgnoreCase(cmdArg[0])){
            sender.sendMessage(ChatColor.YELLOW+"This command will purge all data damaged shop, create in disallow world shop, create disallow sell items shop and IN NOT LOADED WORLD SHOPS, make sure you have backup your shops data, and use /qs cleanghost confirm to continue.");
            return;
        }

        sender.sendMessage(ChatColor.GREEN+"Starting checking the shop be ghost, all does not exist shop will be removed...");

        new BukkitRunnable() {
            @Override
            public void run() {
                sender.sendMessage(ChatColor.GREEN+"Async thread is started, please wait...");
                Util.backupDatabase(); //Already warn the user, don't care about backup result.
                for (Shop shop : plugin.getShopLoader().getShopsInDatabase()) {
                    sender.sendMessage(ChatColor.GRAY + "Checking the shop " + shop+" metadata and location block state...");
                    if (shop == null) {
                        continue; //WTF
                    }
                    /*
                    shop.getItem() is a constant that has NotNull annotations so.
                    if (shop.getItem() == null) {
                        sender.sendMessage(ChatColor.YELLOW + "Shop " + shop + " removing cause item data is damaged.");
                        shop.delete();
                        continue;
                    }*/
                    if (shop.getItem().getType() == Material.AIR) {
                        sender.sendMessage(ChatColor.YELLOW + "Shop " + shop + " removing cause item data is damaged.");
                        shop.delete();
                        continue;
                    }
                    /*
                    shop.getLocation() is a constant that has NotNull annotations so.
                    if (shop.getLocation() == null) {
                        sender.sendMessage(ChatColor.YELLOW + "Shop " + shop + " removing cause location data is damaged.");
                        shop.delete();
                        continue;
                    }*/
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
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        Util.debugLog("Posted to main server thread to continue access Bukkit API for shop "+shop);
                        if (!Util.canBeShop(shop.getLocation().getBlock())) {
                            sender.sendMessage(ChatColor.YELLOW + "Shop " + shop + " removing cause target location nolonger is a shop or disallow create the shop.");
                            shop.delete();
                        }
                    }); //Post to server main thread to check.
                    try {
                        Thread.sleep(50); //Have a rest, don't blow up the main server thread.
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                sender.sendMessage(ChatColor.GREEN+"All shops completed checks.");
            }
        }.runTaskAsynchronously(plugin);
    }
}
