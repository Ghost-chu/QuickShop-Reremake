package org.maxgamer.quickshop.event;

import org.maxgamer.quickshop.QuickShop;

public class QSReloadEvent extends QSEvent {

    QuickShop instance;

    /**
     * Called when Quickshop plugin reloaded
     *
     * @param instance Quickshop instance
     */
    public QSReloadEvent(QuickShop instance) {
        this.instance = instance;
    }

    public QuickShop getInstance() {
        return instance;
    }
}
