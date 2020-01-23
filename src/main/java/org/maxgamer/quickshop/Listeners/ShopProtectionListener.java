/*
 * This file is a part of project QuickShop, the name is ShopProtectionListener.java
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

package org.maxgamer.quickshop.Listeners;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

@SuppressWarnings("DuplicatedCode")
public class ShopProtectionListener implements Listener {

  @NotNull private final QuickShop plugin;
  private final boolean useEnhanceProtection;
  // Protect Minecart steal shop
  Map.Entry<Location, Boolean> lastInventoryMoveItemCheck =
      new AbstractMap.SimpleEntry<>(null, null);

  public ShopProtectionListener(@NotNull QuickShop plugin) {
    this.plugin = plugin;
    useEnhanceProtection = plugin.getConfig().getBoolean("shop.enchance-shop-protect");
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBlockExplode(BlockExplodeEvent e) {

    for (int i = 0; i < e.blockList().size(); i++) {
      final Block b = e.blockList().get(i);
      final Shop shop = plugin.getShopManager().getShopIncludeAttached(b.getLocation());

      if (shop != null) {
        if (plugin.getConfig().getBoolean("protect.explode")) {
          e.setCancelled(true);
        } else {
          shop.delete();
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBlockFromTo(BlockFromToEvent e) {
    if (!useEnhanceProtection) {
      return;
    }

    final Shop shop = plugin.getShopManager().getShopIncludeAttached(e.getToBlock().getLocation());

    if (shop == null) {
      return;
    }

    e.setCancelled(true);
  }

  // Protect Redstone active shop
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBlockRedstoneChange(BlockRedstoneEvent event) {
    if (!useEnhanceProtection) {
      return;
    }

    final Shop shop =
        plugin.getShopManager().getShopIncludeAttached(event.getBlock().getLocation());

    if (shop == null) {
      return;
    }

    event.setNewCurrent(event.getOldCurrent());
    // plugin.getLogger().warning("[Exploit Alert] a Redstone tried to active of " + shop);
    // Util.debugLog(ChatColor.RED + "[QuickShop][Exploit alert] Redstone was activated on the
    // following shop " + shop);
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBlockSpread(BlockSpreadEvent e) {
    if (!useEnhanceProtection) {
      return;
    }

    final Block newBlock = e.getNewState().getBlock();
    final Shop thisBlockShop =
        plugin.getShopManager().getShopIncludeAttached(newBlock.getLocation());
    final Shop underBlockShop =
        plugin
            .getShopManager()
            .getShopIncludeAttached(newBlock.getRelative(BlockFace.DOWN).getLocation());

    if (thisBlockShop == null && underBlockShop == null) {
      return;
    }
    e.setCancelled(true);
  }

  /*
   * Handles shops breaking through explosions
   */
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onExplode(EntityExplodeEvent e) {

    for (int i = 0; i < e.blockList().size(); i++) {
      final Block b = e.blockList().get(i);
      final Shop shop = plugin.getShopManager().getShopIncludeAttached(b.getLocation());

      if (shop == null) {
        continue;
      }
      if (plugin.getConfig().getBoolean("protect.explode")) {
        e.setCancelled(true);
      } else {
        shop.delete();
      }
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
  public void onInventoryMove(InventoryMoveItemEvent event) {

    final Location loc = event.getSource().getLocation();

    if (loc == null) {
      return;
    }

    if (!Util.isShoppables(loc.getBlock().getType())) {
      return;
    }

    final Shop shop = plugin.getShopManager().getShopIncludeAttached(loc);

    if (shop == null) {
      return;
    }

    event.setCancelled(true);

    final Location location = event.getInitiator().getLocation();

    if (location == null) {
      return;
    }

    final InventoryHolder holder = event.getInitiator().getHolder();

    if (holder instanceof Entity) {
      ((Entity) holder).remove();
    } else if (holder instanceof Block) {
      location.getBlock().breakNaturally();
    } else {
      Util.debugLog("Unknown location = " + loc);
    }

    if (sendProtectionAlert) {
      MsgUtil.sendGlobalAlert("[DisplayGuard] Defened a item steal action at" + location);
    }
  }
  private boolean sendProtectionAlert = QuickShop.instance.getConfig().getBoolean("send-shop-protection-alert",false);

  // Protect Entity pickup shop
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onMobChangeBlock(EntityChangeBlockEvent event) {
    if (!useEnhanceProtection) {
      return;
    }

    final Shop shop =
        plugin.getShopManager().getShopIncludeAttached(event.getBlock().getLocation());

    if (shop == null) {
      return;
    }

    if (plugin.getConfig().getBoolean("protect.entity")) {
      event.setCancelled(true);
      return;
    }

    shop.delete();
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onStructureGrow(StructureGrowEvent event) {
    if (!useEnhanceProtection) {
      return;
    }

    for (BlockState blockstate : event.getBlocks()) {
      final Shop shop = plugin.getShopManager().getShopIncludeAttached(blockstate.getLocation());

      if (shop == null) {
        continue;
      }

      event.setCancelled(true);
      return;
      // plugin.getLogger().warning("[Exploit Alert] a StructureGrowing tried to break the shop of "
      // + shop);
      // Util.sendMessageToOps(ChatColor.RED + "[QuickShop][Exploit alert] A StructureGrowing tried
      // to break the shop of " + shop);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onSpongeing(SpongeAbsorbEvent event) {
    if (!useEnhanceProtection) {
      return;
    }
    List<BlockState> blocks = event.getBlocks();
    for (BlockState block : blocks) {
      if (plugin.getShopManager().getShopIncludeAttached(block.getLocation()) != null) {
        event.setCancelled(true);
      }
    }
  }
}
