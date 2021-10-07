/*
 * This file is a part of project QuickShop, the name is BungeeQuickChat.java
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

package org.maxgamer.quickshop.chat.platform.minedown;

import lombok.AllArgsConstructor;
import net.md_5.bungee.api.chat.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.chat.QuickChat;
import org.maxgamer.quickshop.api.chat.QuickComponent;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.chat.QuickComponentImpl;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.ReflectFactory;
import org.maxgamer.quickshop.util.Util;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

/**
 * BungeeChat module to send complex chats and impl QuickChat
 *
 * @author Ghost_chu
 */
@AllArgsConstructor
public class BungeeQuickChat implements QuickChat {
    private final QuickShop plugin;

    @Override
    public void send(@NotNull CommandSender receiver, @Nullable String message) {
        if (StringUtils.isEmpty(message)) {
            return;
        }
        receiver.spigot().sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public void send(@NotNull CommandSender receiver, @Nullable QuickComponent component) {
        if (component == null) {
            return;
        }
        if (component.get() instanceof BaseComponent[]) {
            receiver.spigot().sendMessage((BaseComponent[]) component.get());
            return;
        }
        if (component.get() instanceof BaseComponent) {
            receiver.spigot().sendMessage((BaseComponent) component.get());
            return;
        }
        Util.debugLog("Illegal component " + component.get().getClass().getName() + " sending to " + this.getClass().getName() + " processor, trying force sending.");

    }

    @Override
    public void sendItemHologramChat(@NotNull Player player, @NotNull String text, @NotNull ItemStack itemStack) {
        TextComponent errorComponent = new TextComponent(QuickShop.getInstance().text().of(player, "menu.item-holochat-error").forLocale());
        try {
            String json = ReflectFactory.convertBukkitItemStackToJson(itemStack);
            TextComponent centerItem = new TextComponent(text);
            ComponentBuilder cBuilder = new ComponentBuilder(json);
            centerItem.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, cBuilder.create())); //FIXME: Update this when drop 1.15 supports
            player.spigot().sendMessage(centerItem);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException | InstantiationException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to process chat component", e);
            player.spigot().sendMessage(errorComponent);
        }
    }

    @Override
    public @NotNull QuickComponent getItemHologramChat(@NotNull Shop shop, @NotNull ItemStack itemStack, @NotNull Player player, @NotNull String message) {
        TextComponent errorComponent = new TextComponent(QuickShop.getInstance().text().of(player, "menu.item-holochat-error").forLocale());
        try {

            String json = ReflectFactory.convertBukkitItemStackToJson(itemStack);
            if (json == null) {
                return new QuickComponentImpl(errorComponent);
            }
            TextComponent normalmessage = new TextComponent(message + " " + QuickShop.getInstance().text().of(player, "menu.preview").forLocale());
            ComponentBuilder cBuilder = new ComponentBuilder(json);
            if (QuickShop.getPermissionManager().hasPermission(player, "quickshop.preview")) {
                normalmessage.setClickEvent(new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        MsgUtil.fillArgs(
                                "/qs silentpreview {0}",
                                shop.getRuntimeRandomUniqueId().toString())));
            }
            normalmessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, cBuilder.create())); //FIXME: Update this when drop 1.15 supports
            return new QuickComponentImpl(normalmessage);
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "Failed to process chat component", t);
            return new QuickComponentImpl(errorComponent);
        }
    }

    @Override
    public @NotNull QuickComponent getItemTextComponent(@NotNull Player player, @NotNull ItemStack itemStack, @NotNull String normalText) {
        TextComponent errorComponent = new TextComponent(QuickShop.getInstance().text().of(player, "menu.item-holochat-error").forLocale());

        String json;
        try {
            json = ReflectFactory.convertBukkitItemStackToJson(itemStack);
        } catch (Throwable throwable) {
            plugin.getLogger().log(Level.SEVERE, "Failed to saving item to json for holochat", throwable);
            return new QuickComponentImpl(errorComponent);
        }
        if (json == null) {
            return new QuickComponentImpl(errorComponent);
        }

        TextComponent component = new TextComponent(normalText + " " + QuickShop.getInstance().text().of(player, "menu.preview").forLocale());
        ComponentBuilder cBuilder = new ComponentBuilder(json);
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, cBuilder.create()));
        return new QuickComponentImpl(component);

    }

    @Override
    public void sendExecutableChat(@NotNull CommandSender receiver, @NotNull String message, @NotNull String hoverText, @NotNull String command) {
        TextComponent component =
                new TextComponent(ChatColor.DARK_PURPLE + plugin.text().of(receiver, "tableformat.left_begin").forLocale() + message);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        component.setHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create())); //FIXME: Update this when drop 1.15 supports
        receiver.spigot().sendMessage(component);
    }

    @Override
    public void sendSuggestedChat(@NotNull CommandSender receiver, @NotNull String message, @NotNull String hoverText, @NotNull String command) {
        TextComponent component =
                new TextComponent(ChatColor.DARK_PURPLE + plugin.text().of(receiver, "tableformat.left_begin").forLocale() + message);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        component.setHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create())); //FIXME: Update this when drop 1.15 supports
        receiver.spigot().sendMessage(component);
    }
}
