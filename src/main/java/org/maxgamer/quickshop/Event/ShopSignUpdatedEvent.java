package org.maxgamer.quickshop.Event;

import lombok.Getter;
import lombok.ToString;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Shop.Shop;

/**
 * Calling when shop sign update, Can't cancel
 **/
@ToString
public class ShopSignUpdatedEvent extends QSEvent {

    @Getter
    @NotNull
    private final Shop shop;

    @Getter
    @NotNull
    private final Sign sign;

    /**
     * Will call when shop price was changed.
     *
     * @param shop Target shop
     * @param sign Updated sign
     */
    public ShopSignUpdatedEvent(@NotNull Shop shop, @NotNull Sign sign) {
        this.shop = shop;
        this.sign = sign;
    }

}
