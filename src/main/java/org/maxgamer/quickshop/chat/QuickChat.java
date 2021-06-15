/*
 * This file is a part of project QuickShop, the name is QuickChat.java
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

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.shop.Shop;

/**
 * QuickChat is a system to allow us to processing plain text and also MineDown
 */
public interface QuickChat {
    void send(@NotNull CommandSender receiver, @Nullable String message);

    void send(@NotNull CommandSender receiver, @Nullable QuickComponent component);

    void sendItemHologramChat(
            @NotNull Player player,
            @NotNull String text,
            @NotNull ItemStack itemStack);

    @NotNull QuickComponent getItemHologramChat(@NotNull Shop shop, @NotNull ItemStack itemStack, @NotNull Player player, @NotNull String
            message);


    @NotNull QuickComponent getItemTextComponent(@NotNull ItemStack itemStack, @NotNull Player player, @NotNull String normalText);

    void sendExecutableChat(@NotNull CommandSender receiver, @NotNull String message, @NotNull String hoverText, @NotNull String command);

    void sendSuggestedChat(@NotNull CommandSender receiver, @NotNull String message, @NotNull String hoverText, @NotNull String command);

}
