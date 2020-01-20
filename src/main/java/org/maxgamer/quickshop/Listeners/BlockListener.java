/*
 * This file is a part of project QuickShop, the name is BlockListener.java
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

import lombok.AllArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Info;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopAction;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

@AllArgsConstructor
public class BlockListener implements Listener {

  @NotNull private final QuickShop plugin;

  /**
   * Gets the shop a sign is attached to
   *
   * @param loc The location of the sign
   * @return The shop
   */
  @Nullable
  private Shop getShopNextTo(@NotNull Location loc) {
    final Block b = Util.getAttached(loc.getBlock());
    // Util.getAttached(b)
    if (b == null) {
      return null;
    }

    return plugin.getShopManager().getShop(b.getLocation());
  }

  /*
   * Removes chests when they're destroyed.
   */
  @EventHandler(priority = EventPriority.LOW)
  public void onBreak(BlockBreakEvent e) {
    if (ListenerHelper.isDisabled(e.getClass())) {
      return;
    }

    final Block b = e.getBlock();

    if (b.getState() instanceof Sign) {
      Sign sign = (Sign) b.getState();
      if (plugin.getConfig().getBoolean("lockette.enable")
              && sign.getLine(0).equals(plugin.getConfig().getString("lockette.private"))
          || sign.getLine(0).equals(plugin.getConfig().getString("lockette.more_users"))) {
        // Ignore break lockette sign
        plugin
            .getLogger()
            .info("Skipped a dead-lock shop sign.(Lockette or other sign-lock plugin)");
        return;
      }
    }

    final Player p = e.getPlayer();
    // If the shop was a chest
    if (Util.canBeShop(b)) {
      final Shop shop = plugin.getShopManager().getShop(b.getLocation());
      if (shop == null) {
        return;
      }
      // If they're either survival or the owner, they can break it
      if (p.getGameMode() == GameMode.CREATIVE && !p.getUniqueId().equals(shop.getOwner())) {
        // Check SuperTool
        if (p.getInventory().getItemInMainHand().getType() == Material.GOLDEN_AXE) {
          p.sendMessage(MsgUtil.getMessage("break-shop-use-supertool", p));
          return;
        }
        e.setCancelled(true);
        p.sendMessage(
            MsgUtil.getMessage(
                "no-creative-break", p, MsgUtil.getItemi18n(Material.GOLDEN_AXE.name())));
        return;
      }

      if (e.isCancelled()) {
        p.sendMessage(MsgUtil.getMessage("no-permission", p));
        Util.debugLog("The action was cancelled by other plugin");
        return;
      }

      if (!shop.getModerator().isOwner(p.getUniqueId())
          && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.other.destroy")) {
        e.setCancelled(true);
        p.sendMessage(MsgUtil.getMessage("no-permission", p));
        return;
      }
      // Cancel their current menu... Doesnt cancel other's menu's.
      final Info action = plugin.getShopManager().getActions().get(p.getUniqueId());

      if (action != null) {
        action.setAction(ShopAction.CANCELLED);
      }

      shop.onUnload();
      shop.delete();
      p.sendMessage(MsgUtil.getMessage("success-removed-shop", p));
    } else if (Util.isWallSign(b.getType())) {
      if (b instanceof Sign) {
        Sign sign = (Sign) b;
        if (sign.getLine(0).equals(plugin.getConfig().getString("lockette.private"))
            || sign.getLine(0).equals(plugin.getConfig().getString("lockette.more_users"))) {
          // Ignore break lockette sign
          return;
        }
      }

      final Shop shop = getShopNextTo(b.getLocation());

      if (shop == null) {
        return;
      }
      // If they're in creative and not the owner, don't let them
      // (accidents happen)
      if (p.getGameMode() == GameMode.CREATIVE && !p.getUniqueId().equals(shop.getOwner())) {
        // Check SuperTool
        if (p.getInventory().getItemInMainHand().getType() == Material.GOLDEN_AXE) {
          p.sendMessage(MsgUtil.getMessage("break-shop-use-supertool", p));
          shop.delete();
          return;
        }
        e.setCancelled(true);
        p.sendMessage(
            MsgUtil.getMessage(
                "no-creative-break", p, MsgUtil.getItemi18n(Material.GOLDEN_AXE.name())));
      }

      Util.debugLog("Cannot break the sign.");
      e.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onInventoryMove(InventoryMoveItemEvent event) {
    if (ListenerHelper.isDisabled(event.getClass())) {
      return;
    }

    if (!plugin.getConfig().getBoolean("shop.update-sign-when-inventory-moving", true)) {
      return;
    }

    final Inventory inventory = event.getDestination();
    final Location location = inventory.getLocation();

    if (location == null) {
      return;
    }

    // Delayed task. Event triggers when item is moved, not when it is received.
    final Shop shop = plugin.getShopManager().getShopIncludeAttached(location);
    if (shop != null) {
      plugin.getSignUpdateWatcher().scheduleSignUpdate(shop);
    }
  }

  /*
   * Listens for chest placement, so a doublechest shop can't be created.
   */
  @EventHandler(ignoreCancelled = true)
  public void onPlace(BlockPlaceEvent e) {
    if (ListenerHelper.isDisabled(e.getClass())) {
      return;
    }

    final BlockState bs = e.getBlock().getState();

    if (!(bs instanceof DoubleChest)) {
      return;
    }

    final Block b = e.getBlock();
    final Player p = e.getPlayer();
    final Block chest = Util.getSecondHalf(b);

    if (chest != null
        && plugin.getShopManager().getShop(chest.getLocation()) != null
        && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.double")) {
      e.setCancelled(true);
      p.sendMessage(MsgUtil.getMessage("no-double-chests", p));
    }
  }
}
