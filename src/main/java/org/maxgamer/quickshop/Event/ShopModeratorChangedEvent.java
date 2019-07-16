package org.maxgamer.quickshop.Event;

import lombok.*;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopModerator;

/** Calling when moderator was changed, Can't cancel **/
@Builder
public class ShopModeratorChangedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    @NonNull
    private Shop shop;
    @Getter
    @NonNull
    private ShopModerator moderator;

    /**
     * Will call when shop price was changed.
     *
     * @param shop          Target shop
     * @param shopModerator The shop moderator
     */
    public ShopModeratorChangedEvent(@NonNull Shop shop, @NonNull ShopModerator shopModerator) {
        this.shop = shop;
        this.moderator = shopModerator;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
