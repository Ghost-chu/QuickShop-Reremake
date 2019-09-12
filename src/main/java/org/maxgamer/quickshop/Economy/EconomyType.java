package org.maxgamer.quickshop.Economy;

import org.jetbrains.annotations.NotNull;

public enum EconomyType {
    /*
    * UNKNOWN = FALLBACK TO VAULT
    * VAULT = USE VAULT API
    * RESERVE = USE RESERVE API
    * */
    UNKNOWN(-1), VAULT(0), RESERVE(1);
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

    private int id;

    EconomyType(int id) {
        this.id = id;
    }

    public int toID() {
        return id;
    }
}
