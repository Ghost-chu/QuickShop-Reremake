package org.maxgamer.quickshop.Util;

import org.maxgamer.quickshop.QuickShop;

public class AsyncDetector {
    /**
     * A helper to allow quickshop check the task is running under async thread.
     * @return The calling method running under async thread.
     */
    public static boolean isAsync() {
        try {
            Object serverInstance = Util.getNMSClass(null).getMethod("getServer").invoke(null);
            Object serverThread = serverInstance.getClass().getField("serverThread");
            return Thread.currentThread() != serverThread;
        }catch (Throwable throwable){
            QuickShop.instance.getLogger().warning("Failed to get Minecraft server instance, did you up-to-date the QuickShop?");
            return false;
        }

    }
}
