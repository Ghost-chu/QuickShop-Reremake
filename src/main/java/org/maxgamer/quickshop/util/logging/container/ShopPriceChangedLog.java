package org.maxgamer.quickshop.util.logging.container;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.maxgamer.quickshop.api.shop.ShopInfoStorage;

@AllArgsConstructor
@Data
public class ShopPriceChangedLog {
    private ShopInfoStorage shop;
    private double oldPrice;
    private double newPrice;
}
