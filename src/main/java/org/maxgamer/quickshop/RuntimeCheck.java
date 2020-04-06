package org.maxgamer.quickshop;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.shop.DisplayType;
import org.maxgamer.quickshop.util.IncompatibleChecker;
import org.maxgamer.quickshop.util.ReflectFactory;
import org.maxgamer.quickshop.util.Util;

public class RuntimeCheck {
    public RuntimeCheck(@NotNull QuickShop plugin) {
        if (Util.isClassAvailable("org.maxgamer.quickshop.Util.NMS")) {
            plugin.getLogger().severe("FATAL: Old QuickShop is installed, You must remove old quickshop jar from plugins folder!");
            throw new RuntimeException("FATAL: Old QuickShop is installed, You must remove old quickshop jar from plugins folder!");
        }
        String nmsVersion = Util.getNMSVersion();
        IncompatibleChecker incompatibleChecker = new IncompatibleChecker();
        plugin.getLogger().info("Running QuickShop-Reremake on NMS version " + nmsVersion + " For Minecraft version " + ReflectFactory.getServerVersion());
        if (incompatibleChecker.isIncompatible(nmsVersion)) {
            throw new RuntimeException("Your Minecraft version is nolonger supported: " + ReflectFactory.getServerVersion() + " (" + nmsVersion + ")");
        }
        try {
            plugin.getServer().spigot();
        } catch (Throwable e) {
            plugin.getLogger().severe("FATAL: QSRR can only be run on Spigot servers and forks of Spigot!");
            throw new RuntimeException("Server must be Spigot based, Don't use CraftBukkit!");
        }
        if (plugin.getServer().getName().toLowerCase().contains("catserver") || Util.isClassAvailable("moe.luohuayu.CatServer") || Util.isClassAvailable("catserver.server.CatServer")) {
            // Send FATAL ERROR TO CatServer's users.
            int csi = 0;
            while (csi < 101) {
                csi++;
                plugin.getLogger().severe("FATAL: QSRR can't run on CatServer Community/Personal/Pro/Async, Go https://github.com/Luohuayu/QuickShop-Reremake to get CatServer Edition.");
                plugin.getLogger().severe("FATAL: Don't report any bugs or other issues to the Ghost-chu's QuickShop-Reremake repo as we do not support CatServer");
            }
            try {
                Thread.sleep(180000); //Make sure CS user 100% can see alert sent above.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            plugin.setBootError(new BootError("Cannot load on CatServer, go download CS edition, don't fucking asking us for support or we will send an army of angry bees your way.", "https://github.com/Luohuayu/QuickShop-Reremake"));
            return;
        }
        if ((plugin.getServer().getName().toLowerCase().contains("mohist") || Util.isClassAvailable("red.mohist.mohist"))) {
            //Because it passed compatible checker checks, Mohist version must is 1.13+.
            //We doesn't need check the mohist is 1.12 or not.
            //Because QuickShop doesn't support ANY 1.12 server.
            int moi = 0;
            while (moi < 3) {
                moi++;
                plugin.getLogger().severe("WARN: QSRR compatibility on Mohist 1.13+ modded server currently unknown, report any issue to Mohist issue tracker or QuickShop issue tracker.");
                if (DisplayType.fromID(plugin.getConfig().getInt("shop.display-type")) != DisplayType.VIRTUALITEM) {//Even VIRTUALITEM still WIP, but we should install checker first.
                    plugin.getLogger().warning("Switch to Virtual display item to make sure displays won't duped by mods.");
                }
            }
        }

        if (Util.isDevEdition()) {
            plugin.getLogger().severe("WARNING: You are running QSRR in dev-mode");
            plugin.getLogger().severe("WARNING: Keep backup and DO NOT run this in a production environment!");
            plugin.getLogger().severe("WARNING: Test version may destroy everything!");
            plugin.getLogger().severe("WARNING: QSRR won't start without your confirmation, nothing will change before you turn on dev allowed.");
            if (!plugin.getConfig().getBoolean("dev-mode")) {
                plugin.getLogger().severe("WARNING: Set dev-mode: true in config.yml to allow qs load in dev mode(You may need add this line to the config yourself).");
                throw new RuntimeException("Snapshot cannot run when dev-mode is false in the config");
            }
        }
        plugin.getLogger().info("Checking the tax account infos...");
        String taxAccount = plugin.getConfig().getString("tax-account", "tax");
        //noinspection ConstantConditions
        if (!Bukkit.getOfflinePlayer(taxAccount).hasPlayedBefore()) {
            plugin.getLogger().warning("Tax account's player never played server before, that may cause server lagg or economy system error, you should change that name.");
        }
    }
}
