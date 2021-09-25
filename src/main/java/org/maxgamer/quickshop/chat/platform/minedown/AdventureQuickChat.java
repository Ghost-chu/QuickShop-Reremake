///*
// * This file is a part of project QuickShop, the name is AdventureQuickChat.java
// *  Copyright (C) PotatoCraft Studio and contributors
// *
// *  This program is free software: you can redistribute it and/or modify it
// *  under the terms of the GNU General Public License as published by the
// *  Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// *
// *  This program is distributed in the hope that it will be useful, but WITHOUT
// *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// *  for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with this program. If not, see <http://www.gnu.org/licenses/>.
// *
// */
//
//package org.maxgamer.quickshop.chat.platform.minedown;
//
//import net.kyori.adventure.identity.Identity;
//import net.kyori.adventure.key.Key;
//import net.kyori.adventure.nbt.api.BinaryTagHolder;
//import net.kyori.adventure.text.Component;
//import net.kyori.adventure.text.ComponentLike;
//import net.kyori.adventure.text.TextComponent;
//import net.kyori.adventure.text.event.ClickEvent;
//import net.kyori.adventure.text.event.HoverEvent;
//import net.kyori.adventure.text.format.NamedTextColor;
//import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
//import org.apache.commons.lang.StringUtils;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//import org.bukkit.inventory.ItemStack;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//import org.maxgamer.quickshop.QuickShop;
//import org.maxgamer.quickshop.chat.QuickChat;
//import org.maxgamer.quickshop.chat.QuickComponent;
//import org.maxgamer.quickshop.chat.QuickComponentImpl;
//import org.maxgamer.quickshop.shop.Shop;
//import org.maxgamer.quickshop.util.MsgUtil;
//import org.maxgamer.quickshop.util.Util;
//
//import java.util.logging.Level;
//
//public class AdventureQuickChat implements QuickChat {
//    private final QuickShop plugin = QuickShop.getInstance();
//
//    @Override
//    public void send(@NotNull CommandSender receiver, @Nullable String message) {
//        if (StringUtils.isEmpty(message)) {
//            return;
//        }
//        plugin.adventure().sender(receiver).sendMessage(LegacyComponentSerializer.legacySection().deserialize(message));
//    }
//
//    @Override
//    public void send(@NotNull CommandSender receiver, @Nullable QuickComponent component) {
//        if (component == null) {
//            return;
//        }
//        if ((component.get() instanceof ComponentLike)) {
//            plugin.adventure().sender(receiver).sendMessage(Identity.nil(), (ComponentLike) component.get());
//            return;
//        }
//        Util.debugLog("Illegal component " + component.get().getClass().getName() + " sending to " + this.getClass().getName() + " processor, rejected.");
//
//    }
//
//    @Override
//    public void sendItemHologramChat(@NotNull Player player, @NotNull String text, @NotNull ItemStack itemStack) {
//
//    }
//
//
//    @Override
//    public @NotNull QuickComponent getItemHologramChat(@NotNull Shop shop, @NotNull ItemStack itemStack, @NotNull Player player, @NotNull String message) {
//        Component component = (Component) getItemTextComponent( player,itemStack, message).get();
//        if (QuickShop.getPermissionManager().hasPermission(player, "quickshop.preview")) {
//            component = component.clickEvent(ClickEvent.runCommand(MsgUtil.fillArgs(
//                    "/qs silentpreview {0}",
//                    shop.getRuntimeRandomUniqueId().toString())));
//        }
//        return new QuickComponentImpl(component);
//    }
//
//
//    @Override
//    public @NotNull QuickComponent getItemTextComponent( @NotNull Player player,@NotNull ItemStack itemStack, @NotNull String normalText) {
//        TextComponent errorComponent = Component.text(MsgUtil.getMessage("menu.item-holochat-error", player));
//        try {
//            String json = ItemNMS.saveJsonfromNMS(itemStack);
//            if (json != null) {
//                return new QuickComponentImpl(Component
//                        .text(normalText + " " + MsgUtil.getMessage("menu.preview", player))
//                        .hoverEvent(HoverEvent.showItem()));
//            }
//        } catch (Throwable throwable) {
//            plugin.getLogger().log(Level.SEVERE, "Failed to saving item to json for holochat", throwable);
//        }
//        return new QuickComponentImpl(errorComponent);
//
//    }
//
//    @Override
//    public void sendExecutableChat(@NotNull CommandSender receiver, @NotNull String message, @NotNull String hoverText, @NotNull String command) {
//        TextComponent component = Component.text(MsgUtil.getMessage("tableformat.left_begin", receiver) + message)
//                .color(NamedTextColor.DARK_PURPLE)
//                .clickEvent(ClickEvent.runCommand(command))
//                .hoverEvent(HoverEvent.showText(Component.text(hoverText)));
//
//        audiences.sender(receiver).sendMessage(component);
//    }
//
//    @Override
//    public void sendSuggestedChat(@NotNull CommandSender receiver, @NotNull String message, @NotNull String hoverText, @NotNull String command) {
//        TextComponent component = Component.text(MsgUtil.getMessage("tableformat.left_begin", receiver) + message)
//                .color(NamedTextColor.DARK_PURPLE)
//                .clickEvent(ClickEvent.suggestCommand(command))
//                .hoverEvent(HoverEvent.showText(Component.text(hoverText)));
//        audiences.sender(receiver).sendMessage(component);
//    }
//
//}
