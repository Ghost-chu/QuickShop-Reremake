package org.maxgamer.quickshop.Shop;

import lombok.ToString;

@ToString
public enum ShopAction {
    //buy = trading create = creating shop cancelled = stopped
    BUY(), CREATE(), CANCELLED()
}