/*
 * This file is a part of project QuickShop, the name is DisplayType.java
 * Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 * Copyright (C) Bukkit Commons Studio and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.maxgamer.quickshop.Shop.DisplayItem;

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
        if (displayItem instanceof VirtualDisplayItem) {
            return VIRTUALITEM;
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
