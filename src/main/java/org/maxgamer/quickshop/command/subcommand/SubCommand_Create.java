/*
 * This file is a part of project QuickShop, the name is SubCommand_Create.java
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

package org.maxgamer.quickshop.command.subcommand;

import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandProcesser;
import org.maxgamer.quickshop.shop.Info;
import org.maxgamer.quickshop.shop.ShopAction;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class SubCommand_Create implements CommandProcesser {

    private final QuickShop plugin;

    @Override
    public void onCommand(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (!(sender instanceof Player)) {
            MsgUtil.sendMessage(sender, "This command can't be run by console");
            return;
        }

        final Player p = (Player) sender;
        final ItemStack item = p.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            MsgUtil.sendMessage(sender, MsgUtil.getMessage("no-anythings-in-your-hand", sender));
            return;
        }

        final BlockIterator bIt = new BlockIterator((LivingEntity) sender, 10);

        while (bIt.hasNext()) {
            final Block b = bIt.next();

            if (!Util.canBeShop(b)) {
                continue;
            }

            if (p.isOnline() && !plugin.getPermissionChecker().canBuild(p, b)) {
                Util.debugLog("Failed permission build check, canceled");
                return;
            }

            BlockFace blockFace;
            try {
                blockFace = p.getFacing();
            } catch (Throwable throwable) {
                blockFace = Util.getYawFace(p.getLocation().getYaw());
            }

            if (!plugin.getShopManager().canBuildShop(p, b, blockFace)) {
                // As of the new checking system, most plugins will tell the
                // player why they can't create a shop there.
                // So telling them a message would cause spam etc.
                Util.debugLog("Util report you can't build shop there.");
                return;
            }

            if (Util.getSecondHalf(b) != null
                    && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.double")) {
                MsgUtil.sendMessage(p, MsgUtil.getMessage("no-double-chests", sender));
                return;
            }

            if (Util.isBlacklisted(item)
                    && !QuickShop.getPermissionManager()
                    .hasPermission(p, "quickshop.bypass." + item.getType().name())) {
                MsgUtil.sendMessage(p, MsgUtil.getMessage("blacklisted-item", sender));
                return;
            }

            // Send creation menu.
            plugin
                    .getShopManager()
                    .getActions()
                    .put(
                            p.getUniqueId(),
                            new Info(
                                    b.getLocation(),
                                    ShopAction.CREATE,
                                    p.getInventory().getItemInMainHand(),
                                    b.getRelative(p.getFacing().getOppositeFace())));

            if (cmdArg.length >= 1) {
                plugin.getShopManager().handleChat(p, cmdArg[0]);

                return;
            }

            MsgUtil.sendMessage(p,
                    MsgUtil.getMessage("how-much-to-trade-for", sender, Util.getItemStackName(item), Integer.toString(plugin.isAllowStack() && QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.stacks") ? item.getAmount() : 1)));

            return;
        }
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return cmdArg.length == 1 ? Collections.singletonList(MsgUtil.getMessage("tabcomplete.price", sender)) : Collections.emptyList();
    }

}
