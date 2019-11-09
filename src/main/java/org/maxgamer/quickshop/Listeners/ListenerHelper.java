package org.maxgamer.quickshop.Listeners;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class ListenerHelper {
    private static Set<Class> disabledListener = new HashSet<>();

    /**
     * Make QuickShop ignore the specify event
     * @param eventClass The event class, E.g BlockBreakEvent.class
     */
    public static void disableEvent(@NotNull Class eventClass) {
        disabledListener.add(eventClass);
    }
    /**
     * Make QuickShop nolonger ignore the specify event
     * @param eventClass The event class, E.g BlockBreakEvent.class
     */
    public static void enableEvent(@NotNull Class eventClass) {
        disabledListener.remove(eventClass);
    }
    /**
     * Check the specify event is disabled
     * @param eventClass The event class, E.g BlockBreakEvent.class
     */
    public static boolean isDisabled(Class eventClass) {
        return disabledListener.contains(eventClass);
    }
}
