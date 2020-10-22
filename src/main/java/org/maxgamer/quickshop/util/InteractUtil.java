/*
 * This file is a part of project QuickShop, the name is Bootstrap.java
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
package org.maxgamer.quickshop.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.maxgamer.quickshop.QuickShop;

import java.util.EnumMap;

/***
 * Util class for interacting with shop
 *
 * @author sandtechnology
 * @since 4.0.5.0
 */
public class InteractUtil {
    private final static EnumMap<Action, Boolean> sneakingActionMap = new EnumMap<>(Action.class);
    private static Mode mode;
    private static boolean init;

    public static void init() {
        FileConfiguration configuration = QuickShop.getInstance().getConfig();
        mode = Mode.getMode(configuration.getInt("shop.interact.interact-mode", 0));
        sneakingActionMap.put(Action.CREATE, configuration.getBoolean("shop.interact.sneak-to-create"));
        sneakingActionMap.put(Action.TRADE, configuration.getBoolean("shop.interact.sneak-to-trade"));
        sneakingActionMap.put(Action.CONTROL, configuration.getBoolean("shop.interact.sneak-to-control"));
        init = true;
    }

    /**
     * Check if can interact with shop
     *
     * @param action     the action taking
     * @param isSneaking is the player sneaking
     * @return if can interact with shop
     */
    public static boolean check(Action action, boolean isSneaking) {
        if (!init) {
            init();
        }
        //Hopefully some coders can read this
        boolean sneakAllowed = sneakingActionMap.get(action);
        switch (mode) {
            case ONLY:
                return sneakAllowed && isSneaking;
            case BOTH:
                return !isSneaking || sneakAllowed;
            case REVERSED:
                return !isSneaking || !sneakAllowed;
            default:
                return true;
        }
    }

    public enum Mode {
        ONLY, BOTH, REVERSED;

        /**
         * Return the mode by int value
         *
         * @param mode int value
         * @return the mode by int value, return ONLY when out of bounds
         */
        public static Mode getMode(int mode) {
            return Mode.values().length > mode ? Mode.values()[mode] : ONLY;
        }
    }

    public enum Action {
        CREATE, TRADE, CONTROL
    }

}
