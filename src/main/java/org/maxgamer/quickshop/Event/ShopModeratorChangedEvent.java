package org.maxgamer.quickshop.Event;

import lombok.*;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopModerator;

/** Calling when moderator was changed, Can't cancel **/

public class ShopModeratorChangedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    @NotNull
    private ShopModerator moderator;
    @Getter
    @NotNull
    private Shop shop;

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

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
