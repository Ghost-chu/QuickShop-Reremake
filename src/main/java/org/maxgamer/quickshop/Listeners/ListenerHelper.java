package org.maxgamer.quickshop.Listeners;

import java.util.LinkedList;

public class ListenerHelper {
    private static LinkedList<Class> disabledListener = new LinkedList<>();

    public static void disableEvent(Class eventClass) {
        if (disabledListener.contains(eventClass)) {
            return;
        }
        disabledListener.add(eventClass);
    }

    public static void enableEvent(Class eventClass) {
        if (!disabledListener.contains(eventClass)) {
            return;
        }
        disabledListener.remove(eventClass);
    }

    public static boolean isDisabled(Class eventClass) {
        return disabledListener.contains(eventClass);
    }
}
