/*
 * This file is a part of project QuickShop, the name is ChatSheetPrinter.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;


@AllArgsConstructor
@Getter
@Setter
/*
 A utils for print sheet on chat.
*/
public class ChatSheetPrinter {
    private final CommandSender p;
    private final BukkitAudiences audiences = BukkitAudiences.create(QuickShop.getInstance());

    public void printCenterLine(@NotNull String text) {
        if (!text.isEmpty()) {
            MsgUtil.sendMessage(p,
                    NamedTextColor.DARK_PURPLE
                            + MsgUtil.getMessage("tableformat.left_half_line", p)
                            + text
                            + MsgUtil.getMessage("tableformat.right_half_line", p));
        }
    }

    public void printExecuteableCmdLine(
            @NotNull String text, @NotNull String hoverText, @NotNull String executeCmd) {
        TextComponent message =
                Component.text(MsgUtil.getMessage("tableformat.left_begin", p) + text)
                        .color(NamedTextColor.DARK_PURPLE)
                        .clickEvent(ClickEvent.runCommand(executeCmd))
                        .hoverEvent(HoverEvent.showText(Component.text(hoverText)));
        audiences.sender(p).sendMessage(message);
    }

    public void printFooter() {
        MsgUtil.sendColoredMessage(p, ChatColor.DARK_PURPLE, MsgUtil.getMessage("tableformat.full_line", p));
    }

    public void printHeader() {
        MsgUtil.sendColoredMessage(p, ChatColor.DARK_PURPLE, MsgUtil.getMessage("tableformat.full_line", p));
    }

    public void printLine(@NotNull String text) {
        String[] texts = text.split("\n");
        for (String str : texts) {
            if (!str.isEmpty()) {
                MsgUtil.sendMessage(p, ChatColor.DARK_PURPLE + MsgUtil.getMessage("tableformat.left_begin", p) + str);
            }
        }
    }

    public void printSuggestableCmdLine(
            @NotNull String text, @NotNull String hoverText, @NotNull String suggestCmd, Component... additionText) {
        TextComponent message = Component.text(MsgUtil.getMessage("tableformat.left_begin", p) + text)
                .color(NamedTextColor.DARK_PURPLE)
                .clickEvent(ClickEvent.suggestCommand(suggestCmd))
                .hoverEvent(HoverEvent.showText(Component.text(hoverText)));
        if (additionText.length >= 1) {
            for (Component component : additionText) {
                message = message.append(component);
            }
        }
        audiences.sender(p).sendMessage(message);
    }

}
