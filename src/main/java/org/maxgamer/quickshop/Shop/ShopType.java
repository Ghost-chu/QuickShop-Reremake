package org.maxgamer.quickshop.Shop;

import org.jetbrains.annotations.*;

public enum ShopType {
    SELLING(0), BUYING(1);
    public static ShopType fromID(int id) {
        for (ShopType type : ShopType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
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