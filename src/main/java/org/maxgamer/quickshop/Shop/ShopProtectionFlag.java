package org.maxgamer.quickshop.Shop;

import lombok.*;

@Getter
@Setter
public class ShopProtectionFlag {
    private String shopLocation;
    private String itemStackString;
    private String mark = "QuickShop DisplayItem";

    public ShopProtectionFlag(String shopLocation, String itemStackString) {
        this.shopLocation = shopLocation;
        this.itemStackString = itemStackString;
    }
}
