package org.maxgamer.quickshop.Shop;

import org.jetbrains.annotations.NotNull;

public enum ShopType {
    //SELLING = SELLMODE BUYING = BUY MODE
    SELLING(0), BUYING(1);

    public static @NotNull ShopType fromID(int id) {
        for (ShopType type : ShopType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return SELLING;
    }

    public static int toID(@NotNull ShopType shopType) {
        return shopType.id;
    }

    private int id;

    ShopType(int id) {
        this.id = id;
    }

    public int toID() {
        return id;
    }
}