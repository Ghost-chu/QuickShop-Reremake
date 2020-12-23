/*
 * This file is a part of project QuickShop, the name is AdventureQuickChat.java
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

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.chat.QuickChat;
import org.maxgamer.quickshop.chat.QuickComponent;
import org.maxgamer.quickshop.util.Util;

public class AdventureQuickChat implements QuickChat {
    private final QuickShop plugin = QuickShop.getInstance();

    @Override
    public void send(@NotNull CommandSender receiver, @Nullable String message) {
        if (StringUtils.isEmpty(message)) {
            return;
        }
        plugin.getBukkitAudiences().sender(receiver).sendMessage(LegacyComponentSerializer.legacySection().deserialize(message));
    }

    @Override
    public void send(@NotNull CommandSender receiver, @Nullable QuickComponent component) {
        if (component == null) {
            return;
        }
        if ((component.get() instanceof Component)) {
            plugin.getBukkitAudiences().sender(receiver).sendMessage(Identity.nil(), (Component) component.get());
        }
        if ((component.get() instanceof ComponentLike)) {
            plugin.getBukkitAudiences().sender(receiver).sendMessage(Identity.nil(), (ComponentLike) component.get());
        }
        Util.debugLog("Illegal component " + component.get().getClass().getName() + " sending to " + this.getClass().getName() + " processor, rejected.");

    }
}
