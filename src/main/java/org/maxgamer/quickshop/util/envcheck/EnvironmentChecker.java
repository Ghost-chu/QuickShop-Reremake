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

import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class EnvironmentChecker {
    private final QuickShop plugin;
    private final List<Method> tests = new ArrayList<>();

    public EnvironmentChecker(QuickShop plugin) {
        this.plugin = plugin;
        List<ResultContainer> results = new ArrayList<>();
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
        if (envCheckEntry == null) return;
        if (method.getReturnType() != ResultContainer.class) {
            Util.debugLog("Failed loading EncCheckEntry [" + method.getName() + "]: Illegal test returns! This should be a bug!");
            return;
        }
        tests.add(method);
        Util.debugLog("Registered test entry [" + method.getName() + "].");
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
        List<ResultContainer> results = new ArrayList<>();
        boolean skipAllTest = false;

        for (Method declaredMethod : this.tests) {
            if (skipAllTest) {
                break;
            }
            try {
                EnvCheckEntry envCheckEntry = declaredMethod.getAnnotation(EnvCheckEntry.class);
                ResultContainer executeResult = (ResultContainer) declaredMethod.invoke(this, (Object) null);
                if (executeResult.getResult().ordinal() > result.ordinal()) { //set bad result if it worse than latest one.
                    result = executeResult.getResult();
                }
                switch (executeResult.getResult()) {
                    case PASSED:
                        plugin.getLogger().info("[OK] " + envCheckEntry.name());
                        Util.debugLog("[Pass] " + envCheckEntry.name() + ": " + executeResult.getResultMessage());
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
                results.add(executeResult);
            } catch (Exception e) {
                Util.debugLog("Failed executing EncCheckEntry [" + declaredMethod.getName() + "]: Exception thrown out without caught. Something going wrong!");
                Util.debugLog(e.getClass().getName() + ": " + e.getMessage());
                MsgUtil.debugStackTrace(e.getStackTrace());
            }
        }
        return new ResultReport(result, results);
    }

    @EnvCheckEntry(name = "SelfTest", priority = 1)
    public ResultContainer selfTest() {
        return new ResultContainer(CheckResult.PASSED, "I'm fine :)");
    }

}
