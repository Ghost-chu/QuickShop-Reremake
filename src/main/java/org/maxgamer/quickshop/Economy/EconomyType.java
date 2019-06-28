package org.maxgamer.quickshop.Economy;

import org.jetbrains.annotations.*;

public enum EconomyType {
    UNKNOWN(-1), VAULT(0), RESERVE(1);
    private int id;

    EconomyType(int id) {
        this.id = id;
    }

    public static EconomyType fromID(int id) {
        for (EconomyType type : EconomyType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public static int toID(@NotNull EconomyType economyType) {
        return economyType.id;
    }

    public int toID() {
        return id;
    }
}
