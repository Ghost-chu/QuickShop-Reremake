/*
 * This file is a part of project QuickShop, the name is QuickChatType.java
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

package org.maxgamer.quickshop.chat;

import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.chat.platform.minedown.BungeeQuickChat;

public enum QuickChatType {
    /*
     * BUNGEECHAT = Use the chat lib that Spigot offered.
     * ADVENTURE  = Use the chat lib Adventure, it can resolve player disconnected issue but have no message output bug.
     */
    BUNGEECHAT(0);
    //ADVENTURE(1);

    private final int id;

    QuickChatType(int id) {
        this.id = id;
    }

    public static @NotNull QuickChatType fromID(int id) {
        for (QuickChatType type : QuickChatType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return BUNGEECHAT;
    }

    public static int toID(@NotNull QuickChatType type) {
        return type.id;
    }

    public static QuickChat createById(int id) {
//        QuickChatType type = fromID(id);
//        if (type == QuickChatType.ADVENTURE) {
//            return new AdventureQuickChat();
//        }
        return new BungeeQuickChat();
    }

    public static QuickChat createByType(QuickChatType type) {
//        if (type == QuickChatType.ADVENTURE) {
//            return new AdventureQuickChat();
//        }
        return new BungeeQuickChat();
    }

    public int toID() {
        return id;
    }
}
