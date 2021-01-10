/*
 * This file is a part of project QuickShop, the name is EnvironmentChecker.java
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

package org.maxgamer.quickshop;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.util.GameVersion;
import org.maxgamer.quickshop.util.ReflectFactory;
import org.maxgamer.quickshop.util.Util;

public class EnvironmentChecker {
    private static boolean showed = false;
    @Getter
    private final GameVersion gameVersion;

    private final QuickShop plugin;

    public EnvironmentChecker(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        String nmsVersion = Util.getNMSVersion();
        gameVersion = GameVersion.get(nmsVersion);
        if (Util.isClassAvailable("org.maxgamer.quickshop.Util.NMS")) {
            plugin.getLogger().severe("FATAL: Old QuickShop is installed, You must remove old quickshop jar from plugins folder!");
            throw new RuntimeException("FATAL: Old QuickShop is installed, You must remove old quickshop jar from plugins folder!");
        }

        if (!showed && isOutdatedJvm()) {
            showed = true;
            String jvmWarning = "\n" +
                    "============================================================\n" +
                    "    Warning! You're running an outdated version of Java\n" +
                    "============================================================\n" +
                    "* QuickShop will stop being compatible with this Java build\n" +
                    "* since we released 1.17 updates.\n" +
                    "*\n" +
                    "* You should schedule an upgrade for Java on your server,\n" +
                    "* because we will drop support with any version Java that\n" +
                    "* lower Java 11.\n" +
                    "*\n" +
                    "* That means:\n" +
                    "* 1) The new version of QuickShop for 1.17 updates will stop working on your server.\n" +
                    "* 2) No more supporting for QuickShop that running\n" +
                    "* on an outdated Java builds.\n" +
                    "* 3) You will get performance improvements in the\n" +
                    "* new version of Java builds.\n" +
                    "* \n" +
                    "* Why:\n" +
                    "* 1) We didn't want to keep compatibility with legacy software\n" +
                    "* and systems. As Paper did it, we will follow the step.\n" +
                    "* 2) Newer Java builds support legacy plugin that built for 8 \n" +
                    "* or legacy, so the most plugins will still working.\n" +
                    "* 3) New Java API allows access resources and processing it\n" +
                    "*  faster than before, and that means performance improvement.\n" +
                    "* \n" +
                    "* What should I do?\n" +
                    "* You should update your server Java builds as soon as you can.\n" +
                    "* Java 11 or any version after 11 is okay. \n" +
                    "*\n" +
                    "* Most plugins can run on Java 11+ without problems un-\n" +
                    "* less the code is really bad/hacky and you should uninstall it.\n" +
                    "*\n" +
                    "* You can get Java at here:\n" +
                    "* https://www.oracle.com/java/technologies/javase-downloads.html\n" +
                    "* And we recommended Java SE 11 (LTS) build for Minecraft Server.\n" +
                    "*\n" +
                    "*\n" +
                    String.format("* Current Java version: %s", System.getProperty("java.version"));
            plugin.getLogger().warning(jvmWarning);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }


        plugin.getLogger().info("Running QuickShop-" + QuickShop.getFork() + " on NMS version " + nmsVersion + " For Minecraft version " + ReflectFactory.getServerVersion());
        if (!gameVersion.isCoreSupports()) {
            throw new RuntimeException("Your Minecraft version is no-longer supported: " + ReflectFactory.getServerVersion() + " (" + nmsVersion + ")");
        }
        if (gameVersion == GameVersion.UNKNOWN) {
            plugin.getLogger().warning("Alert: QuickShop may not fully support your current version " + nmsVersion + "/" + ReflectFactory.getServerVersion() + ", Some features may not working.");
        }

        if (!isSpigotBasedServer(plugin)) {
            plugin.getLogger().severe("FATAL: QSRR can only be run on Spigot servers and forks of Spigot!");
            throw new RuntimeException("Server must be Spigot based, Don't use CraftBukkit!");
        }
        if (isForgeBasedServer()) {
            plugin.getLogger().warning("WARN: QSRR not designed and tested on Forge platform, you're running QuickShop modded server and use at your own risk.");
            plugin.getLogger().warning("WARN: You won't get any support under Forge platform.");
            //try {
            //    //Thread.sleep(300000);
            //} catch (InterruptedException e) {
            //    e.printStackTrace();
            //}
        }
        if (isFabricBasedServer()) {
            plugin.getLogger().warning("WARN: QSRR not designed and tested on Fabric platform, you're running QuickShop modded server and use at your own risk.");
            plugin.getLogger().warning("WARN: You won't get any support under Fabric platform.");
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
        this.checkJulySafe();
    }


    public void checkJulySafe() {
        Plugin julySafe = Bukkit.getPluginManager().getPlugin("JulySafe");
        boolean triggered = false;
        if (julySafe != null) {
            for (RegisteredListener registeredListener : BlockPlaceEvent.getHandlerList().getRegisteredListeners()) {
                if (registeredListener.getPlugin().equals(julySafe)) {
                    Class<?> clazz = null;
                    try {
                        clazz = Class.forName("com.github.julyss2019.bukkit.plugins.julysafe.listeners.QuickShopBugFixListener");
                    } catch (ClassNotFoundException ignored) {
                    }
                    if (registeredListener.getListener().getClass().equals(clazz)
                            || registeredListener.getListener().getClass().getName().contains("julysafe.listeners.QuickShopBugFixListener")) {
                        HandlerList.unregisterAll(registeredListener.getListener()); //Unregister JulySafe's QuickShopBugFixListener
                        triggered = true;
                    }
                }
            }
            if (julySafe.getConfig().getBoolean("quickshop_bug_fix.enabled")) {
                julySafe.getConfig().set("quickshop_bug_fix.enabled", false); // Disables JulySafe's QuickShopBugFix module in configuration.
                plugin.getLogger().warning("========================================");// Send chinese alert to users cause this plugin is Chinese plugins.
                plugin.getLogger().warning("警告：检测到您已安装 JulySafe 插件，且已开启 QuickShop 偷东西bug修复模块");
                plugin.getLogger().warning("警告：我们已确认不存在此 BUG 且此模块对正常 Gameplay 造成了严重影响且");
                plugin.getLogger().warning("警告：实际上并不会真正做到修复的效果。并且存在潜在破坏 QuickShop 本身保护机制的行为。");
                plugin.getLogger().warning("警告：我们强烈您使用 虚拟悬浮物 (Virtual DisplayItem) 解决潜在的安全隐患。");
                plugin.getLogger().warning("警告：同时我(Ghost_chu)也对国内部分开发者将其他插件 BUG 在不与插件本身开发者交流报告的情况下直接作为插件卖点宣传的行为强烈抨击。");
                plugin.getLogger().warning("警告：我长期活跃并维护相关插件，这些所谓的修复插件开发者完全应该可以联系插件开发者报告错误，但是这些插件的插件开发者并没有这么做。");
                plugin.getLogger().warning("警告：如果你说你的修复模块有点用，那么我也可以接受；" +
                        "而 JulySafe 插件采用了完全修复不了问题的解决方案去修复了一个 " + ChatColor.RED + ChatColor.BOLD + "根本不存在的BUG");
                plugin.getLogger().warning("警告：并将锅甩给 QuickShop 的行为是我所不能接受的，在此公开对此行为进行批评。");
                plugin.getLogger().warning("========================================");
                plugin.getLogger().warning("警告：QuickShop 已自动采取措施在配置文件中禁用此模块并注销 JulySafe 的 QuickShopBugFixListener 监听器使此模块强制失效。");
                plugin.getLogger().warning("警告：您的服务器将会在25秒后继续加载，以确保您已阅读相关声明，感谢您的理解。");
                triggered = true;
            }
            if (triggered) {
                try {
                    Thread.sleep(25 * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public boolean isSpigotBasedServer(@NotNull QuickShop plugin) {
        //Class checking
        if (!Util.isClassAvailable("org.spigotmc.SpigotConfig")) {
            return false;
        }
        //API test
        try {
            plugin.getServer().spigot();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean isForgeBasedServer() {
        //Forge server detect - Arclight
        if (Util.isClassAvailable("net.minecraftforge.server.ServerMain")) {
            return true;
        }
        return Util.isClassAvailable("net.minecraftforge.fml.loading.ModInfo");
    }

    public boolean isFabricBasedServer() {
        //Nobody really make it right!?
        return Util.isClassAvailable("net.fabricmc.loader.launch.knot.KnotClient"); //OMG
    }

    public boolean isOutdatedJvm() {
        //String jvmVersion = ManagementFactory.getRuntimeMXBean().getVmVersion();
        String jvmVersion = System.getProperty("java.version"); //Use java version not jvm version.
        String[] splitVersion = jvmVersion.split("\\.");
        if (splitVersion.length < 1) {
            Util.debugLog("Failed to parse jvm version to check: " + jvmVersion);
            return false;
        }
        int major = 0;
        try {
            int majorVersion = Integer.parseInt(splitVersion[0]);
            return majorVersion < 11; //Target JDK/JRE version
        } catch (NumberFormatException ignored) {
            Util.debugLog("Failed to parse jvm major version to check: " + splitVersion[0]);
            return false;
        }
    }
}
