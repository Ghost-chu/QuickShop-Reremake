/*
 * This file is a part of project QuickShop, the name is LockListener.java
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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.InventoryPreview;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

@AllArgsConstructor
public class LockListener implements Listener {

  @NotNull private final QuickShop plugin;

  @EventHandler(ignoreCancelled = true)
  public void invEvent(InventoryMoveItemEvent e) {
    if (!InventoryPreview.isPreviewItem(e.getItem())) {
      return;
    }

    e.setCancelled(true);
  }

  @EventHandler
  public void invEvent(InventoryClickEvent e) {
    if (InventoryPreview.isPreviewItem(e.getCursor())) {
      e.setCancelled(true);
      e.setResult(Event.Result.DENY);
      return;
    }

    if (InventoryPreview.isPreviewItem(e.getCurrentItem())) {
      e.setCancelled(true);
      e.setResult(Event.Result.DENY);
    }
  }

  @EventHandler
  public void invEvent(InventoryDragEvent e) {
    if (InventoryPreview.isPreviewItem(e.getCursor())) {
      e.setCancelled(true);
      e.setResult(Event.Result.DENY);
      return;
    }

    if (InventoryPreview.isPreviewItem(e.getOldCursor())) {
      e.setCancelled(true);
      e.setResult(Event.Result.DENY);
    }
  }

  @EventHandler
  public void invEvent(InventoryPickupItemEvent e) {
    final Inventory inventory = e.getInventory();
    final ItemStack[] stacks = inventory.getContents();

    for (ItemStack itemStack : stacks) {
      if (itemStack == null) {
        continue;
      }

      if (InventoryPreview.isPreviewItem(itemStack)) {
        e.setCancelled(true);
        return;
      }
    }
  }

  /*
   * Removes chests when they're destroyed.
   */
  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onBreak(BlockBreakEvent e) {
    Block b = e.getBlock();

    if (b.getState() instanceof Sign) {
      final Sign sign = (Sign) b.getState();

      if (sign.getLine(0).equals(plugin.getConfig().getString("lockette.private"))
          || sign.getLine(0).equals(plugin.getConfig().getString("lockette.more_users"))) {
        // Ignore break lockette sign
        plugin
            .getLogger()
            .info("Skipped a dead-lock shop sign.(Lockette or other sign-lock plugin)");
        return;
      }
    }

    final Player p = e.getPlayer();
    // If the chest was a chest
    if (Util.canBeShop(b)) {
      final Shop shop = plugin.getShopManager().getShopIncludeAttached(b.getLocation());

      if (shop == null) {
        return; // Wasn't a shop
      }
      // If they owned it or have bypass perms, they can destroy it
      if (!shop.getOwner().equals(p.getUniqueId())
          && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.other.destroy")) {
        e.setCancelled(true);
        p.sendMessage(MsgUtil.getMessage("no-permission", p));
      }
    } else if (Util.isWallSign(b.getType())) {
      if (b instanceof Sign) {
        final Sign sign = (Sign) b;

        if (sign.getLine(0).equals(plugin.getConfig().getString("lockette.private"))
            || sign.getLine(0).equals(plugin.getConfig().getString("lockette.more_users"))) {
          // Ignore break lockette sign
          Util.debugLog("Skipped a dead-lock shop sign.(Lockette)");
          return;
        }
      }
      b = Util.getAttached(b);

      if (b == null) {
        return;
      }

      final Shop shop = plugin.getShopManager().getShop(b.getLocation());

      if (shop == null) {
        return;
      }
      // If they're the shop owner or have bypass perms, they can destroy
      // it.
      if (!shop.getOwner().equals(p.getUniqueId())
          && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.other.destroy")) {
        e.setCancelled(true);
        p.sendMessage(MsgUtil.getMessage("no-permission", p));
      }
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onClick(PlayerInteractEvent e) {
    if (ListenerHelper.isDisabled(e.getClass())) {
      return;
    }

    final Block b = e.getClickedBlock();

    if (b == null) {
      return;
    }

    if (!Util.canBeShop(b)) {
      return;
    }

    final Player p = e.getPlayer();

    if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return; // Didn't right click it, we dont care.
    }

    final Shop shop = plugin.getShopManager().getShopIncludeAttached(b.getLocation());
    // Make sure they're not using the non-shop half of a double chest.
    if (shop == null) {
      return;
    }

    if (!shop.getModerator().isModerator(p.getUniqueId())) {
      if (QuickShop.getPermissionManager().hasPermission(p, "quickshop.other.open")) {
        p.sendMessage(MsgUtil.getMessage("bypassing-lock", p));
        return;
      }
      p.sendMessage(MsgUtil.getMessage("that-is-locked", p));
      e.setCancelled(true);
    }
  }
  /* Moved to ShopProtectionListener
   * Handles shops breaking through explosions
  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onExplode(EntityExplodeEvent e) {
      for (int i = 0; i < e.blockList().size(); i++) {
          Block b = e.blockList().get(i);
          Shop shop = plugin.getShopManager().getShop(b.getLocation());
          if (shop != null) {
              e.blockList().remove(b); //Protect shop
          }
          if (Util.isWallSign(b.getType())) {
              Block block = Util.getAttached(b);
              if (block != null) {
                  shop = plugin.getShopManager().getShop(block.getLocation());
                  if (shop != null) {
                      e.blockList().remove(b); //Protect shop
                  }
              }
          }
      }
  }
   */

  /*
   * Handles hopper placement
   */
  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onPlace(BlockPlaceEvent e) {
    if (ListenerHelper.isDisabled(e.getClass())) {
      return;
    }

    final Block b = e.getBlock();

    if (b.getType() != Material.HOPPER) {
      return;
    }

    final Player p = e.getPlayer();

    if (!Util.isOtherShopWithinHopperReach(b, p)) {
      return;
    }

    if (QuickShop.getPermissionManager().hasPermission(p, "quickshop.other.open")) {
      p.sendMessage(MsgUtil.getMessage("bypassing-lock", p));
      return;
    }

    p.sendMessage(MsgUtil.getMessage("that-is-locked", p));
    e.setCancelled(true);
  }
}
