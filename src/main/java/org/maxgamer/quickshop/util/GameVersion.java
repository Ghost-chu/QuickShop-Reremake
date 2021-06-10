/*
 * This file is a part of project QuickShop, the name is GameVersion.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.util;

import org.jetbrains.annotations.NotNull;

/**
 * Utilities to help QuickShop quickly check server supported features
 *
 * @author Ghost_chu and sandtechnology
 */
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
    v1_16_R3(true, true, true),
    v1_16_R4(true, true, true),
    UNKNOWN(true, true, true);
    /**
     * CoreSupports - Check does QuickShop most features supports this server version
     */
    private final boolean coreSupports;
    /**
     * VirtualDisplaySupports - Check does QuickShop VirtualDisplayItem feature this server version
     */
    private final boolean virtualDisplaySupports;
    /**
     * PersistentStorageApiSupports - Check if server support PersistentStorage API
     */
    private final boolean persistentStorageApiSupports;

    /**
     * Utilities to help QuickShop quickly check server supported features
     *
     * @param coreSupports                 Does QuickShop most features supports this server version
     * @param virtualDisplaySupports       Does QuickShop VirtualDisplayItem feature this server version
     * @param persistentStorageApiSupports Does server support PersistentStorage API
     */
    GameVersion(boolean coreSupports, boolean virtualDisplaySupports, boolean persistentStorageApiSupports) {
        this.coreSupports = coreSupports;
        this.virtualDisplaySupports = virtualDisplaySupports;
        this.persistentStorageApiSupports = persistentStorageApiSupports;
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

    public boolean isCoreSupports() {
        return coreSupports;
    }

    public boolean isVirtualDisplaySupports() {
        return virtualDisplaySupports;
    }

    public boolean isPersistentStorageApiSupports() {
        return persistentStorageApiSupports;
    }
}
