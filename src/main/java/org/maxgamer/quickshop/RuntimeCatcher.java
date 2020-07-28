package org.maxgamer.quickshop;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.util.GameVersion;
import org.maxgamer.quickshop.util.ReflectFactory;
import org.maxgamer.quickshop.util.Util;

public class RuntimeCatcher {
    @Getter
    private final GameVersion gameVersion;

    public RuntimeCatcher(@NotNull QuickShop plugin) {
        String nmsVersion = Util.getNMSVersion();
        gameVersion = GameVersion.get(nmsVersion);
        if (Util.isClassAvailable("org.maxgamer.quickshop.Util.NMS")) {
            plugin.getLogger().severe("FATAL: Old QuickShop is installed, You must remove old quickshop jar from plugins folder!");
            throw new RuntimeException("FATAL: Old QuickShop is installed, You must remove old quickshop jar from plugins folder!");
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
        if (isForgeBasedServer(plugin)) {
            plugin.getLogger().warning("WARN: QSRR not designed and tested on Forge platform, you're running QuickShop modded server and use at your own risk.");
            plugin.getLogger().warning("WARN: You won't get any support under Forge platform. Server will continue loading after 30s.");
            try {
                Thread.sleep(300000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (isFabricBasedServer(plugin)) {
            plugin.getLogger().warning("WARN: QSRR not designed and tested on Fabric platform, you're running QuickShop modded server and use at your own risk.");
            plugin.getLogger().warning("WARN: You won't get any support under Fabric platform. Server will continue loading after 30s.");
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

    private boolean isSpigotBasedServer(@NotNull QuickShop plugin) {
        //Class checking
        if (!Util.isClassAvailable("org.spigotmc.SpigotConfig")) {
            return false;
        }
        //API test
        try {
            plugin.getServer().spigot();
        } catch (Throwable e) {
            return false;
        }
        return true;
    }

    private boolean isForgeBasedServer(@NotNull QuickShop plugin) {
        //Forge server detect - Arclight
        if (Util.isClassAvailable("net.minecraftforge.server.ServerMain")) {
            return true;
        }
        return Util.isClassAvailable("net.minecraftforge.fml.loading.ModInfo");
    }

    private boolean isFabricBasedServer(@NotNull QuickShop plugin) {
        //Nobody really make it right!?
        return Util.isClassAvailable("net.fabricmc.loader.launch.knot.KnotClient"); //OMG
    }
}
