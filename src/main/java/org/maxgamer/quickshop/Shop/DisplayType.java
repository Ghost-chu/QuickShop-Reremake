package org.maxgamer.quickshop.Shop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum DisplayType {
    /*
     * UNKNOWN = FALLBACK TO REALITEM
     * REALITEM = USE REAL DROPPED ITEM
     * ARMORSTAND = USE ARMORSTAND DISPLAY
     * VIRTUALITEM = USE VIRTUAL DROPPED ITEM (CLIENT SIDE)
     * */
    UNKNOWN(-1), REALITEM(0), ARMORSTAND(1), VIRTUALITEM(2);

    public static @NotNull DisplayType fromID(int id) {
        for (DisplayType type : DisplayType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public static int toID(@NotNull DisplayType displayType) {
        return displayType.id;
    }

    public static DisplayType typeIs(@Nullable DisplayItem displayItem) {
        if (displayItem instanceof RealDisplayItem) {
            return REALITEM;
        }
        if (displayItem instanceof ArmorStandDisplayItem) {
            return ARMORSTAND;
        }
        return UNKNOWN;
    }

    private int id;

    DisplayType(int id) {
        this.id = id;
    }

    public int toID() {
        return id;
    }
}
