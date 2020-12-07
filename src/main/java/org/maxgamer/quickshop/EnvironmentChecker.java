/*
 * This file is a part of project QuickShop, the name is EnvironmentChecker.java
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

package org.maxgamer.quickshop;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.util.GameVersion;
import org.maxgamer.quickshop.util.ReflectFactory;
import org.maxgamer.quickshop.util.Util;

public class EnvironmentChecker {
    @Getter
    private final GameVersion gameVersion;
    private static boolean showed = false;

    public EnvironmentChecker(@NotNull QuickShop plugin) {
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
                e.printStackTrace();
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
