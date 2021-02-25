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

package org.maxgamer.quickshop.util.envcheck;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.GameVersion;
import org.maxgamer.quickshop.util.JsonUtil;
import org.maxgamer.quickshop.util.ReflectFactory;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.security.JarVerifyTool;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

public class EnvironmentChecker {
    private final QuickShop plugin;
    private final List<Method> tests = new ArrayList<>();

    public EnvironmentChecker(QuickShop plugin) {
        this.plugin = plugin;
        this.registerTests(this.getClass()); //register self
    }

    /**
     * Register tests to QuickShop EnvChecker
     *
     * @param clazz The class contains test
     */
    public void registerTests(@NotNull Class<?> clazz) {
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            registerTest(declaredMethod);
        }
    }

    /**
     * Register test to QuickShop EnvChecker
     *
     * @param method The test method
     */
    public void registerTest(@NotNull Method method) {
        EnvCheckEntry envCheckEntry = method.getAnnotation(EnvCheckEntry.class);
        if (envCheckEntry == null) {
            return;
        }
        if (method.getReturnType() != ResultContainer.class) {
            plugin.getLogger().warning("Failed loading EncCheckEntry [" + method.getName() + "]: Illegal test returns");
            return;
        }
        tests.add(method);
        //plugin.getLogger().info("Registered test entry [" + method.getName() + "].");
    }

    private void sortTests() {
        tests.sort((o1, o2) -> {
            EnvCheckEntry e1 = o1.getAnnotation(EnvCheckEntry.class);
            EnvCheckEntry e2 = o2.getAnnotation(EnvCheckEntry.class);
            return Integer.compare(e1.priority(), e2.priority());
        });
    }

    public ResultReport run() {
        sortTests();
        CheckResult result = CheckResult.PASSED;
        Map<EnvCheckEntry, ResultContainer> results = new LinkedHashMap<>();
        boolean skipAllTest = false;

        for (Method declaredMethod : this.tests) {
            if (skipAllTest) {
                break;
            }
            try {
                EnvCheckEntry envCheckEntry = declaredMethod.getAnnotation(EnvCheckEntry.class);
                    ResultContainer executeResult = (ResultContainer) declaredMethod.invoke(this);
                    //plugin.getLogger().info("Result: "+executeResult.getResultMessage());
                    if (executeResult.getResult().ordinal() > result.ordinal()) { //set bad result if it worse than latest one.
                        result = executeResult.getResult();
                    }
                    switch (executeResult.getResult()) {
                        case PASSED:
                            if (Util.isDevEdition() || Util.isDevMode()) {
                                plugin.getLogger().info("[OK] " + envCheckEntry.name());
                                Util.debugLog("[Pass] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
                            }
                            break;
                        case WARNING:
                            plugin.getLogger().warning("[WARN] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
                            Util.debugLog("[Warning] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
                            break;
                        case STOP_WORKING:
                            plugin.getLogger().warning("[STOP] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
                            Util.debugLog("[Stop-Freeze] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
                            //It's okay, QuickShop should continue executing checks to collect more data.
                            //And show user all errors at once.
                            break;
                        case DISABLE_PLUGIN:
                            plugin.getLogger().warning("[FATAL] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
                            Util.debugLog("[Fatal-Disable] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
                            skipAllTest = true; //We need disable plugin NOW! Some HUGE exception is here, hurry up!
                            break;
                    }
                    results.put(envCheckEntry, executeResult);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed executing EnvCheckEntry [" + declaredMethod.getName() + "]: Exception thrown out without caught. Something going wrong!", e);
                plugin.getLogger().warning("[FAIL] " + declaredMethod.getName());
            }
        }
        return new ResultReport(result, results);
    }

    public boolean isOutdatedJvm() {
        //String jvmVersion = ManagementFactory.getRuntimeMXBean().getVmVersion();
        String jvmVersion = System.getProperty("java.version"); //Use java version not jvm version.
        String[] splitVersion = jvmVersion.split("\\.");
        if (splitVersion.length < 1) {
            Util.debugLog("Failed to parse jvm version to check: " + jvmVersion);
            return false;
        }
        try {
            int majorVersion = Integer.parseInt(splitVersion[0]);
            return majorVersion < 11; //Target JDK/JRE version
        } catch (NumberFormatException ignored) {
            Util.debugLog("Failed to parse jvm major version to check: " + splitVersion[0]);
            return false;
        }
    }

    @EnvCheckEntry(name = "Signature Verify", priority = 0)
    public ResultContainer securityVerify() {
        JarVerifyTool tool = new JarVerifyTool();
        try {
            ClassLoader loader = this.getClass().getClassLoader();
            if (loader.getResourceAsStream("META-INF/MANIFEST.MF") == null
                    || loader.getResourceAsStream("META-INF/SELFSIGN.DSA") == null
                    || loader.getResourceAsStream("META-INF/SELFSIGN.SF") == null) {
                plugin.getLogger().warning("The signature not exists in QuickShop jar. The jar has been modified or you're running custom build.");
                return new ResultContainer(CheckResult.STOP_WORKING, "Security risk detected, QuickShop jar has been modified.");
            }

            String jarPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
            Util.debugLog("JarPath selected: " + jarPath);
            JarFile jarFile = new JarFile(jarPath);
            List<JarEntry> modifiedEntry = tool.verify(jarFile);
            if (modifiedEntry.isEmpty()) {
                return new ResultContainer(CheckResult.PASSED, "The jar is valid. No issues detected");
            } else {
                modifiedEntry.forEach(jarEntry -> {
                    plugin.getLogger().warning(">> Modified Class Detected <<");
                    plugin.getLogger().warning("Name: " + jarEntry.getName());
                    plugin.getLogger().warning("CRC: " + jarEntry.getCrc());
                    plugin.getLogger().warning(JsonUtil.getGson().toJson(jarEntry));
                });
                plugin.getLogger().severe("QuickShop detected the jar has been modified. This usually caused by the file downloaded damaged or virus infected. You should run virus scanning to prevent virus infected.");
                plugin.getLogger().severe("To prevent serve server fail, QuickShop has been disabled.");
                return new ResultContainer(CheckResult.STOP_WORKING, "Security risk detected, QuickShop jar has been modified.");
            }
        } catch (IOException ioException) {
            plugin.getLogger().log(Level.WARNING, "ALERT: QuickShop cannot validate itself. This may caused by your have deleted QuickShop's jar while server running.", ioException);
            return new ResultContainer(CheckResult.WARNING, "Failed to validate file digital signature, Security ");
        }

        //tool.verify()


    }

    @EnvCheckEntry(name = "EnvChecker SelfTest", priority = 1)
    public ResultContainer selfTest() {
        return new ResultContainer(CheckResult.PASSED, "I'm fine :)");
    }


    @SneakyThrows
    @EnvCheckEntry(name = "Java Runtime Environment Version Test", priority = 1)
    public ResultContainer jrevTest() {
        if (isOutdatedJvm()) {
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
            Thread.sleep(5000);
            return new ResultContainer(CheckResult.WARNING, "Unsupported outdated JRE version detected.");
        }
        return new ResultContainer(CheckResult.PASSED, "Java is up-to-date: " + System.getProperty("java.version"));
    }

    @EnvCheckEntry(name = "Spigot Based Server Test", priority = 2)
    public ResultContainer spigotBasedServer() {
        ResultContainer success = new ResultContainer(CheckResult.PASSED, "Server");
        ResultContainer failed = new ResultContainer(CheckResult.STOP_WORKING, "Server must be Spigot based, Don't use CraftBukkit!");
        try {
            //API test
            try {
                plugin.getServer().spigot();
                String nmsVersion = Util.getNMSVersion();
                plugin.getLogger().info("Running QuickShop-" + QuickShop.getFork() + " on NMS version " + nmsVersion + " For Minecraft version " + ReflectFactory.getServerVersion());
            } catch (Exception e) {
                return failed;
            }
            return success;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to check Spigot API, the server " + Bukkit.getBukkitVersion() + " may not Spigot or Spigot's forks");
            return failed;
        }

    }

    @EnvCheckEntry(name = "Old QuickShop Test", priority = 3)
    public ResultContainer oldQuickShopTest() {
        if (Util.isClassAvailable("org.maxgamer.quickshop.Util.NMS")) {
            return new ResultContainer(CheckResult.STOP_WORKING, "FATAL: Old QuickShop is installed, You must remove old QuickShop jar from plugins folder!");
        }
        return new ResultContainer(CheckResult.PASSED, "No old QuickShop jar installled on this server");
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

    @EnvCheckEntry(name = "ModdedServer Based Test", priority = 4)
    public ResultContainer moddedBasedTest() {
        boolean trigged = false;
        if (isForgeBasedServer()) {
            plugin.getLogger().warning("WARN: QuickShop not designed and tested on Forge platform, you're running QuickShop modded server and use at your own risk.");
            plugin.getLogger().warning("WARN: You won't get any support under Forge platform.");
            trigged = true;
        }
        if (isFabricBasedServer()) {
            plugin.getLogger().warning("WARN: QSRR not designed and tested on Fabric platform, you're running QuickShop modded server and use at your own risk.");
            plugin.getLogger().warning("WARN: You won't get any support under Fabric platform.");
            trigged = true;
        }
        if (trigged) {
            return new ResultContainer(CheckResult.WARNING, "Modded server won't get any support.");
        }
        return new ResultContainer(CheckResult.PASSED, "Server is unmodified.");
    }

    public boolean checkJulySafe() {
        Plugin julySafe = Bukkit.getPluginManager().getPlugin("JulySafe");
        boolean triggered = false;
        if (julySafe != null) {
            for (RegisteredListener registeredListener : BlockPlaceEvent.getHandlerList().getRegisteredListeners()) {
                if (registeredListener.getPlugin().equals(julySafe)) {
                    Class<?> clazz = null;
                    try {
                        clazz = Class.forName("com.github.julyss2019.bukkit.plugins.julysafe.listeners.QuickShopBugFixListener");
                    } catch (ClassNotFoundException ignored) {
                        return false;
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
        return triggered;
    }

    @EnvCheckEntry(name = "JulySafe Test", priority = 5)
    public ResultContainer julySafeTest() {
        if (checkJulySafe()) {
            return new ResultContainer(CheckResult.WARNING, "JulySafe is installed and \"QuickShop *bugfix* \" module is enabled");
        }
        return new ResultContainer(CheckResult.PASSED, "JulySafe not installed or qs bug fix module is disabled.");
    }

    @EnvCheckEntry(name = "CoreSupport Test", priority = 6)
    public ResultContainer coreSupportTest() {
        String nmsVersion = Util.getNMSVersion();
        GameVersion gameVersion = GameVersion.get(nmsVersion);
        if (!gameVersion.isCoreSupports()) {
            return new ResultContainer(CheckResult.STOP_WORKING, "Your Minecraft version is no-longer supported: " + ReflectFactory.getServerVersion() + " (" + nmsVersion + ")");
        }
        if (gameVersion == GameVersion.UNKNOWN) {
            return new ResultContainer(CheckResult.WARNING, "QuickShop may not fully support your current version " + nmsVersion + "/" + ReflectFactory.getServerVersion() + ", Some features may not working.");
        }
        return new ResultContainer(CheckResult.PASSED, "Passed checks");
    }

    @EnvCheckEntry(name = "Virtual DisplayItem Support Test", priority = 7)
    public ResultContainer virtualDisplaySupportTest() {
        String nmsVersion = Util.getNMSVersion();
        GameVersion gameVersion = GameVersion.get(nmsVersion);
        if (!gameVersion.isVirtualDisplaySupports()) {
            return new ResultContainer(CheckResult.WARNING, "Virtual DisplayItem seems won't working on this Minecraft server, Make sure you have up-to-date QuickShop and server core jar.");
        }
        return new ResultContainer(CheckResult.PASSED, "Passed checks");
    }

    @EnvCheckEntry(name = "PersistentStorageApi Support Test", priority = 8)
    public ResultContainer persistentStorageApiSupportTest() {
        String nmsVersion = Util.getNMSVersion();
        GameVersion gameVersion = GameVersion.get(nmsVersion);
        if (!gameVersion.isPersistentStorageApiSupports()) {
            return new ResultContainer(CheckResult.WARNING, "PersistentStorageApi seems won't working on this Minecraft server, You may hit exploit risk. Make sure you have up-to-date server at least higher than 1.13.2");
        }
        return new ResultContainer(CheckResult.PASSED, "Passed checks");
    }
}
