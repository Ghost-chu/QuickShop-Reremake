package org.maxgamer.quickshop.Shop;

import lombok.*;

@Getter
@Setter
public class QueueShopObject {
    /**
     * The shop you want to do actions
     */
    private Shop shop;
    /**
     * The actions you want to do
     */
    private QueueAction[] action;

    /**
     * @param shop   The shop
     * @param action The action you want to do for the shop.
     */
    public QueueShopObject(Shop shop, QueueAction... action) {
        this.shop = shop;
        this.action = action;
    }

}
