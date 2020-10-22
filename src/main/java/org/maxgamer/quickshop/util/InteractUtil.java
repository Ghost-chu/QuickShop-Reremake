package org.maxgamer.quickshop.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.maxgamer.quickshop.QuickShop;

import java.util.EnumMap;

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

        public static Mode getMode(int mode) {
            return Mode.values().length > mode ? Mode.values()[mode] : ONLY;
        }
    }

    public enum Action {
        CREATE, TRADE, CONTROL
    }

}
