/*
 * This file is a part of project QuickShop, the name is CommandContainer.java
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

package org.maxgamer.quickshop.command;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.util.MsgUtil;

import java.util.List;
import java.util.function.Function;

@Data
@Builder
public class CommandContainer {
    @NotNull
    private CommandProcesser executor;

    private boolean hidden; // Hide from help, tabcomplete
    /*
      E.g you can use the command when having quickshop.removeall.self or quickshop.removeall.others permission
    */
    @Singular
    private List<String> selectivePermissions;
    @Singular
    private List<String> permissions; // E.g quickshop.unlimited
    @NotNull
    private String prefix; // E.g /qs <prefix>
    @Nullable
    private String description; // Will show in the /qs help

    private boolean disabled; //Set command is disabled or not.
    @Nullable
    private String disablePlaceholder; //Set the text shown if command disabled
    @Nullable
    private Function<@Nullable CommandSender, @NotNull String> disableCallback; //Set the callback that should return a text to shown

    public final @NotNull String getDisableText(@NotNull CommandSender sender) {
        if (this.getDisableCallback() != null) {
            return this.getDisableCallback().apply(sender);
        } else if (StringUtils.isNotEmpty(this.getDisablePlaceholder())) {
            return this.getDisablePlaceholder();
        } else {
            return MsgUtil.getMessage("command.feature-not-enabled", null);
        }
    }


}
