package org.maxgamer.quickshop.Util;

import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.QuickShop;

public class WarningSender {
    private long cooldown;
    private long lastSend = 0;
    private QuickShop plugin;

    /**
     * Create a warning sender
     *
     * @param plugin   Main class
     * @param cooldown Time unit: ms
     */
    public WarningSender(@NotNull QuickShop plugin, long cooldown) {
        this.plugin = plugin;
        this.cooldown = cooldown;
    }

    /**
     * Send warning a warning
     *
     * @param text The text you want send/
     * @return Success sended, if in cooldowning, it will return false
     */
    public boolean sendWarn(String text) {
        if (System.currentTimeMillis() - lastSend > cooldown) {
            plugin.getLogger().warning(text);
            this.lastSend = System.currentTimeMillis();
            return true;
        }
        return false;
    }
}
