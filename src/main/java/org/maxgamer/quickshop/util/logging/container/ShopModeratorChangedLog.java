package org.maxgamer.quickshop.util.logging.container;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.maxgamer.quickshop.shop.ShopInfoStorage;
import org.maxgamer.quickshop.shop.ShopModerator;

@AllArgsConstructor
@Data
public class ShopModeratorChangedLog {
    private ShopInfoStorage shop;
    private ShopModerator moderator;
}
