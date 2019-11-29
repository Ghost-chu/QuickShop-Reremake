package org.maxgamer.quickshop.Event;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopModerator;

/**
 * Calling when moderator was changed, Can't cancel
 **/

public class ShopModeratorChangedEvent extends QSEvent {

    @Getter
    @NotNull
    private final ShopModerator moderator;

    @Getter
    @NotNull
    private final Shop shop;

    /**
     * Will call when shop price was changed.
     *
     * @param shop          Target shop
     * @param shopModerator The shop moderator
     */
    public ShopModeratorChangedEvent(@NotNull Shop shop, @NotNull ShopModerator shopModerator) {
        this.shop = shop;
        this.moderator = shopModerator;
    }

}
