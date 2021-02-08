/*
 * This file is a part of project QuickShop, the name is ShopManager.java
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

package org.maxgamer.quickshop.shop;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.economy.Economy;
import org.maxgamer.quickshop.economy.EconomyTransaction;
import org.maxgamer.quickshop.economy.Trader;
import org.maxgamer.quickshop.event.*;
import org.maxgamer.quickshop.util.CalculateUtil;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.PriceLimiter;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.holder.Result;

/**
 * Manage a lot of shops.
 */
public class ShopManager {

  private final Map<String, Map<ShopChunk, Map<Location, Shop>>> shops =
      Maps.newConcurrentMap();

  private final Set<Shop> loadedShops = Sets.newConcurrentHashSet();

  private final Map<UUID, Info> actions = Maps.newConcurrentMap();

  private final QuickShop plugin;
  private final Trader cacheTaxAccount;
  @Getter private final PriceLimiter priceLimiter;
  private final boolean useFastShopSearchAlgorithm;
  private final boolean useOldCanBuildAlgorithm;
  private final boolean autoSign;
  private final Cache<UUID, Shop> shopRuntimeUUIDCaching =
      CacheBuilder.newBuilder()
          .expireAfterAccess(120, TimeUnit.SECONDS)
          .maximumSize(50)
          .weakValues()
          .initialCapacity(5)
          .build();

  public ShopManager(@NotNull QuickShop plugin) {
    Util.ensureThread(false);
    this.plugin = plugin;
    this.useFastShopSearchAlgorithm = plugin.getConfig().getBoolean(
        "shop.use-fast-shop-search-algorithm", false);
    Util.debugLog("Loading caching tax account...");
    String taxAccount = plugin.getConfig().getString("tax-account", "tax");
    if (!(taxAccount == null || taxAccount.isEmpty())) {
      this.cacheTaxAccount =
          new Trader(taxAccount, Bukkit.getOfflinePlayer(taxAccount));
    } else {
      // disable tax account
      cacheTaxAccount = null;
    }
    this.priceLimiter =
        new PriceLimiter(plugin.getConfig().getDouble("shop.minimum-price"),
                         plugin.getConfig().getInt("shop.maximum-price"),
                         plugin.getConfig().getBoolean("shop.allow-free-shop"));
    this.useOldCanBuildAlgorithm =
        plugin.getConfig().getBoolean("limits.old-algorithm");
    this.autoSign = plugin.getConfig().getBoolean("shop.auto-sign");
  }

  /**
   * Checks other plugins to make sure they can use the chest they're making a
   * shop.
   *
   * @param p  The player to check
   * @param b  The block to check
   * @param bf The blockface to check
   * @return True if they're allowed to place a shop there.
   */
  public boolean canBuildShop(@NotNull Player p, @NotNull Block b,
                              @NotNull BlockFace bf) {
    Util.ensureThread(false);
    if (plugin.isLimit()) {
      int owned = 0;
      if (useOldCanBuildAlgorithm) {
        owned = getPlayerAllShops(p.getUniqueId()).size();
      } else {
        for (final Shop shop : getPlayerAllShops(p.getUniqueId())) {
          if (!shop.isUnlimited()) {
            owned++;
          }
        }
      }
      int max = plugin.getShopLimit(p);
      if (owned + 1 > max) {
        MsgUtil.sendMessage(p, MsgUtil.getMessage("reached-maximum-can-create",
                                                  p, String.valueOf(owned),
                                                  String.valueOf(max)));
        return false;
      }
    }
    ShopPreCreateEvent spce = new ShopPreCreateEvent(p, b.getLocation());
    return !Util.fireCancellableEvent(spce);
  }

  /**
   * Returns a map of World - Chunk - Shop
   *
   * @return a map of World - Chunk - Shop
   */
  public @NotNull Map<String, Map<ShopChunk, Map<Location, Shop>>> getShops() {
    return this.shops;
  }

  /**
   * Returns a new shop iterator object, allowing iteration over shops easily,
   * instead of sorting through a 3D map.
   *
   * @return a new shop iterator object.
   */
  public @NotNull Iterator<Shop> getShopIterator() {
    return new ShopIterator();
  }

  /**
   * Removes all shops from memory and the world. Does not delete them from the
   * database. Call this on plugin disable ONLY.
   */
  public void clear() {
    Util.ensureThread(false);
    if (plugin.isDisplay()) {
      for (World world : Bukkit.getWorlds()) {
        for (Chunk chunk : world.getLoadedChunks()) {
          Map<Location, Shop> inChunk = this.getShops(chunk);
          if (inChunk == null || inChunk.isEmpty()) {
            continue;
          }
          for (Shop shop : inChunk.values()) {
            if (shop.isLoaded()) {
              shop.onUnload();
            }
          }
        }
      }
    }
    this.actions.clear();
    this.shops.clear();
  }

  /**
   * Returns a map of Shops
   *
   * @param c The chunk to search. Referencing doesn't matter, only coordinates
   *     and world are used.
   * @return Shops
   */
  public @Nullable Map<Location, Shop> getShops(@NotNull Chunk c) {
    // long start = System.nanoTime();
    return getShops(c.getWorld().getName(), c.getX(), c.getZ());
    // long end = System.nanoTime();
    // plugin.getLogger().log(Level.WARNING, "Chunk lookup in " + ((end -
    // start)/1000000.0) + "ms.");
  }

  public @Nullable Map<Location, Shop> getShops(@NotNull String world,
                                                int chunkX, int chunkZ) {
    final Map<ShopChunk, Map<Location, Shop>> inWorld = this.getShops(world);
    if (inWorld == null) {
      return null;
    }
    return inWorld.get(new ShopChunk(world, chunkX, chunkZ));
  }

  /**
   * Returns a map of Chunk - Shop
   *
   * @param world The name of the world (case sensitive) to get the list of
   *     shops from
   * @return a map of Chunk - Shop
   */
  public @Nullable Map<ShopChunk, Map<Location, Shop>>
  getShops(@NotNull String world) {
    return this.shops.get(world);
  }

  /**
   * Create a shop use Shop and Info object.
   *
   * @param shop The shop object
   * @param info The info object
   */
  public void createShop(@NotNull Shop shop, @NotNull Info info) {
    Util.ensureThread(false);
    Player player = Bukkit.getPlayer(shop.getOwner());
    if (player == null) {
      throw new IllegalStateException(
          "The owner creating the shop is offline or not exist");
    }
    if (info.getSignBlock() != null && autoSign) {
      if (Util.isAir(info.getSignBlock().getType()) ||
          info.getSignBlock().getType() == Material.WATER) {
        info.getSignBlock().setType(Util.getSignMaterial());
        BlockState bs = info.getSignBlock().getState();
        if (info.getSignBlock().getType() == Material.WATER &&
            (bs.getBlockData() instanceof Waterlogged)) {
          Waterlogged waterable = (Waterlogged)bs.getBlockData();
          waterable.setWaterlogged(
              true); // Looks like sign directly put in water
        }
        if (bs.getBlockData() instanceof WallSign) {
          WallSign signBlockDataType = (WallSign)bs.getBlockData();
          BlockFace bf =
              info.getLocation().getBlock().getFace(info.getSignBlock());
          if (bf != null) {
            signBlockDataType.setFacing(bf);
            bs.setBlockData(signBlockDataType);
          }
        } else {
          plugin.getLogger().warning(
              "Sign material " + bs.getType().name() +
              " not a WallSign, make sure you using correct sign material.");
        }
        bs.update(true);
      } else {
        if (!plugin.getConfig().getBoolean(
                "shop.allow-shop-without-space-for-sign")) {
          MsgUtil.sendMessage(player,
                              MsgUtil.getMessage("failed-to-put-sign", player));
          Util.debugLog(
              "Sign cannot placed cause no enough space(Not air block)");
          return;
        }
      }
    }
    // load the shop finally
    shop.onLoad();
    // first init
    shop.setSignText();
    // sync add to prevent compete issue
    addShop(shop.getLocation().getWorld().getName(), shop);
    // save to database
    plugin.getDatabaseHelper().createShop(
        shop, null, e -> Bukkit.getScheduler().runTask(plugin, () -> {
          // also remove from memory when failed
          shop.delete(true);
          plugin.getLogger().warning(
              "Shop create failed, trying to auto fix the database...");
          boolean backupSuccess = Util.backupDatabase();
          if (backupSuccess) {
            plugin.getDatabaseHelper().removeShop(shop);
          } else {
            plugin.getLogger().warning(
                "Failed to backup the database, all changes will revert after a reboot.");
          }
        }));
  }

  /**
   * Format the price use economy system
   *
   * @param d price
   * @return formated price
   */
  public @Nullable String format(double d, @NotNull World world,
                                 @Nullable String currency) {
    return plugin.getEconomy().format(d, world, currency);
  }

  /**
   * Gets a shop in a specific location
   *
   * @param loc The location to get the shop from
   * @return The shop at that location
   */
  public @Nullable Shop getShop(@NotNull Location loc) {
    return getShop(loc, false);
  }

  /**
   * Gets a shop in a specific location
   *
   * @param loc                  The location to get the shop from
   * @param skipShopableChecking whether to check is shopable
   * @return The shop at that location
   */
  public @Nullable Shop getShop(@NotNull Location loc,
                                boolean skipShopableChecking) {
    if (!skipShopableChecking && !Util.isShoppables(loc.getBlock().getType())) {
      return null;
    }
    final Map<Location, Shop> inChunk = getShops(loc.getChunk());
    if (inChunk == null) {
      return null;
    }
    loc = loc.clone();
    // Fix double chest XYZ issue
    loc.setX(loc.getBlockX());
    loc.setY(loc.getBlockY());
    loc.setZ(loc.getBlockZ());
    // We can do this because WorldListener updates the world reference so
    // the world in loc is the same as world in inChunk.get(loc)
    return inChunk.get(loc);
  }

  /**
   * Gets a shop in a specific location Include the attached shop, e.g
   * DoubleChest shop.
   *
   * @param loc The location to get the shop from
   * @return The shop at that location
   */
  public @Nullable Shop getShopIncludeAttached(@Nullable Location loc) {
    return getShopIncludeAttached(loc, true);
  }

  /**
   * Gets a shop in a specific location Include the attached shop, e.g
   * DoubleChest shop.
   *
   * @param loc      The location to get the shop from
   * @param useCache whether to use cache
   * @return The shop at that location
   */
  public @Nullable Shop getShopIncludeAttached(@Nullable Location loc,
                                               boolean useCache) {
    if (loc == null) {
      Util.debugLog("Location is null.");
      return null;
    }

    if (this.useFastShopSearchAlgorithm) {
      return getShopIncludeAttached_Fast(loc, false, useCache);
    } else {
      return getShopIncludeAttached_Classic(loc);
    }
  }

  public @Nullable Shop getShopIncludeAttached_Classic(@NotNull Location loc) {
    @Nullable Shop shop;
    // Get location's chunk all shops
    @Nullable Map<Location, Shop> inChunk = getShops(loc.getChunk());
    // Found some shops in this chunk.
    if (inChunk != null) {
      shop = inChunk.get(loc);
      if (shop != null) {
        // Okay, shop was founded.
        return shop;
      }
      // Ooops, not founded that shop in this chunk.
    }
    @Nullable Block secondHalfShop = Util.getSecondHalf(loc.getBlock());
    if (secondHalfShop != null) {
      inChunk = getShops(secondHalfShop.getChunk());
      if (inChunk != null) {
        shop = inChunk.get(secondHalfShop.getLocation());
        if (shop != null) {
          // Okay, shop was founded.
          return shop;
        }
        // Oooops, no any shops matched.
      }
    }

    // only check if is sign
    if (loc.getBlock().getState() instanceof Sign) {
      // If that chunk nothing we founded, we should check it is attached.
      @Nullable Block attachedBlock = Util.getAttached(loc.getBlock());
      // Check is attached on some block.
      if (attachedBlock == null) {
        // Nope
        return null;
      } else {
        // Okay we know it on some blocks.
        // We need set new location and chunk.
        inChunk = getShops(attachedBlock.getChunk());
        // Found some shops in this chunk
        if (inChunk != null) {
          shop = inChunk.get(attachedBlock.getLocation());
          // Okay, shop was founded.
          return shop;
          // Oooops, no any shops matched.
        }
      }
    }
    return null;
  }

  public void bakeShopRuntimeRandomUniqueIdCache(@NotNull Shop shop) {
    shopRuntimeUUIDCaching.put(shop.getRuntimeRandomUniqueId(), shop);
  }

  @Nullable
  public Shop
  getShopFromRuntimeRandomUniqueId(@NotNull UUID runtimeRandomUniqueId) {
    return getShopFromRuntimeRandomUniqueId(runtimeRandomUniqueId, false);
  }

  @Nullable
  public Shop
  getShopFromRuntimeRandomUniqueId(@NotNull UUID runtimeRandomUniqueId,
                                   boolean includeInvalid) {
    Shop shop = shopRuntimeUUIDCaching.getIfPresent(runtimeRandomUniqueId);
    if (shop == null) {
      for (Shop shopWithoutCache : this.getLoadedShops()) {
        if (shopWithoutCache.getRuntimeRandomUniqueId().equals(
                runtimeRandomUniqueId)) {
          return shopWithoutCache;
        }
      }
      return null;
    }
    if (includeInvalid) {
      return shop;
    }
    if (shop.isValid()) {
      return shop;
    }
    return null;
  }

  public void handleChat(@NotNull Player p, @NotNull String msg) {
    handleChat(p, msg, false);
  }

  public void handleChat(@NotNull Player p, @NotNull String msg,
                         boolean bypassProtectionChecks) {
    if (!plugin.getShopManager().getActions().containsKey(p.getUniqueId())) {
      return;
    }
    final String message = ChatColor.stripColor(msg);
    // Use from the main thread, because Bukkit hates life
    Bukkit.getScheduler().runTask(plugin, () -> {
      Map<UUID, Info> actions = getActions();
      // They wanted to do something.
      Info info = actions.remove(p.getUniqueId());
      if (info == null) {
        return; // multithreaded means this can happen
      }
      if (info.getLocation().getWorld() != p.getLocation().getWorld() ||
          info.getLocation().distanceSquared(p.getLocation()) > 25) {
        MsgUtil.sendMessage(p, MsgUtil.getMessage("not-looking-at-shop", p));
        return;
      }
      if (info.getAction() == ShopAction.CREATE) {
        actionCreate(p, info, message, bypassProtectionChecks);
      }
      if (info.getAction() == ShopAction.BUY) {
        actionTrade(p, info, message);
      }
    });
  }

  /**
   * Load shop method for loading shop into mapping, so getShops method will can
   * find it. It also effects a lots of feature, make sure load it after create
   * it.
   *
   * @param world The world the shop is in
   * @param shop  The shop to load
   */
  public void loadShop(@NotNull String world, @NotNull Shop shop) {
    this.addShop(world, shop);
  }

  /**
   * Adds a shop to the world. Does NOT require the chunk or world to be loaded
   * Call shop.onLoad by yourself
   *
   * @param world The name of the world
   * @param shop  The shop to add
   */
  public void addShop(@NotNull String world, @NotNull Shop shop) {
    Map<ShopChunk, Map<Location, Shop>> inWorld =
        this.getShops().computeIfAbsent(
            world, k -> new MapMaker().initialCapacity(3).makeMap());
    // There's no world storage yet. We need to create that map.
    // Put it in the data universe
    // Calculate the chunks coordinates. These are 1,2,3 for each chunk, NOT
    // location rounded to the nearest 16.
    int x = (int)Math.floor((shop.getLocation().getBlockX()) / 16.0);
    int z = (int)Math.floor((shop.getLocation().getBlockZ()) / 16.0);
    // Get the chunk set from the world info
    ShopChunk shopChunk = new ShopChunk(world, x, z);
    Map<Location, Shop> inChunk = inWorld.computeIfAbsent(
        shopChunk, k -> new MapMaker().initialCapacity(1).makeMap());
    // That chunk data hasn't been created yet - Create it!
    // Put it in the world
    // Put the shop in its location in the chunk list.
    inChunk.put(shop.getLocation(), shop);
    // shop.onLoad();
  }

  /**
   * Removes a shop from the world. Does NOT remove it from the database. *
   * REQUIRES * the world to be loaded Call shop.onUnload by your self.
   *
   * @param shop The shop to remove
   */
  public void removeShop(@NotNull Shop shop) {
    // shop.onUnload();
    Location loc = shop.getLocation();
    String world = Objects.requireNonNull(loc.getWorld()).getName();
    Map<ShopChunk, Map<Location, Shop>> inWorld = this.getShops().get(world);
    int x = (int)Math.floor((loc.getBlockX()) / 16.0);
    int z = (int)Math.floor((loc.getBlockZ()) / 16.0);
    ShopChunk shopChunk = new ShopChunk(world, x, z);
    Map<Location, Shop> inChunk = inWorld.get(shopChunk);
    if (inChunk == null) {
      return;
    }
    inChunk.remove(loc);
    // shop.onUnload();
  }

  /**
   * @return Returns the Map. Info contains what their last question etc was.
   */
  public @NotNull Map<UUID, Info> getActions() { return this.actions; }

  /**
   * Get all loaded shops.
   *
   * @return All loaded shops.
   */
  public @NotNull Set<Shop> getLoadedShops() { return this.loadedShops; }

  /**
   * Get a players all shops.
   *
   * <p>Make sure you have caching this, because this need a while to get
   * player's all shops
   *
   * @param playerUUID The player's uuid.
   * @return The list have this player's all shops.
   */
  public @NotNull List<Shop> getPlayerAllShops(@NotNull UUID playerUUID) {
    final List<Shop> playerShops = new ArrayList<>(10);
    for (final Shop shop : getAllShops()) {
      if (shop.getOwner().equals(playerUUID)) {
        playerShops.add(shop);
      }
    }
    return playerShops;
  }

  /**
   * Returns all shops in the whole database, include unloaded.
   *
   * <p>Make sure you have caching this, because this need a while to get all
   * shops
   *
   * @return All shop in the database
   */
  public @NotNull List<Shop> getAllShops() {
    final List<Shop> shops = new ArrayList<>();
    for (final Map<ShopChunk, Map<Location, Shop>> shopMapData :
         getShops().values()) {
      for (final Map<Location, Shop> shopData : shopMapData.values()) {
        shops.addAll(shopData.values());
      }
    }
    return shops;
  }

  /**
   * Get the all shops in the world.
   *
   * @param world The world you want get the shops.
   * @return The list have this world all shops
   */
  public @NotNull List<Shop> getShopsInWorld(@NotNull World world) {
    final List<Shop> worldShops = new ArrayList<>();
    for (final Shop shop : getAllShops()) {
      if (Objects.equals(shop.getLocation().getWorld(), world)) {
        worldShops.add(shop);
      }
    }
    return worldShops;
  }

  public void actionBuy(@NotNull UUID buyer, @NotNull Inventory buyerInventory,
                        @NotNull Economy eco, @NotNull Info info,
                        @NotNull Shop shop, int amount) {
    Util.ensureThread(false);
    if (shopIsNotValid(buyer, info, shop)) {
      return;
    }
    int space = shop.getRemainingSpace();
    if (space == -1) {
      space = 10000;
    }
    if (space < amount) {
      MsgUtil.sendMessage(
          buyer, MsgUtil.getMessage(buyer, "shop-has-no-space",
                                    Integer.toString(space),
                                    Util.getItemStackName(shop.getItem())));
      return;
    }
    int count = Util.countItems(buyerInventory, shop.getItem());
    // Not enough items
    if (amount > count) {
      MsgUtil.sendMessage(
          buyer, MsgUtil.getMessage(buyer, "you-dont-have-that-many-items",
                                    Integer.toString(count),
                                    Util.getItemStackName(shop.getItem())));
      return;
    }
    if (amount < 1) {
      // & Dumber
      MsgUtil.sendMessage(buyer, MsgUtil.getMessage(buyer, "negative-amount"));
      return;
    }

    // Money handling
    // BUYING MODE  Shop Owner -> Player
    double taxModifier = getTax(shop, buyer);
    double total = CalculateUtil.multiply(amount, shop.getPrice());
    ShopPurchaseEvent e =
        new ShopPurchaseEvent(shop, buyer, buyerInventory, amount, total);
    if (Util.fireCancellableEvent(e)) {
      return; // Cancelled
    } else {
      total = e.getTotal(); // Allow addon to set it
    }
    EconomyTransaction transaction;
    if (!shop.isUnlimited() ||
        (plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners") &&
         shop.isUnlimited())) {
      transaction = EconomyTransaction.builder()
                        .core(eco)
                        .amount(total)
                        .from(shop.getOwner())
                        .to(buyer)
                        .taxModifier(taxModifier)
                        .taxAccount(cacheTaxAccount)
                        .currency(shop.getCurrency())
                        .build();
    } else {
      transaction = EconomyTransaction.builder()
                        .core(eco)
                        .amount(total)
                        .from(null)
                        .to(buyer)
                        .taxModifier(taxModifier)
                        .taxAccount(cacheTaxAccount)
                        .currency(shop.getCurrency())
                        .build();
    }
    if (!transaction.failSafeCommit()) {
      if (transaction.getSteps() == EconomyTransaction.TransactionSteps.CHECK) {
        MsgUtil.sendMessage(
            buyer,
            MsgUtil.getMessage(
                buyer, "the-owner-cant-afford-to-buy-from-you",
                Objects.requireNonNull(format(
                    total, shop.getLocation().getWorld(), shop.getCurrency())),
                Objects.requireNonNull(format(
                    eco.getBalance(shop.getOwner(),
                                   shop.getLocation().getWorld(),
                                   shop.getCurrency()),
                    shop.getLocation().getWorld(), shop.getCurrency()))));
      } else {
        MsgUtil.sendMessage(buyer,
                            MsgUtil.getMessage(buyer, "purchase-failed"));
        plugin.getLogger().severe("EconomyTransaction Failed, last error:" +
                                  transaction.getLastError());
        QuickShop.getInstance().log("EconomyTransaction Failed, last error:" +
                                    transaction.getLastError());
      }
      return;
    }

    // Notify the owner of the purchase. //TODO: move to a standalone method
    Player player = Bukkit.getPlayer(buyer);

    String msg = MsgUtil.getMessage(
        buyer, "player-sold-to-your-store",
        player != null ? player.getName() : buyer.toString(),
        String.valueOf(amount),
        "##########" + Util.serialize(shop.getItem()) + "##########");

    if (space == amount) {
      msg += "\n" + MsgUtil.getMessage(
                        buyer, "shop-out-of-space",
                        Integer.toString(shop.getLocation().getBlockX()),
                        Integer.toString(shop.getLocation().getBlockY()),
                        Integer.toString(shop.getLocation().getBlockZ()));
    }
    if (plugin.getConfig().getBoolean("shop.sending-stock-message-to-staffs")) {
      for (UUID staff : shop.getModerator().getStaffs()) {
        MsgUtil.send(shop, staff, msg);
      }
    }
    MsgUtil.send(shop, shop.getOwner(), msg);
    shop.buy(buyer, buyerInventory,
             player != null ? player.getLocation() : shop.getLocation(),
             amount);
    MsgUtil.sendSellSuccess(buyer, shop, amount);
    ShopSuccessPurchaseEvent se = new ShopSuccessPurchaseEvent(
        shop, buyer, buyerInventory, amount, total, taxModifier);
    Bukkit.getPluginManager().callEvent(se);
    shop.setSignText(); // Update the signs count
  }

  @Deprecated
  public void actionBuy(@NotNull Player p, @NotNull Economy eco,
                        @NotNull Info info, @NotNull Shop shop, int amount) {
    Util.ensureThread(false);
    actionBuy(p.getUniqueId(), p.getInventory(), eco, info, shop, amount);
  }

  @Deprecated
  public double getTax(@NotNull Shop shop, @NotNull Player p) {
    return getTax(shop, p.getUniqueId());
  }

  public double getTax(@NotNull Shop shop, @NotNull UUID p) {
    Util.ensureThread(false);
    double tax = plugin.getConfig().getDouble("tax");
    Player player = Bukkit.getPlayer(p);
    if (player != null) {
      if (QuickShop.getPermissionManager().hasPermission(player,
                                                         "quickshop.tax")) {
        tax = 0;
        Util.debugLog("Disable the Tax for player " + player +
                      " cause they have permission quickshop.tax");
      }
      if (shop.isUnlimited() && QuickShop.getPermissionManager().hasPermission(
                                    player, "quickshop.tax.bypassunlimited")) {
        tax = 0;
        Util.debugLog(
            "Disable the Tax for player " + player +
            " cause they have permission quickshop.tax.bypassunlimited and shop is unlimited.");
      }
    }
    if (tax >= 1.0) {
      plugin.getLogger().warning(
          "Disable tax due to is invalid, it should be in 0.0-1.0 (current value is " +
          tax + ")");
      tax = 0;
    }
    if (tax < 0) {
      tax = 0; // Tax was disabled.
    }
    if (shop.getModerator().isModerator(p)) {
      tax = 0; // Is staff or owner, so we won't will take them tax
    }
    ShopTaxEvent taxEvent = new ShopTaxEvent(shop, tax, p);
    taxEvent.callEvent();
    return taxEvent.getTax();
  }

  public void actionCreate(@NotNull Player p, @NotNull Info info,
                           @NotNull String message,
                           boolean bypassProtectionChecks) {
    Util.ensureThread(false);
    if (plugin.getEconomy() == null) {
      MsgUtil.sendMessage(
          p,
          "Error: Economy system not loaded, type /qs main command to get details.");
      return;
    }
    if (plugin.isAllowStack() && !p.hasPermission("quickshop.create.stacks")) {
      Util.debugLog(
          "Player " + p +
          " no permission to create stacks shop, forcing creating single item shop");
      info.getItem().setAmount(1);
    }

    // Checking the shop can be created
    Util.debugLog("Calling for protection check...");
    // Fix openInv compatiable issue
    if (!bypassProtectionChecks) {
      Result result =
          plugin.getPermissionChecker().canBuild(p, info.getLocation());
      if (!result.isSuccess()) {
        MsgUtil.sendMessage(p,
                            MsgUtil.getMessage("3rd-plugin-build-check-failed",
                                               p, result.getMessage()));
        Util.debugLog(
            "Failed to create shop because protection check failed, found:" +
            result.getMessage());
        return;
      }
    }

    if (plugin.getShopManager().getShop(info.getLocation()) != null) {
      MsgUtil.sendMessage(p, MsgUtil.getMessage("shop-already-owned", p));
      return;
    }
    if (Util.getSecondHalf(info.getLocation().getBlock()) != null &&
        !QuickShop.getPermissionManager().hasPermission(
            p, "quickshop.create.double")) {
      MsgUtil.sendMessage(p, MsgUtil.getMessage("no-double-chests", p));
      return;
    }
    if (!Util.canBeShop(info.getLocation().getBlock())) {
      MsgUtil.sendMessage(p, MsgUtil.getMessage("chest-was-removed", p));
      return;
    }
    if (info.getLocation().getBlock().getType() ==
        Material.ENDER_CHEST) { // FIXME: Need a better impl
      if (!QuickShop.getPermissionManager().hasPermission(
              p, "quickshop.create.enderchest")) {
        return;
      }
    }

    if (autoSign) {
      if (info.getSignBlock() == null) {
        if (!plugin.getConfig().getBoolean(
                "shop.allow-shop-without-space-for-sign")) {
          MsgUtil.sendMessage(p, MsgUtil.getMessage("failed-to-put-sign", p));
          return;
        }
      }
      Material signType = info.getSignBlock().getType();
      if (signType != Material.WATER && !Util.isAir(signType) &&
          !plugin.getConfig().getBoolean(
              "shop.allow-shop-without-space-for-sign")) {
        MsgUtil.sendMessage(p, MsgUtil.getMessage("failed-to-put-sign", p));
        return;
      }
    }

    // Price per item
    double price;

    // Price parsing
    if (plugin.getConfig().getBoolean("whole-number-prices-only")) {
      try {
        price = Integer.parseInt(message);
      } catch (NumberFormatException numberFormatException) {
        Util.debugLog(numberFormatException.getMessage());
        MsgUtil.sendMessage(p, MsgUtil.getMessage("not-a-integer", p, message));
        return;
      }
    } else {
      try {
        price = Double.parseDouble(message);
        if (Double.isInfinite(price)) {
          MsgUtil.sendMessage(
              p, MsgUtil.getMessage("exceeded-maximum", p, message));
          return;
        }
        String strFormat = new DecimalFormat("#.#########")
                               .format(Math.abs(price))
                               .replace(",", ".");
        String[] processedDouble = strFormat.split("\\.");
        if (processedDouble.length > 1) {
          int maximumDigitsLimit =
              plugin.getConfig().getInt("maximum-digits-in-price", -1);
          if (processedDouble[1].length() > maximumDigitsLimit &&
              maximumDigitsLimit != -1) {
            MsgUtil.sendMessage(
                p, MsgUtil.getMessage("digits-reach-the-limit", p,
                                      String.valueOf(maximumDigitsLimit)));
            return;
          }
        }
      } catch (NumberFormatException ex) {
        Util.debugLog(ex.getMessage());
        MsgUtil.sendMessage(p, MsgUtil.getMessage("not-a-number", p, message));
        return;
      }
    }

    // Price limit checking
    boolean decFormat = plugin.getConfig().getBoolean("use-decimal-format");
    switch (this.priceLimiter.check(info.getItem(), price)) {
    case REACHED_PRICE_MIN_LIMIT:
      MsgUtil.sendMessage(
          p, MsgUtil.getMessage(
                 "price-too-cheap", p,
                 (decFormat)
                     ? MsgUtil.decimalFormat(this.priceLimiter.getMaxPrice())
                     : Double.toString(this.priceLimiter.getMinPrice())));
      return;
    case REACHED_PRICE_MAX_LIMIT:
      MsgUtil.sendMessage(
          p, MsgUtil.getMessage(
                 "price-too-high", p,
                 (decFormat)
                     ? MsgUtil.decimalFormat(this.priceLimiter.getMaxPrice())
                     : Double.toString(this.priceLimiter.getMinPrice())));
      return;
    case PRICE_RESTRICTED:
      Map.Entry<Double, Double> materialLimit =
          Util.getPriceRestriction(info.getItem().getType());
      MsgUtil.sendMessage(
          p, MsgUtil.getMessage("restricted-prices", p,
                                Util.getItemStackName(info.getItem()),
                                String.valueOf(materialLimit.getKey()),
                                String.valueOf(materialLimit.getValue())));
      return;
    }

    // Set to 1 when disabled stacking shop
    if (!plugin.isAllowStack()) {
      info.getItem().setAmount(1);
    }

    // Create the sample shop
    ContainerShop shop =
        new ContainerShop(plugin, info.getLocation(), price, info.getItem(),
                          new ShopModerator(p.getUniqueId()), false,
                          ShopType.SELLING, new ConcurrentHashMap<>());
    if (!bypassProtectionChecks) {
      Result result = plugin.getIntegrationHelper().callIntegrationsCanCreate(
          p, info.getLocation());
      if (!result.isSuccess()) {
        MsgUtil.sendMessage(
            p, MsgUtil.getMessage("integrations-check-failed-create", p,
                                  result.getMessage()));
        Util.debugLog("Cancelled by integrations: " + result);
        return;
      }
    }

    // Calling ShopCreateEvent
    ShopCreateEvent shopCreateEvent =
        new ShopCreateEvent(shop, p.getUniqueId());
    if (Util.fireCancellableEvent(shopCreateEvent)) {
      Util.debugLog("Cancelled by plugin");
      return;
    }
    // Handle create cost
    // This must be called after the event has been called.
    // Else, if the event is cancelled, they won't get their
    // money back.
    double createCost = plugin.getConfig().getDouble("shop.cost");
    if (QuickShop.getPermissionManager().hasPermission(
            p, "quickshop.bypasscreatefee")) {
      createCost = 0;
    }
    if (createCost > 0) {
      EconomyTransaction economyTransaction =
          EconomyTransaction.builder()
              .taxAccount(cacheTaxAccount)
              .taxModifier(0.0)
              .core(plugin.getEconomy())
              .from(p.getUniqueId())
              .to(null)
              .amount(createCost)
              .currency(plugin.getCurrency())
              .build();
      if (!economyTransaction.failSafeCommit()) {
        if (economyTransaction.getSteps() ==
            EconomyTransaction.TransactionSteps.CHECK) {
          MsgUtil.sendMessage(
              p,
              MsgUtil.getMessage("you-cant-afford-a-new-shop", p,
                                 Objects.requireNonNull(format(
                                     createCost, shop.getLocation().getWorld(),
                                     shop.getCurrency()))));
        } else {
          MsgUtil.sendMessage(p, MsgUtil.getMessage("purchase-failed", p));
          plugin.getLogger().severe("EconomyTransaction Failed, last error:" +
                                    economyTransaction.getLastError());
          plugin.log("EconomyTransaction Failed, last error:" +
                     economyTransaction.getLastError());
        }
        return;
      }
    }

    // The shop about successfully created
    createShop(shop, info);
    if (!plugin.getConfig().getBoolean("shop.lock")) {
      MsgUtil.sendMessage(p, MsgUtil.getMessage("shops-arent-locked", p));
    }

    // Figures out which way we should put the sign on and
    // sets its text.
    if (shop.isDoubleShop()) {
      Shop nextTo = shop.getAttachedShop();
      if (Objects.requireNonNull(nextTo).getPrice() > shop.getPrice()) {
        // The one next to it must always be a
        // buying shop.
        MsgUtil.sendMessage(p,
                            MsgUtil.getMessage("buying-more-than-selling", p));
      }
    }
  }

  @Deprecated
  public void actionSell(@NotNull Player p, @NotNull Economy eco,
                         @NotNull Info info, @NotNull Shop shop, int amount) {
    Util.ensureThread(false);
    actionSell(p.getUniqueId(), p.getInventory(), eco, info, shop, amount);
  }

  public void actionSell(@NotNull UUID seller,
                         @NotNull Inventory sellerInventory,
                         @NotNull Economy eco, @NotNull Info info,
                         @NotNull Shop shop, int amount) {
    Util.ensureThread(false);
    if (shopIsNotValid(seller, info, shop)) {
      return;
    }
    int stock = shop.getRemainingStock();
    if (stock == -1) {
      stock = 10000;
    }
    if (stock < amount) {
      MsgUtil.sendMessage(
          seller, MsgUtil.getMessage(seller, "shop-stock-too-low",
                                     Integer.toString(stock),
                                     Util.getItemStackName(shop.getItem())));
      return;
    }
    if (amount < 1) {
      // & Dumber
      MsgUtil.sendMessage(seller,
                          MsgUtil.getMessage(seller, "negative-amount"));
      return;
    }
    int pSpace = Util.countSpace(sellerInventory, shop.getItem());
    if (amount > pSpace) {
      MsgUtil.sendMessage(seller, MsgUtil.getMessage(seller, "not-enough-space",
                                                     String.valueOf(pSpace)));
      return;
    }

    double taxModifier = getTax(shop, seller);
    double total = CalculateUtil.multiply(amount, shop.getPrice());

    ShopPurchaseEvent e =
        new ShopPurchaseEvent(shop, seller, sellerInventory, amount, total);
    if (Util.fireCancellableEvent(e)) {
      return; // Cancelled
    } else {
      total = e.getTotal(); // Allow addon to set it
    }
    // Money handling
    // SELLING Player -> Shop Owner
    EconomyTransaction transaction;
    if (!shop.isUnlimited() ||
        (plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners") &&
         shop.isUnlimited())) {
      transaction = EconomyTransaction.builder()
                        .allowLoan(plugin.getConfig().getBoolean(
                            "shop.allow-economy-loan", false))
                        .core(eco)
                        .from(seller)
                        .to(shop.getOwner())
                        .amount(total)
                        .taxModifier(taxModifier)
                        .taxAccount(cacheTaxAccount)
                        .currency(shop.getCurrency())
                        .build();
    } else {
      transaction = EconomyTransaction.builder()
                        .allowLoan(plugin.getConfig().getBoolean(
                            "shop.allow-economy-loan", false))
                        .core(eco)
                        .from(seller)
                        .to(null)
                        .amount(total)
                        .taxModifier(taxModifier)
                        .taxAccount(cacheTaxAccount)
                        .world(shop.getLocation().getWorld())
                        .currency(shop.getCurrency())
                        .build();
    }
    if (!transaction.failSafeCommit()) {
      if (transaction.getSteps() == EconomyTransaction.TransactionSteps.CHECK) {
        MsgUtil.sendMessage(
            seller,
            MsgUtil.getMessage(
                seller, "you-cant-afford-to-buy",
                Objects.requireNonNull(format(
                    total, shop.getLocation().getWorld(), shop.getCurrency())),
                Objects.requireNonNull(format(
                    eco.getBalance(seller, shop.getLocation().getWorld(),
                                   shop.getCurrency()),
                    shop.getLocation().getWorld(), shop.getCurrency()))));
      } else {
        MsgUtil.sendMessage(seller,
                            MsgUtil.getMessage(seller, "purchase-failed"));
        plugin.getLogger().severe("EconomyTransaction Failed, last error:" +
                                  transaction.getLastError());
        QuickShop.getInstance().log("EconomyTransaction Failed, last error:" +
                                    transaction.getLastError());
      }
      return;
    }

    String msg;
    // Notify the shop owner //TODO: move to a standalone method
    Player player = Bukkit.getPlayer(seller);
    if (plugin.getConfig().getBoolean("show-tax")) {
      msg = MsgUtil.getMessage(
          seller, "player-bought-from-your-store-tax",
          player != null ? player.getName() : seller.toString(),
          Integer.toString(amount * shop.getItem().getAmount()),
          "##########" + Util.serialize(shop.getItem()) + "##########",
          Double.toString(total),
          Util.format(CalculateUtil.multiply(taxModifier, total), shop));
    } else {
      msg = MsgUtil.getMessage(
          seller, "player-bought-from-your-store",
          player != null ? player.getName() : seller.toString(),
          Integer.toString(amount * shop.getItem().getAmount()),
          "##########" + Util.serialize(shop.getItem()) + "##########",
          Double.toString(total));
    }
    // Transfers the item from A to B
    if (stock == amount) {
      msg += "\n" + MsgUtil.getMessage(
                        seller, "shop-out-of-stock",
                        Integer.toString(shop.getLocation().getBlockX()),
                        Integer.toString(shop.getLocation().getBlockY()),
                        Integer.toString(shop.getLocation().getBlockZ()),
                        Util.getItemStackName(shop.getItem()));
    }

    MsgUtil.send(shop, shop.getOwner(), msg);
    if (plugin.getConfig().getBoolean("shop.sending-stock-message-to-staffs")) {
      for (UUID staff : shop.getModerator().getStaffs()) {
        MsgUtil.send(shop, staff, msg);
      }
    }
    shop.sell(seller, sellerInventory,
              player != null ? player.getLocation() : shop.getLocation(),
              amount);
    MsgUtil.sendPurchaseSuccess(seller, shop, amount);
    ShopSuccessPurchaseEvent se = new ShopSuccessPurchaseEvent(
        shop, seller, sellerInventory, amount, total, taxModifier);
    Bukkit.getPluginManager().callEvent(se);
  }

  public boolean shopIsNotValid(@NotNull UUID uuid, @NotNull Info info,
                                @NotNull Shop shop) {
    Player player = Bukkit.getPlayer(uuid);
    return shopIsNotValid(player, info, shop);
  }

  private boolean shopIsNotValid(@Nullable Player p, @NotNull Info info,
                                 @NotNull Shop shop) {
    if (plugin.getEconomy() == null) {
      MsgUtil.sendMessage(
          p,
          "Error: Economy system not loaded, type /qs main command to get details.");
      return true;
    }
    if (!Util.canBeShop(info.getLocation().getBlock())) {
      MsgUtil.sendMessage(p, MsgUtil.getMessage("chest-was-removed", p));
      return true;
    }
    if (info.hasChanged(shop)) {
      MsgUtil.sendMessage(p, MsgUtil.getMessage("shop-has-changed", p));
      return true;
    }
    return false;
  }

  private void actionTrade(@NotNull Player p, @NotNull Info info,
                           @NotNull String message) {
    Util.ensureThread(false);
    if (plugin.getEconomy() == null) {
      MsgUtil.sendMessage(
          p,
          "Error: Economy system not loaded, type /qs main command to get details.");
      return;
    }
    Result result = plugin.getIntegrationHelper().callIntegrationsCanTrade(
        p, info.getLocation());
    if (!result.isSuccess()) {
      MsgUtil.sendMessage(p,
                          MsgUtil.getMessage("integrations-check-failed-trade",
                                             p, result.getMessage()));
      Util.debugLog("Cancel by integrations.");
      return;
    }
    Economy eco = plugin.getEconomy();

    // Get the shop they interacted with
    Shop shop = plugin.getShopManager().getShop(info.getLocation());
    // It's not valid anymore
    if (shop == null || !Util.canBeShop(info.getLocation().getBlock())) {
      MsgUtil.sendMessage(p, MsgUtil.getMessage("chest-was-removed", p));
      return;
    }
    if (p.getGameMode() == GameMode.CREATIVE &&
        plugin.getConfig().getBoolean("shop.disable-creative-mode-trading")) {
      MsgUtil.sendMessage(
          p, MsgUtil.getMessage("trading-in-creative-mode-is-disabled", p));
      return;
    }
    int amount;
    if (info.hasChanged(shop)) {
      MsgUtil.sendMessage(p, MsgUtil.getMessage("shop-has-changed", p));
      return;
    }
    if (shop.isBuying()) {
      try {
        amount = Integer.parseInt(message);
      } catch (NumberFormatException e) {
        if (message.equalsIgnoreCase(plugin.getConfig().getString(
                "shop.word-for-trade-all-items", "all"))) {
          int shopHaveSpaces = Util.countSpace(
              ((ContainerShop)shop).getInventory(), shop.getItem());
          int invHaveItems = Util.countItems(p.getInventory(), shop.getItem());
          // Check if shop owner has enough money
          double ownerBalance =
              eco.getBalance(shop.getOwner(), shop.getLocation().getWorld(),
                             shop.getCurrency());
          int ownerCanAfford;

          if (shop.getPrice() != 0) {
            ownerCanAfford = (int)(ownerBalance / shop.getPrice());
          } else {
            ownerCanAfford = Integer.MAX_VALUE;
          }

          if (!shop.isUnlimited()) {
            amount = Math.min(shopHaveSpaces, invHaveItems);
            amount = Math.min(amount, ownerCanAfford);
          } else {
            amount = Util.countItems(p.getInventory(), shop.getItem());
            // even if the shop is unlimited, the config option
            // pay-unlimited-shop-owners is set to true, the unlimited shop
            // owner should have enough money.
            if (plugin.getConfig().getBoolean(
                    "shop.pay-unlimited-shop-owners")) {
              amount = Math.min(amount, ownerCanAfford);
            }
          }
          if (amount < 1) { // typed 'all' but the auto set amount is 0
            if (shopHaveSpaces == 0) {
              // when typed 'all' but the shop doesn't have any empty space
              MsgUtil.sendMessage(
                  p, MsgUtil.getMessage("shop-has-no-space", p,
                                        Integer.toString(shopHaveSpaces),
                                        Util.getItemStackName(shop.getItem())));
              return;
            }
            if (ownerCanAfford == 0 &&
                (!shop.isUnlimited() ||
                 plugin.getConfig().getBoolean(
                     "shop.pay-unlimited-shop-owners"))) {
              // when typed 'all' but the shop owner doesn't have enough money
              // to buy at least 1 item (and shop isn't unlimited or
              // pay-unlimited is true)
              MsgUtil.sendMessage(
                  p, MsgUtil.getMessage(
                         "the-owner-cant-afford-to-buy-from-you", p,
                         Objects.requireNonNull(format(
                             shop.getPrice(), shop.getLocation().getWorld(),
                             shop.getCurrency())),
                         Objects.requireNonNull(
                             format(ownerBalance, shop.getLocation().getWorld(),
                                    shop.getCurrency()))));
              return;
            }
            // when typed 'all' but player doesn't have any items to sell
            MsgUtil.sendMessage(
                p, MsgUtil.getMessage("you-dont-have-that-many-items", p,
                                      Integer.toString(amount),
                                      Util.getItemStackName(shop.getItem())));
            return;
          }
        } else {
          // instead of output cancelled message (when typed neither integer or
          // 'all'), just let player know that there should be positive number
          // or 'all'
          MsgUtil.sendMessage(p,
                              MsgUtil.getMessage("not-a-integer", p, message));
          Util.debugLog("Receive the chat " + message +
                        " and it format failed: " + e.getMessage());
          return;
        }
      }
      actionBuy(p.getUniqueId(), p.getInventory(), eco, info, shop, amount);
    } else if (shop.isSelling()) {
      try {
        amount = Integer.parseInt(message);
      } catch (NumberFormatException e) {
        if (message.equalsIgnoreCase(plugin.getConfig().getString(
                "shop.word-for-trade-all-items", "all"))) {
          int shopHaveItems = Util.countItems(
              ((ContainerShop)shop).getInventory(), shop.getItem());
          int invHaveSpaces = Util.countSpace(p.getInventory(), shop.getItem());
          if (!shop.isUnlimited()) {
            amount = Math.min(shopHaveItems, invHaveSpaces);
          } else {
            // should check not having items but having empty slots, cause
            // player is trying to buy items from the shop.
            amount = Util.countSpace(p.getInventory(), shop.getItem());
          }
          // typed 'all', check if player has enough money than price * amount
          double price = shop.getPrice();
          double balance =
              eco.getBalance(p.getUniqueId(), shop.getLocation().getWorld(),
                             shop.getCurrency());
          amount = Math.min(amount, (int)Math.floor(balance / price));
          if (amount < 1) { // typed 'all' but the auto set amount is 0
            // when typed 'all' but player can't buy any items
            if (!shop.isUnlimited() && shopHaveItems < 1) {
              // but also the shop's stock is 0
              MsgUtil.sendMessage(
                  p,
                  MsgUtil.getMessage("shop-stock-too-low", p,
                                     Integer.toString(shop.getRemainingStock()),
                                     Util.getItemStackName(shop.getItem())));
            } else {
              // when if player's inventory is full
              if (invHaveSpaces <= 0) {
                MsgUtil.sendMessage(
                    p, MsgUtil.getMessage("not-enough-space", p,
                                          String.valueOf(invHaveSpaces)));
                return;
              }
              MsgUtil.sendMessage(
                  p, MsgUtil.getMessage(
                         "you-cant-afford-to-buy", p,
                         Objects.requireNonNull(
                             format(price, shop.getLocation().getWorld(),
                                    shop.getCurrency())),
                         Objects.requireNonNull(
                             format(balance, shop.getLocation().getWorld(),
                                    shop.getCurrency()))));
            }
            return;
          }
        } else {
          // instead of output cancelled message, just let player know that
          // there should be positive number or 'all'
          MsgUtil.sendMessage(p,
                              MsgUtil.getMessage("not-a-integer", p, message));
          Util.debugLog("Receive the chat " + message +
                        " and it format failed: " + e.getMessage());
          return;
        }
      }
      actionSell(p.getUniqueId(), p.getInventory(), eco, info, shop, amount);
    } else {
      MsgUtil.sendMessage(p, MsgUtil.getMessage("shop-purchase-cancelled", p));
      plugin.getLogger().warning("Shop data broken? Loc:" + shop.getLocation());
    }
  }

  private @Nullable Shop getShopIncludeAttached_Fast(@NotNull Location loc,
                                                     boolean fromAttach,
                                                     boolean useCache) {
    Shop shop = getShop(loc);

    // failed, get attached shop
    if (shop == null) {
      final Block currentBlock = loc.getBlock();
      if (!fromAttach) {
        // sign
        if (Util.isWallSign(currentBlock.getType())) {
          final Block attached = Util.getAttached(currentBlock);
          if (attached != null) {
            shop = this.getShopIncludeAttached_Fast(attached.getLocation(),
                                                    true, useCache);
          }
          // double chest
        } else {
          @Nullable final Block half = Util.getSecondHalf(currentBlock);
          if (half != null) {
            shop = getShop(half.getLocation());
          }
        }
      }
    }
    // add cache if using
    if (plugin.getShopCache() != null && useCache) {
      plugin.getShopCache().setCache(loc, shop);
    }

    return shop;
  }

  public class ShopIterator implements Iterator<Shop> {

    private final Iterator<Map<ShopChunk, Map<Location, Shop>>> worlds;

    private Iterator<Map<Location, Shop>> chunks;

    private Iterator<Shop> shops;

    public ShopIterator() { worlds = getShops().values().iterator(); }

    /**
     * Returns true if there is still more shops to iterate over.
     */
    @Override
    public boolean hasNext() {
      if (shops == null || !shops.hasNext()) {
        if (chunks == null || !chunks.hasNext()) {
          if (!worlds.hasNext()) {
            return false;
          } else {
            chunks = worlds.next().values().iterator();
            return hasNext();
          }
        } else {
          shops = chunks.next().values().iterator();
          return hasNext();
        }
      }
      return true;
    }

    /**
     * Fetches the next shop. Throws NoSuchElementException if there are no more
     * shops.
     */
    @Override
    public @NotNull Shop next() {
      if (shops == null || !shops.hasNext()) {
        if (chunks == null || !chunks.hasNext()) {
          if (!worlds.hasNext()) {
            throw new NoSuchElementException("No more shops to iterate over!");
          }
          chunks = worlds.next().values().iterator();
        }
        shops = chunks.next().values().iterator();
      }
      if (!shops.hasNext()) {
        return this.next(); // Skip to the next one (Empty iterator?)
      }
      return shops.next();
    }
  }
}
