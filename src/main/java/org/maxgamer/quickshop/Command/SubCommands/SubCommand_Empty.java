/*
 * This file is a part of project QuickShop, the name is SubCommand_Empty.java
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

package org.maxgamer.quickshop.Command.SubCommands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.ContainerShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;

public class SubCommand_Empty implements CommandProcesser {

  private final QuickShop plugin = QuickShop.instance;

  @NotNull
  @Override
  public List<String> onTabComplete(
      @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
    return new ArrayList<>();
  }

  @Override
  public void onCommand(
      @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("Can't run this command from Console");
      return;
    }

    final BlockIterator bIt = new BlockIterator((LivingEntity) sender, 10);

    if (!bIt.hasNext()) {
      sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop", sender));
      return;
    }

    while (bIt.hasNext()) {
      final Block b = bIt.next();
      final Shop shop = plugin.getShopManager().getShop(b.getLocation());

      if (shop == null) {
        continue;
      }

      if (shop instanceof ContainerShop) {
        final ContainerShop cs = (ContainerShop) shop;
        final Inventory inventory = cs.getInventory();

        if (inventory == null) {
          // TODO: 24/11/2019 Send message about that issue.
          return;
        }

        cs.getInventory().clear();
        sender.sendMessage(MsgUtil.getMessage("empty-success", sender));
      } else {
        sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop", sender));
      }

      return;
    }

    sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop", sender));
  }
}
