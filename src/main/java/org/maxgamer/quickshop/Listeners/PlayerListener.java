/*
 * This file is a part of project QuickShop, the name is PlayerListener.java
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

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Economy.Economy;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Info;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopAction;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

// import com.griefcraft.lwc.LWC;
// import com.griefcraft.lwc.LWCPlugin;
@AllArgsConstructor
public class PlayerListener implements Listener {

  @NotNull private final QuickShop plugin;

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onClick(PlayerInteractEvent e) {

    if (!e.getAction().equals(Action.LEFT_CLICK_BLOCK) && e.getClickedBlock() != null) {
      if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)
          && Util.isWallSign(e.getClickedBlock().getType())) {
        final Block block;

        if (Util.isWallSign(e.getClickedBlock().getType())) {
          block = Util.getAttached(e.getClickedBlock());
        } else {
          block = e.getClickedBlock();
        }

        if (plugin.getShopManager().getShop(Objects.requireNonNull(block).getLocation()) != null
            && (Objects.requireNonNull(plugin.getShopManager().getShop(block.getLocation()))
                    .getOwner()
                    .equals(e.getPlayer().getUniqueId())
                || e.getPlayer().isOp())) {
          if (plugin.getConfig().getBoolean("shop.sneak-to-control")
              && !e.getPlayer().isSneaking()) {
            return;
          }

          if (plugin.getConfig().getBoolean("effect.sound.onclick")) {
            e.getPlayer()
                .playSound(e.getPlayer().getLocation(), Sound.BLOCK_DISPENSER_FAIL, 80.f, 1.0f);
          }

          MsgUtil.sendControlPanelInfo(
              e.getPlayer(),
              Objects.requireNonNull(plugin.getShopManager().getShop(block.getLocation())));
          Objects.requireNonNull(plugin.getShopManager().getShop(block.getLocation()))
              .setSignText();
        }
      }

      return;
    }

    final Block b = e.getClickedBlock();

    if (b == null) {
      return;
    }

    if (!Util.canBeShop(b) && !Util.isWallSign(b.getType())) {
      return;
    }

    final Player p = e.getPlayer();
    final Location loc = b.getLocation();
    final ItemStack item = e.getItem();
    // Get the shop
    Shop shop = plugin.getShopManager().getShop(loc);
    // If that wasn't a shop, search nearby shops
    if (shop == null) {
      final Block attached;

      if (Util.isWallSign(b.getType())) {
        attached = Util.getAttached(b);

        if (attached != null) {
          shop = plugin.getShopManager().getShop(attached.getLocation());
        }
      } else if (Util.isDoubleChest(b)) {
        attached = Util.getSecondHalf(b);

        if (attached != null) {
          Shop secondHalfShop = plugin.getShopManager().getShop(attached.getLocation());
          if (secondHalfShop != null && !p.getUniqueId().equals(secondHalfShop.getOwner())) {
            // If player not the owner of the shop, make him select the second half of the
            // shop
            // Otherwise owner will be able to create new double chest shop
            shop = secondHalfShop;
          }
        }
      }
    }
    // Purchase handling
    if (shop != null && QuickShop.getPermissionManager().hasPermission(p, "quickshop.use")) {
      if (plugin.getConfig().getBoolean("shop.sneak-to-trade") && !p.isSneaking()) {
        return;
      }

      shop.onClick();

      if (plugin.getConfig().getBoolean("effect.sound.onclick")) {
        e.getPlayer()
            .playSound(e.getPlayer().getLocation(), Sound.BLOCK_DISPENSER_FAIL, 80.f, 1.0f);
      }
      // Text menu
      MsgUtil.sendShopInfo(p, shop);
      shop.setSignText();

      final Economy eco = plugin.getEconomy();
      final double price = shop.getPrice();
      final double money = plugin.getEconomy().getBalance(p.getUniqueId());

      if (shop.isSelling()) {
        int itemAmount =
            Math.min(
                Util.countSpace(p.getInventory(), shop.getItem()), (int) Math.floor(money / price));

        if (!shop.isUnlimited()) {
          itemAmount = Math.min(itemAmount, shop.getRemainingStock());
        }

        if (itemAmount < 0) {
          itemAmount = 0;
        }

        p.sendMessage(MsgUtil.getMessage("how-many-buy", p, "" + itemAmount));
      } else {
        final double ownerBalance = eco.getBalance(shop.getOwner());
        int items = Util.countItems(p.getInventory(), shop.getItem());
        final int ownerCanAfford = (int) (ownerBalance / shop.getPrice());

        if (!shop.isUnlimited()) {
          // Amount check player amount and shop empty slot
          items = Math.min(items, shop.getRemainingSpace());
          // Amount check player selling item total cost and the shop owner's balance
          items = Math.min(items, ownerCanAfford);
        } else if (plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners")) {
          // even if the shop is unlimited, the config option pay-unlimited-shop-owners is set to
          // true,
          // the unlimited shop owner should have enough money.
          items = Math.min(items, ownerCanAfford);
        }

        if (items < 0) {
          items = 0;
        }

        p.sendMessage(MsgUtil.getMessage("how-many-sell", p, "" + items));
      }
      // Add the new action
      HashMap<UUID, Info> actions = plugin.getShopManager().getActions();
      Info info = new Info(shop.getLocation(), ShopAction.BUY, null, null, shop);
      actions.put(p.getUniqueId(), info);
    }
    // Handles creating shops
    else if (e.useInteractedBlock() == Result.ALLOW
        && shop == null
        && item != null
        && item.getType() != Material.AIR
        && QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.sell")
        && p.getGameMode() != GameMode.CREATIVE) {
      if (e.useInteractedBlock() == Result.DENY
          || plugin.getConfig().getBoolean("shop.sneak-to-create") && !p.isSneaking()
          || !plugin.getShopManager().canBuildShop(p, b, e.getBlockFace())) {
        // As of the new checking system, most plugins will tell the
        // player why they can't create a shop there.
        // So telling them a message would cause spam etc.
        return;
      }

      if (Util.getSecondHalf(b) != null
          && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.double")) {
        p.sendMessage(MsgUtil.getMessage("no-double-chests", p));
        return;
      }

      if (Util.isBlacklisted(item)
          && !QuickShop.getPermissionManager()
              .hasPermission(p, "quickshop.bypass." + item.getType().name())) {
        p.sendMessage(MsgUtil.getMessage("blacklisted-item", p));
        return;
      }

      if (b.getType() == Material.ENDER_CHEST
          && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.enderchest")) {
        return;
      }
      /*if (!Util.canBeShop(b)) {
          Util.debugLog("Can be shop check failed.");
          return;
      }
      Already checked above*/

      if (Util.isWallSign(b.getType())) {
        Util.debugLog("WallSign check failed.");
        return;
      }
      // Finds out where the sign should be placed for the shop
      Block last = null;
      final Location from = p.getLocation().clone();

      from.setY(b.getY());
      from.setPitch(0);
      final BlockIterator bIt = new BlockIterator(from, 0, 7);

      while (bIt.hasNext()) {
        final Block n = bIt.next();

        if (n.equals(b)) {
          break;
        }

        last = n;
      }
      // Send creation menu.
      final Info info = new Info(b.getLocation(), ShopAction.CREATE, e.getItem(), last);

      plugin.getShopManager().getActions().put(p.getUniqueId(), info);
      p.sendMessage(
          MsgUtil.getMessage(
              "how-much-to-trade-for",
              p,
              Util.getItemStackName(Objects.requireNonNull(e.getItem()))));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onInventoryClose(InventoryCloseEvent e) {

    @Nullable Inventory inventory = e.getInventory(); // Possibly wrong tag
    @Nullable Location location = null;

    try {
      // This will cause NPE when the internal getLocation method
      // itself NPE, which should be a server issue.
      location = inventory.getLocation();
    } catch (NullPointerException npe) {
      return; // ignored as workaround, GH-303
    }

    if (location == null) return;

    final Shop shop = plugin.getShopManager().getShopIncludeAttached(location);

    if (shop != null) shop.setSignText();
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onJoin(PlayerJoinEvent e) {

    // Notify the player any messages they were sent
    if (plugin.getConfig().getBoolean("shop.auto-fetch-shop-messages")) {
      MsgUtil.flush(e.getPlayer());
    }
  }

  /*
   * Waits for a player to move too far from a shop, then cancels the menu.
   */
  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onMove(PlayerMoveEvent e) {

    final Info info = plugin.getShopManager().getActions().get(e.getPlayer().getUniqueId());

    if (info == null) {
      return;
    }

    final Player p = e.getPlayer();
    final Location loc1 = info.getLocation();
    final Location loc2 = p.getLocation();

    if (loc1.getWorld() != loc2.getWorld() || loc1.distanceSquared(loc2) > 25) {
      if (info.getAction() == ShopAction.CREATE) {
        p.sendMessage(MsgUtil.getMessage("shop-creation-cancelled", p));
        Util.debugLog(p.getName() + " too far with the shop location.");
      } else if (info.getAction() == ShopAction.BUY) {
        p.sendMessage(MsgUtil.getMessage("shop-purchase-cancelled", p));
        Util.debugLog(p.getName() + " too far with the shop location.");
      }
      plugin.getShopManager().getActions().remove(p.getUniqueId());
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerQuit(PlayerQuitEvent e) {

    // Remove them from the menu
    plugin.getShopManager().getActions().remove(e.getPlayer().getUniqueId());
  }

  @EventHandler(ignoreCancelled = true)
  public void onTeleport(PlayerTeleportEvent e) {

    PlayerMoveEvent me = new PlayerMoveEvent(e.getPlayer(), e.getFrom(), e.getTo());
    onMove(me);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onDyeing(PlayerInteractEvent e) {
    if (e.getAction() != Action.RIGHT_CLICK_BLOCK
        || e.getItem() == null
        || !Util.isDyes(e.getItem().getType())) {
      return;
    }

    final Block block = e.getClickedBlock();

    if (block == null || !Util.isWallSign(block.getType())) {
      return;
    }

    final Block attachedBlock = Util.getAttached(block);

    if (attachedBlock == null
        || plugin.getShopManager().getShopIncludeAttached(attachedBlock.getLocation()) == null) {
      return;
    }

    e.setCancelled(true);
    Util.debugLog("Disallow " + e.getPlayer().getName() + " dye the shop sign.");
  }
}
