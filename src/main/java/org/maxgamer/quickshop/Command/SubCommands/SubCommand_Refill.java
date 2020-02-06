/*
 * This file is a part of project QuickShop, the name is SubCommand_Refill.java
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
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;

public class SubCommand_Refill implements CommandProcesser {

  private final QuickShop plugin = QuickShop.instance;

  @NotNull
  @Override
  public List<String> onTabComplete(
      @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
    final ArrayList<String> list = new ArrayList<>();

    list.add(MsgUtil.getMessage("tabcomplete.amount", sender));

    return list;
  }

  @Override
  public void onCommand(
      @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("Can't run by Console");
      return;
    }

    if (cmdArg.length < 1) {
      sender.sendMessage(MsgUtil.getMessage("command.no-amount-given", sender));
      return;
    }

    final int add;

    try {
      add = Integer.parseInt(cmdArg[0]);
    } catch (NumberFormatException e) {
      sender.sendMessage(MsgUtil.getMessage("thats-not-a-number", sender));
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

      shop.add(shop.getItem(), add);
      sender.sendMessage(MsgUtil.getMessage("refill-success", sender));
      return;
    }

    sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop", sender));
  }
}
