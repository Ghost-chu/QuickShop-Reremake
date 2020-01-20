/*
 * This file is a part of project QuickShop, the name is EconomyType.java
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

package org.maxgamer.quickshop.Economy;

import org.jetbrains.annotations.NotNull;

public enum EconomyType {
    /*
     * UNKNOWN = FALLBACK TO VAULT
     * VAULT = USE VAULT API
     * RESERVE = USE RESERVE API
     * */
    UNKNOWN(-1), VAULT(0), RESERVE(1), MIXED(2);

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
