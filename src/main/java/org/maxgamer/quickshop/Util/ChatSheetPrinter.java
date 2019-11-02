package org.maxgamer.quickshop.Util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
@Setter
/*
  A utils for print sheet on chat.
 */
public class ChatSheetPrinter {
    final ChatColor chatColor = ChatColor.DARK_PURPLE;
    //StringBuffer buffer = new StringBuffer();
    private CommandSender p;

    public void printCenterLine(@NotNull String text) {
        p.sendMessage(chatColor + MsgUtil.getMessage("tableformat.left_half_line",p) + text + MsgUtil
                .getMessage("tableformat.right_half_line",p));
    }

    public void printExecuteableCmdLine(@NotNull String text, @NotNull String hoverText, @NotNull String executeCmd) {
        TextComponent message = new TextComponent(chatColor + MsgUtil.getMessage("tableformat.left_begin",p) + text);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, executeCmd));
        message.setHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
        p.spigot().sendMessage(message);
    }

    public void printFooter() {
        p.sendMessage(chatColor + MsgUtil.getMessage("tableformat.full_line",p));
    }

    public void printHeader() {
        p.sendMessage("");
        p.sendMessage("");
        p.sendMessage(chatColor + MsgUtil.getMessage("tableformat.full_line",p));
    }

    public void printLine(@NotNull String text) {
        p.sendMessage(chatColor + MsgUtil.getMessage("tableformat.left_begin",p) + " " + text);
    }

    public void printSuggestableCmdLine(@NotNull String text, @NotNull String hoverText, @NotNull String suggestCmd) {
        TextComponent message = new TextComponent(chatColor + MsgUtil.getMessage("tableformat.left_begin",p) + text);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestCmd));
        message.setHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
        p.spigot().sendMessage(message);
    }
}
