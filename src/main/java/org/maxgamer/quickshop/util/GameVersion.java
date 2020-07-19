package org.maxgamer.quickshop.util;

import org.jetbrains.annotations.NotNull;

public enum GameVersion {
    v1_5_R1(false, false, false),
    v1_5_R2(false, false, false),
    v1_5_R3(false, false, false),
    v1_6_R1(false, false, false),
    v1_6_R2(false, false, false),
    v1_6_R3(false, false, false),
    v1_7_R1(false, false, false),
    v1_7_R2(false, false, false),
    v1_7_R3(false, false, false),
    v1_7_R4(false, false, false),
    v1_8_R1(false, false, false),
    v1_8_R2(false, false, false),
    v1_8_R3(false, false, false),
    v1_9_R1(false, false, false),
    v1_9_R2(false, false, false),
    v1_10_R1(false, false, false),
    v1_11_R1(false, false, false),
    v1_12_R1(false, false, false),
    v1_12_R2(false, false, false),
    v1_13_R1(false, false, false),
    v1_13_R2(true, true, false),
    v1_14_R1(true, true, true),
    v1_14_R2(true, true, true),
    v1_15_R1(true, true, true),
    v1_15_R2(true, true, true),
    v1_16_R1(true, true, true),
    v1_16_R2(true, true, true),
    UNKNOWN(true, true, true);
    private boolean coreSupports;
    private boolean virtualDisplaySupports;
    private boolean persistentStorageApiSupports;

    GameVersion(boolean coreSupports, boolean virtualDisplaySupports, boolean persistentStorageApiSupports) {
        this.coreSupports = coreSupports;
        this.virtualDisplaySupports = virtualDisplaySupports;
        this.persistentStorageApiSupports = persistentStorageApiSupports;
    }

    public boolean isCoreSupports() {
        return coreSupports;
    }

    public boolean isVirtualDisplaySupports() {
        return virtualDisplaySupports;
    }

    public boolean isPersistentStorageApiSupports() {
        return persistentStorageApiSupports;
    }

    /**
     * Matches the version that QuickShop supports or not
     *
     * @param nmsVersion The Minecraft NMS version
     * @return The object contains supports details for GameVersion
     */
    @NotNull
    public static GameVersion get(@NotNull String nmsVersion) {
        for (GameVersion version : GameVersion.values()) {
            if (version.name().equals(nmsVersion)) {
                return version;
            }
        }
        return UNKNOWN;
    }

}
