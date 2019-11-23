package org.maxgamer.quickshop.Event;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Shop.Shop;

/**
 * Getting the unloading shop, Can't cancel.
 **/

public class ShopUnloadEvent extends QSEvent {

    @NotNull
    @Getter
    private Shop shop;

    /**
     * Getting the unloading shop, Can't cancel.
     *
     * @param shop The shop to unload
     */
    public ShopUnloadEvent(@NotNull Shop shop) {
        this.shop = shop;
    }

}
