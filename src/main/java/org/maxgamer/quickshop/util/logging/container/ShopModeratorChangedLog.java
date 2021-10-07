package org.maxgamer.quickshop.util.logging.container;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.maxgamer.quickshop.api.shop.ShopInfoStorage;
import org.maxgamer.quickshop.shop.JavaShopModerator;

@AllArgsConstructor
@Data
public class ShopModeratorChangedLog {
    private ShopInfoStorage shop;
    private JavaShopModerator moderator;
}
