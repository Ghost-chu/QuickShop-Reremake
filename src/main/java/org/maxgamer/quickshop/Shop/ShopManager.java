package org.maxgamer.quickshop.Shop;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Economy.Economy;
import org.maxgamer.quickshop.Event.ShopCreateEvent;
import org.maxgamer.quickshop.Event.ShopPreCreateEvent;
import org.maxgamer.quickshop.Event.ShopPurchaseEvent;
import org.maxgamer.quickshop.Event.ShopSuccessPurchaseEvent;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Manage a lot of shops.
 */
@SuppressWarnings("WeakerAccess")
public class ShopManager {
    private HashMap<UUID, Info> actions = new HashMap<>();
    private QuickShop plugin;
    private HashMap<String, HashMap<ShopChunk, HashMap<Location, Shop>>> shops = new HashMap<>();
    private Set<Shop> loadedShops = new CopyOnWriteArraySet<>();

    public ShopManager(@NotNull QuickShop plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    private void actionBuy(@NotNull Player p, @NotNull Economy eco, @NotNull HashMap<UUID, Info> actions2, @NotNull Info info, @NotNull String message, @NotNull Shop shop, int amount) {
        if (plugin.getEconomy() == null) {
            p.sendMessage("Error: Economy system not loaded, type /qs main command to get details.");
            return;
        }
        if (!Util.canBeShop(info.getLocation().getBlock())) {
            p.sendMessage(MsgUtil.getMessage("chest-was-removed", p));
            return;
        }
        if (info.hasChanged(shop)) {
            p.sendMessage(MsgUtil.getMessage("shop-has-changed", p));
            return;
        }
        int space = shop.getRemainingSpace();
        if (space == -1) {
            space = 10000;
        }
        if (space < amount) {
            p.sendMessage(MsgUtil
                    .getMessage("shop-has-no-space", p, "" + space, Util.getItemStackName(shop.getItem())));
            return;
        }
        int count = Util.countItems(p.getInventory(), shop.getItem());
        // Not enough items
        if (amount > count) {
            p.sendMessage(MsgUtil.getMessage("you-dont-have-that-many-items", p, "" + count, Util.getItemStackName(shop.getItem())));
            return;
        }
        if (amount < 1) {
            // & Dumber
            p.sendMessage(MsgUtil.getMessage("negative-amount", p));
            return;
        }
        ShopPurchaseEvent e = new ShopPurchaseEvent(shop, p, amount);
        Bukkit.getPluginManager().callEvent(e);
        if (e.isCancelled()) {
            return; // Cancelled
        }
        // Money handling
        double tax = plugin.getConfig().getDouble("tax");
        double total = amount * shop.getPrice();
        if (p.hasPermission("quickshop.tax")) {
            tax = 0;
            Util.debugLog("Disable the Tax for player " + p.getName() + " cause they have permission quickshop.tax");
        }
        if (tax < 0) {
            tax = 0; //Tax was disabled.
        }
        if (shop.getModerator().isModerator(p.getUniqueId())) {
            tax = 0; //Is staff or owner, so we won't will take them tax
        }

        boolean shouldPayOwner = !shop.isUnlimited() || (plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners") && shop
                .isUnlimited());
        if (shouldPayOwner) {
            boolean successA = eco.withdraw(shop.getOwner(), total); //Withdraw owner's money
            if (!successA) {
                p.sendMessage(MsgUtil
                        .getMessage("the-owner-cant-afford-to-buy-from-you", p, format(total), format(eco
                                .getBalance(shop.getOwner()))));
                return;
            }
        }
        double depositMoney = total * (1 - tax);
        boolean successB = eco.deposit(p.getUniqueId(), depositMoney);//Deposit player's money
        if (!successB) {
            plugin.getLogger().warning("Failed to deposit the money " + depositMoney + " to player " + e.getPlayer().getName());
            /* Rollback the trade */
            if (shouldPayOwner) {
                if (!eco.deposit(shop.getOwner(), total)) {
                    plugin.getLogger().warning("Failed to rollback the purchase actions for player " + Bukkit
                            .getOfflinePlayer(shop.getOwner()).getName());
                }
            }
            p.sendMessage(MsgUtil.getMessage("purchase-failed", p));
            return;
        }
        //Purchase successfully
        if (tax != 0) {
            String taxAccount = plugin.getConfig().getString("tax-account");
            if (taxAccount != null) {
                eco.deposit(Bukkit.getOfflinePlayer(taxAccount).getUniqueId(), total * tax);
            }
        }
        // Notify the owner of the purchase.
        String msg = MsgUtil.getMessage("player-sold-to-your-store", p, p.getName(), String.valueOf(amount), "##########" + Util.serialize(shop.getItem()) + "##########");

        if (space == amount) {
            msg += "\n" + MsgUtil.getMessage("shop-out-of-space", p, "" + shop.getLocation().getBlockX(), "" + shop.getLocation()
                    .getBlockY(), "" + shop.getLocation().getBlockZ());
        }
        MsgUtil.send(shop.getOwner(), msg, shop.isUnlimited());
        shop.buy(p, amount);
        MsgUtil.sendSellSuccess(p, shop, amount);
        ShopSuccessPurchaseEvent se = new ShopSuccessPurchaseEvent(shop, p, amount, total, tax);
        Bukkit.getPluginManager().callEvent(se);
        shop.setSignText(); // Update the signs count
    }

    @SuppressWarnings("deprecation")
    private void actionCreate(@NotNull Player p, @NotNull HashMap<UUID, Info> actions2, @NotNull Info info, @NotNull String message, boolean bypassProtectionChecks) {
        if (plugin.getEconomy() == null) {
            p.sendMessage("Error: Economy system not loaded, type /qs main command to get details.");
            return;
        }
        Util.debugLog("actionCreate");
        try {
            // Checking the shop can be created
            Util.debugLog("Calling for protection check...");
            //Fix openInv compatiable issue

            if (!bypassProtectionChecks) {
                plugin.getCompatibilityTool().toggleProtectionListeners(false, p);
                if (!plugin.getPermissionChecker().canBuild(p, info.getLocation())) {
                    p.sendMessage(MsgUtil.getMessage("no-permission", p) + ": Some 3rd party plugin denied the permission checks, did you have permission built in there?");
                    Util.debugLog("Failed to create shop: Protection check failed:");
                    for (RegisteredListener belisteners : BlockBreakEvent.getHandlerList().getRegisteredListeners()) {
                        Util.debugLog(belisteners.getPlugin().getName());
                    }
                    return;
                }
                plugin.getCompatibilityTool().toggleProtectionListeners(true, p);
            }

            if (plugin.getShopManager().getShop(info.getLocation()) != null) {
                p.sendMessage(MsgUtil.getMessage("shop-already-owned", p));
                return;
            }
            if (Util.getSecondHalf(info.getLocation().getBlock()) != null
                    && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.double")) {
                p.sendMessage(MsgUtil.getMessage("no-double-chests", p));
                return;
            }
            if (!Util.canBeShop(info.getLocation().getBlock())) {
                p.sendMessage(MsgUtil.getMessage("chest-was-removed", p));
                return;
            }
            if (info.getLocation().getBlock().getType() == Material.ENDER_CHEST) {
                if (!QuickShop.getPermissionManager().hasPermission(p, "quickshop.create.enderchest")) {
                    return;
                }
            }

            // allow-shop-without-space-for-sign check
            if (plugin.getConfig().getBoolean("shop.auto-sign")
                    && !plugin.getConfig().getBoolean("allow-shop-without-space-for-sign")) {
                if (info.getSignBlock() == null) {
                    p.sendMessage(MsgUtil.getMessage("failed-to-put-sign", p));
                    return;
                }
                Material signType = info.getSignBlock().getType();
                if (!Util.isAir(signType) && signType != Material.WATER) {
                    p.sendMessage(MsgUtil.getMessage("failed-to-put-sign", p));
                    return;
                }
            }
            // Price per item
            double price;
            double minPrice = plugin.getConfig().getDouble("shop.minimum-price");
            try {
                if (plugin.getConfig().getBoolean("whole-number-prices-only")) {
                    try {
                        price = Integer.parseInt(message);
                    } catch (NumberFormatException ex2) {
                        // input is number, but not Integer
                        Util.debugLog(ex2.getMessage());
                        p.sendMessage(MsgUtil.getMessage("not-a-integer", p, message));
                        return;
                    }
                } else {
                    price = Double.parseDouble(message);
                }

            } catch (NumberFormatException ex) {
                //No number input
                Util.debugLog(ex.getMessage());
                p.sendMessage(MsgUtil.getMessage("not-a-number", p, message));
                return;
            }

            boolean decFormat = plugin.getConfig().getBoolean("use-deciaml-format");
            if (plugin.getConfig().getBoolean("shop.allow-free-shop")) {
                if (price != 0 && price < minPrice) {
                    p.sendMessage(MsgUtil.getMessage("price-too-cheap", p, (decFormat) ? MsgUtil.decimalFormat(minPrice) : "" + minPrice));
                    return;
                }
            } else {
                if (price < minPrice) {
                    p.sendMessage(MsgUtil.getMessage("price-too-cheap", p, (decFormat) ? MsgUtil.decimalFormat(minPrice) : "" + minPrice));
                    return;
                }
            }

            double price_limit = plugin.getConfig().getInt("shop.maximum-price");
            if (price_limit != -1) {
                if (price > price_limit) {
                    p.sendMessage(MsgUtil.getMessage("price-too-high", p, (decFormat) ? MsgUtil.decimalFormat(price_limit) : "" + price_limit));
                    return;
                }
            }
            // Check price restriction
            Entry<Double, Double> priceRestriction = Util.getPriceRestriction(info.getItem().getType());
            if (priceRestriction != null) {
                if (price < priceRestriction.getKey() || price > priceRestriction.getValue()) {
                    // p.sendMessage(ChatColor.RED+"Restricted prices for
                    // "+info.getItem().getType()+": min "+priceRestriction.getKey()+", max
                    // "+priceRestriction.getValue());
                    p.sendMessage(MsgUtil.getMessage("restricted-prices", p, Util.getItemStackName(info.getItem()),
                            String.valueOf(priceRestriction.getKey()), String.valueOf(priceRestriction.getValue())));
                }
            }

            double createCost = plugin.getConfig().getDouble("shop.cost");
            // Create the sample shop.
            ContainerShop shop = new ContainerShop(info.getLocation(), price, info.getItem(), new ShopModerator(p
                    .getUniqueId()), false, ShopType.SELLING);

            // This must be called after the event has been called.
            // Else, if the event is cancelled, they won't get their
            // money back.
            if (createCost > 0) {
                if (!plugin.getEconomy().withdraw(p.getUniqueId(), createCost)) {
                    p.sendMessage(MsgUtil.getMessage("you-cant-afford-a-new-shop", p, format(createCost)));
                    return;
                }
                try {
                    String taxAccount = plugin.getConfig().getString("tax-account");
                    if (taxAccount != null) {
                        plugin.getEconomy().deposit(
                                Bukkit.getOfflinePlayer(taxAccount).getUniqueId(), createCost);
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                    plugin.getLogger().log(Level.WARNING,
                            "QuickShop can't pay tax to account in config.yml, Please set tax account name to a existing player!");
                }
            }
            shop.onLoad();
            ShopCreateEvent e = new ShopCreateEvent(shop, p);
            Bukkit.getPluginManager().callEvent(e);
            if (e.isCancelled()) {
                shop.onUnload();
                return;
            }
            /* The shop has hereforth been successfully created */
            createShop(shop, info);
            Location loc = shop.getLocation();
            if (!plugin.getConfig().getBoolean("shop.lock")) {
                // Warn them if they haven't been warned since
                // reboot
                if (!plugin.getWarnings().contains(p.getName())) {
                    p.sendMessage(MsgUtil.getMessage("shops-arent-locked", p));
                    plugin.getWarnings().add(p.getName());
                }
            }
            // Figures out which way we should put the sign on and
            // sets its text.

            if (shop.isDoubleShop()) {
                Shop nextTo = shop.getAttachedShop();
                if (nextTo.getPrice() > shop.getPrice()) {
                    // The one next to it must always be a
                    // buying shop.
                    p.sendMessage(MsgUtil.getMessage("buying-more-than-selling", p));
                }
            }
        } catch (NumberFormatException ex) {
            //No number input
            Util.debugLog(ex.getMessage());
            p.sendMessage(MsgUtil.getMessage("not-a-number", p, message));
        }
    }

    private void actionSell(@NotNull Player p, @NotNull Economy eco, @NotNull HashMap<UUID, Info> actions2, @NotNull Info info, @NotNull String message, @NotNull Shop shop, int amount) {
        if (plugin.getEconomy() == null) {
            p.sendMessage("Error: Economy system not loaded, type /qs main command to get details.");
            return;
        }
        if (!Util.canBeShop(info.getLocation().getBlock())) {
            p.sendMessage(MsgUtil.getMessage("chest-was-removed", p));
            return;
        }
        if (info.hasChanged(shop)) {
            p.sendMessage(MsgUtil.getMessage("shop-has-changed", p));
            return;
        }
        int stock = shop.getRemainingStock();
        if (stock == -1) {
            stock = 10000;
        }
        if (stock < amount) {
            p.sendMessage(MsgUtil
                    .getMessage("shop-stock-too-low", p, "" + stock, Util.getItemStackName(shop.getItem())));
            return;
        }
        if (amount < 1) {
            // & Dumber
            p.sendMessage(MsgUtil.getMessage("negative-amount", p));
            return;
        }
        int pSpace = Util.countSpace(p.getInventory(), shop.getItem());
        if (amount > pSpace) {
            p.sendMessage(MsgUtil.getMessage("not-enough-space", p, String.valueOf(pSpace)));
            return;
        }
        ShopPurchaseEvent e = new ShopPurchaseEvent(shop, p, amount);
        Bukkit.getPluginManager().callEvent(e);
        if (e.isCancelled()) {
            return; // Cancelled
        }
        // Money handling
        double tax = plugin.getConfig().getDouble("tax");
        double total = amount * shop.getPrice();
        if (p.hasPermission("quickshop.tax")) {
            tax = 0;
            Util.debugLog("Disable the Tax for player " + p.getName() + " cause they have permission quickshop.tax");
        }
        if (tax < 0) {
            tax = 0; //Tax was disabled.
        }
        if (shop.getModerator().isModerator(p.getUniqueId())) {
            tax = 0; //Is staff or owner, so we won't will take them tax
        }

        boolean successA = eco.withdraw(p.getUniqueId(), total); //Withdraw owner's money
        if (!successA) {
            p.sendMessage(MsgUtil
                    .getMessage("you-cant-afford-to-buy", p, format(total), format(eco.getBalance(p.getUniqueId()))));
            return;
        }
        boolean shouldPayOwner = !shop.isUnlimited() || (plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners") && shop
                .isUnlimited());
        if (shouldPayOwner) {
            double depositMoney = total * (1 - tax);
            boolean successB = eco.deposit(shop.getOwner(), depositMoney);
            if (!successB) {
                plugin.getLogger().warning("Failed to deposit the money for player " + Bukkit.getOfflinePlayer(shop.getOwner()));
                /* Rollback the trade */
                if (!eco.deposit(p.getUniqueId(), depositMoney)) {
                    plugin.getLogger().warning("Failed to rollback the purchase actions for player " + Bukkit
                            .getOfflinePlayer(shop.getOwner()).getName());
                }
                p.sendMessage(MsgUtil.getMessage("purchase-failed", p));
                return;
            }
        }

        String msg;
        // Notify the shop owner
        if (plugin.getConfig().getBoolean("show-tax")) {
            msg = MsgUtil.getMessage("player-bought-from-your-store-tax", p, p.getName(), "" + amount, "##########" + Util.serialize(shop.getItem()) + "##########", Util.format((tax * total)));
        } else {
            msg = MsgUtil.getMessage("player-bought-from-your-store", p, p.getName(), "" + amount, "##########" + Util.serialize(shop.getItem()) + "##########");
        }
        // Transfers the item from A to B
        if (stock == amount) {
            msg += "\n" + MsgUtil.getMessage("shop-out-of-stock", p, "" + shop.getLocation().getBlockX(), "" + shop
                    .getLocation().getBlockY(), "" + shop.getLocation().getBlockZ(), Util
                    .getItemStackName(shop.getItem()));
        }

        MsgUtil.send(shop.getOwner(), msg, shop.isUnlimited());
        shop.sell(p, amount);
        MsgUtil.sendPurchaseSuccess(p, shop, amount);
        ShopSuccessPurchaseEvent se = new ShopSuccessPurchaseEvent(shop, p, amount, total, tax);
        Bukkit.getPluginManager().callEvent(se);
    }

    private void actionTrade(@NotNull Player p, @NotNull HashMap<UUID, Info> actions, @NotNull Info info, @NotNull String message) {
        if (plugin.getEconomy() == null) {
            p.sendMessage("Error: Economy system not loaded, type /qs main command to get details.");
            return;
        }
        Economy eco = plugin.getEconomy();

        // Get the shop they interacted with
        Shop shop = plugin.getShopManager().getShop(info.getLocation());
        // It's not valid anymore
        if (shop == null || !Util.canBeShop(info.getLocation().getBlock())) {
            p.sendMessage(MsgUtil.getMessage("chest-was-removed", p));
            return;
        }
        int amount;
        if (info.hasChanged(shop)) {
            p.sendMessage(MsgUtil.getMessage("shop-has-changed", p));
            return;
        }
        if (shop.isBuying()) {
            try {
                amount = Integer.parseInt(message);
            } catch (NumberFormatException e) {
                if (message.equalsIgnoreCase(plugin.getConfig().getString("shop.word-for-trade-all-items", "all"))) {
                    int shopHaveSpaces = Util.countSpace(((ContainerShop) shop).getInventory(), shop.getItem());
                    int invHaveItems = Util.countItems(p.getInventory(), shop.getItem());
                    // Check if shop owner has enough money
                    double ownerBalance = eco.getBalance(shop.getOwner());
                    int ownerCanAfford;

                    if(ownerBalance != 0) {
                        if(shop.getPrice() != 0){
                            ownerCanAfford = (int) (ownerBalance / shop.getPrice());
                        }else{
                            ownerCanAfford = Integer.MAX_VALUE;
                        }
                    }else {
                        ownerCanAfford = 0;
                    }

                    if (!shop.isUnlimited()) {
                        amount = Math.min(shopHaveSpaces, invHaveItems);
                        amount = Math.min(amount, ownerCanAfford);
                    } else {
                        amount = Util.countItems(p.getInventory(), shop.getItem());
                        // even if the shop is unlimited, the config option pay-unlimited-shop-owners is set to true,
                        // the unlimited shop owner should have enough money.
                        if (plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners")) {
                            amount = Math.min(amount, (int) ownerCanAfford);
                        }
                    }
                    if (amount < 1) { // typed 'all' but the auto set amount is 0
                        if (shopHaveSpaces == 0) {
                            // when typed 'all' but the shop doesn't have any empty space
                            p.sendMessage(MsgUtil
                                    .getMessage("shop-has-no-space", p, "" + shopHaveSpaces, Util.getItemStackName(shop.getItem())));
                            return;
                        }
                        if (ownerCanAfford == 0 && (!shop.isUnlimited() || plugin.getConfig().getBoolean("shop.pay-unlimited-shop-owners"))) {
                            // when typed 'all' but the shop owner doesn't have enough money to buy at least 1 item (and shop isn't unlimited or pay-unlimited is true)
                            p.sendMessage(MsgUtil
                                    .getMessage("the-owner-cant-afford-to-buy-from-you", p, format(shop.getPrice()), format(ownerBalance)));
                            return;
                        }
                        // when typed 'all' but player doesn't have any items to sell
                        p.sendMessage(MsgUtil.getMessage("you-dont-have-that-many-items", p, "" + amount, Util.getItemStackName(shop.getItem())));
                        return;
                    }
                } else {
                    // instead of output cancelled message (when typed neither integer or 'all'), just let player know that there should be positive number or 'all'
                    p.sendMessage(MsgUtil.getMessage("not-a-integer", p, message));
                    Util.debugLog("Receive the chat " + message + " and it format failed: " + e.getMessage());
                    return;
                }
            }
            actionBuy(p, eco, actions, info, message, shop, amount);
        } else if (shop.isSelling()) {
            try {
                amount = Integer.parseInt(message);
            } catch (NumberFormatException e) {
                int shopHaveItems = Util.countItems(((ContainerShop) shop).getInventory(), shop.getItem());
                int invHaveSpaces = Util.countSpace(p.getInventory(), shop.getItem());
                if (message.equalsIgnoreCase(plugin.getConfig().getString("shop.word-for-trade-all-items", "all"))) {
                    if (!shop.isUnlimited()) {
                        amount = Math.min(shopHaveItems, invHaveSpaces);
                    } else {
                        // should check not having items but having empty slots, cause player is trying to buy items from the shop.
                        amount = Util.countSpace(p.getInventory(), shop.getItem());
                    }
                    // typed 'all', check if player has enough money than price * amount
                    double price = shop.getPrice();
                    double balance = eco.getBalance(p.getUniqueId());
                    amount = Math.min(amount, (int) Math.floor(balance / price));
                    if (amount < 1) { // typed 'all' but the auto set amount is 0
                        // when typed 'all' but player can't buy any items
                        if (!shop.isUnlimited() && shopHaveItems < 1) {
                            // but also the shop's stock is 0
                            p.sendMessage(MsgUtil.getMessage("shop-stock-too-low", p, "" + shop.getRemainingStock(), Util.getItemStackName(shop.getItem())));
                            return;
                        } else {
                            // when if player's inventory is full
                            if (invHaveSpaces == 0) {
                                p.sendMessage(MsgUtil.getMessage("not-enough-space", p, String.valueOf(invHaveSpaces)));
                                return;
                            }
                            p.sendMessage(MsgUtil.getMessage("you-cant-afford-to-buy", p, format(price), format(balance)));
                            return;
                        }
                    }
                } else {
                    // instead of output cancelled message, just let player know that there should be positive number or 'all'
                    p.sendMessage(MsgUtil.getMessage("not-a-integer", p, message));
                    Util.debugLog("Receive the chat " + message + " and it format failed: " + e.getMessage());
                    return;
                }
            }
            actionSell(p, eco, actions, info, message, shop, amount);
        } else {
            p.sendMessage(MsgUtil.getMessage("shop-purchase-cancelled", p));
            plugin.getLogger().warning("Shop data broken? Loc:" + shop.getLocation().toString());
        }
        ;
    }

    /**
     * Adds a shop to the world. Does NOT require the chunk or world to be loaded
     * Call shop.onLoad by yourself
     *
     * @param world The name of the world
     * @param shop  The shop to add
     */
    public void addShop(@NotNull String world, @NotNull Shop shop) {
        HashMap<ShopChunk, HashMap<Location, Shop>> inWorld = this.getShops()
                .computeIfAbsent(world, k -> new HashMap<>(3));
        // There's no world storage yet. We need to create that hashmap.
        // Put it in the data universe
        // Calculate the chunks coordinates. These are 1,2,3 for each chunk, NOT
        // location rounded to the nearest 16.
        int x = (int) Math.floor((shop.getLocation().getBlockX()) / 16.0);
        int z = (int) Math.floor((shop.getLocation().getBlockZ()) / 16.0);
        // Get the chunk set from the world info
        ShopChunk shopChunk = new ShopChunk(world, x, z);
        HashMap<Location, Shop> inChunk = inWorld.computeIfAbsent(shopChunk, k -> new HashMap<>(1));
        // That chunk data hasn't been created yet - Create it!
        // Put it in the world
        // Put the shop in its location in the chunk list.
        inChunk.put(shop.getLocation(), shop);
        // shop.onLoad();

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
    public boolean canBuildShop(@NotNull Player p, @NotNull Block b, @NotNull BlockFace bf) {
        RegisteredListener openInvRegisteredListener = null; // added for compatibility reasons with OpenInv - see https://github.com/KaiKikuchi/QuickShop/issues/139
        // try {
        // 	if (plugin.openInvPlugin != null) {
        // 		for (RegisteredListener listener : PlayerInteractEvent.getHandlerList().getRegisteredListeners()) {
        // 			if (listener.getPlugin().getName().equals(plugin.openInvPlugin.getName())) {
        // 				openInvRegisteredListener = listener;
        // 				PlayerInteractEvent.getHandlerList().unregister(listener);
        // 				break;
        // 			}
        // 		}
        // 	}
        try {
            plugin.getCompatibilityTool().toggleProtectionListeners(false, p);

            if (plugin.isLimit()) {
                int owned = 0;
                if (!plugin.getConfig().getBoolean("limits.old-algorithm")) {
                    for (HashMap<ShopChunk, HashMap<Location, Shop>> shopmap : getShops().values()) {
                        for (HashMap<Location, Shop> shopLocs : shopmap.values()) {
                            for (Shop shop : shopLocs.values()) {
                                if (shop.getOwner().equals(p.getUniqueId()) && !shop.isUnlimited()) {
                                    owned++;
                                }
                            }
                        }
                    }
                } else {
                    Iterator<Shop> it = getShopIterator();
                    while (it.hasNext()) {
                        if (it.next().getOwner().equals(p.getUniqueId())) {
                            owned++;
                        }
                    }
                }

                int max = plugin.getShopLimit(p);
                if (owned + 1 > max) {
                    //p.sendMessage(ChatColor.RED + "You have already created a maximum of " + owned + "/" + max + " shops!");
                    p.sendMessage(MsgUtil.getMessage("reached-maximum-can-create", p, String.valueOf(owned), String.valueOf(max)));
                    return false;
                }
            }
            if (!plugin.getPermissionChecker().canBuild(p, b)) {
                Util.debugLog("PermissionChecker canceled shop creation");
                return false;
            }
            ShopPreCreateEvent spce = new ShopPreCreateEvent(p, b.getLocation());
            Bukkit.getPluginManager().callEvent(spce);
            if (spce.isCancelled()) {
                return false;
            }
        } finally {
            // if (plugin.openInvPlugin != null && openInvRegisteredListener != null) {
            // 	PlayerInteractEvent.getHandlerList().register(openInvRegisteredListener);
            // }
            plugin.getCompatibilityTool().toggleProtectionListeners(true, p);
        }

        return true;
    }

    /**
     * Removes all shops from memory and the world. Does not delete them from
     * the database. Call this on plugin disable ONLY.
     */
    public void clear() {
        if (plugin.isDisplay()) {
            for (World world : Bukkit.getWorlds()) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    HashMap<Location, Shop> inChunk = this.getShops(chunk);
                    if (inChunk == null || inChunk.isEmpty()) {
                        continue;
                    }
                    for (Shop shop : inChunk.values()) {
                        shop.onUnload();
                    }
                }
            }
        }
        this.actions.clear();
        this.shops.clear();
    }

    /**
     * Create a shop use Shop and Info object.
     *
     * @param shop The shop object
     * @param info The info object
     */
    public void createShop(@NotNull Shop shop, @NotNull Info info) {
        Player player = Bukkit.getPlayer(shop.getOwner());
        if (player == null) {
            throw new IllegalStateException("The owner creating the shop is offline or not exist");
        }
        ShopCreateEvent ssShopCreateEvent = new ShopCreateEvent(shop, player);
        Bukkit.getPluginManager().callEvent(ssShopCreateEvent);
        if (ssShopCreateEvent.isCancelled()) {
            return;
        }
        Location loc = shop.getLocation();
        try {
            // Write it to the database
            plugin.getDatabaseHelper().createShop(ShopModerator.serialize(shop.getModerator()), shop
                    .getPrice(), shop.getItem(), (shop.isUnlimited() ?
                    1 :
                    0), shop.getShopType().toID(), Objects.requireNonNull(loc.getWorld()).getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            // Add it to the world
            addShop(loc.getWorld().getName(), shop);
        } catch (SQLException error) {
            plugin.getLogger().warning("SQLException detected, trying to auto fix the database...");
            boolean backupSuccess = Util.backupDatabase();
            try {
                if (backupSuccess) {
                    plugin.getDatabaseHelper().removeShop(loc.getBlockX(), loc.getBlockY(), loc
                            .getBlockZ(), loc.getWorld().getName());
                } else {
                    plugin.getLogger().warning("Failed to backup the database, all changes will revert after a reboot.");
                }
            } catch (SQLException error2) {
                //Failed removing
                plugin.getLogger().warning("Failed to autofix the database, all changes will revert after a reboot.");
                error2.printStackTrace();
            }
            error.printStackTrace();
        }
        //Create sign
        if (info.getSignBlock() != null && plugin.getConfig().getBoolean("shop.auto-sign")) {
            if (!Util.isAir(info.getSignBlock().getType())) {
                Util.debugLog("Sign cannot placed cause no enough space(Not air block)");
                return;
            }
            boolean isWaterLogged = false;
            if (info.getSignBlock().getType() == Material.WATER) {
                isWaterLogged = true;
            }

            info.getSignBlock().setType(Util.getSignMaterial());
            BlockState bs = info.getSignBlock().getState();
            if (isWaterLogged) {
                if (bs.getBlockData() instanceof Waterlogged) {
                    Waterlogged waterable = (Waterlogged) bs.getBlockData();
                    waterable.setWaterlogged(true); // Looks like sign directly put in water
                }
            }
            if (bs.getBlockData() instanceof WallSign) {
                org.bukkit.block.data.type.WallSign signBlockDataType = (org.bukkit.block.data.type.WallSign) bs.getBlockData();
                BlockFace bf = info.getLocation().getBlock().getFace(info.getSignBlock());
                if (bf != null) {
                    signBlockDataType.setFacing(bf);
                    bs.setBlockData(signBlockDataType);
                }
            } else {
                plugin.getLogger().warning("Sign material " + bs.getType()
                        .name() + " not a WallSign, make sure you using correct sign material.");
            }
            bs.update(true);
            shop.setSignText();
        }
    }

    /**
     * Format the price use economy system
     *
     * @param d price
     * @return formated price
     */
    public @Nullable String format(double d) {
        return plugin.getEconomy().format(d);
    }

    /**
     * Gets a shop in a specific location
     *
     * @param loc The location to get the shop from
     * @return The shop at that location
     */
    public @Nullable Shop getShop(@NotNull Location loc) {
        HashMap<Location, Shop> inChunk = getShops(loc.getChunk());
        if (inChunk == null) {
            return null;
        }
        loc = loc.clone();
        //Fix double chest XYZ issue
        loc.setX(loc.getBlockX());
        loc.setY(loc.getBlockY());
        loc.setZ(loc.getBlockZ());
        // We can do this because WorldListener updates the world reference so
        // the world in loc is the same as world in inChunk.get(loc)
        return inChunk.get(loc);
    }

    /**
     * Gets a shop in a specific location
     * Include the attached shop, e.g DoubleChest shop.
     *
     * @param loc The location to get the shop from
     * @return The shop at that location
     */
    public @Nullable Shop getShopIncludeAttached(@Nullable Location loc) {
        if (loc == null) {
            Util.debugLog("Location is null.");
            return null;
        }
        Util.debugLog("Start searching the shops...");
        @Nullable Shop shop;
        //Get location's chunk all shops
        @Nullable HashMap<Location, Shop> inChunk = getShops(loc.getChunk());
        //Found some shops in this chunk.
        if (inChunk != null) {
            shop = inChunk.get(loc);
            if (shop != null) {
                //Okay, shop was founded.
                return shop;
            }
            //Ooops, not founded that shop in this chunk.
        }
        @Nullable Block secondHalfShop = Util.getSecondHalf(loc.getBlock());
        if (secondHalfShop != null) {
            inChunk = getShops(secondHalfShop.getChunk());
            if (inChunk != null) {
                shop = inChunk.get(secondHalfShop.getLocation());
                if (shop != null) {
                    //Okay, shop was founded.
                    return shop;
                }
                //Oooops, no any shops matched.
            }
        }
        //If that chunk nothing we founded, we should check it is attached.
        @Nullable Block attachedBlock = Util.getAttached(loc.getBlock());
        //Check is attached on some block.
        if (attachedBlock == null) {
            //Nope
            Util.debugLog("No attached block.");
            return null;
        } else {
            //Okay we know it on some blocks.
            //We need set new location and chunk.
            inChunk = getShops(attachedBlock.getChunk());
            //Found some shops in this chunk
            if (inChunk != null) {
                shop = inChunk.get(attachedBlock.getLocation());
                if (shop != null) {
                    //Okay, shop was founded.
                    return shop;
                }
                //Oooops, no any shops matched.
            }
        }

        Util.debugLog("Not found shops use the attached util.");

        return null;
    }

    /**
     * Returns a hashmap of Chunk - Shop
     *
     * @param world The name of the world (case sensitive) to get the list of
     *              shops from
     * @return a hashmap of Chunk - Shop
     */
    public @Nullable HashMap<ShopChunk, HashMap<Location, Shop>> getShops(@NotNull String world) {
        return this.shops.get(world);
    }

    /**
     * Returns a hashmap of Shops
     *
     * @param c The chunk to search. Referencing doesn't matter, only
     *          coordinates and world are used.
     * @return Shops
     */
    public @Nullable HashMap<Location, Shop> getShops(@NotNull Chunk c) {
        // long start = System.nanoTime();
        return getShops(c.getWorld().getName(), c.getX(), c.getZ());
        // long end = System.nanoTime();
        // plugin.getLogger().log(Level.WARNING, "Chunk lookup in " + ((end - start)/1000000.0) +
        // "ms.");
    }

    public @Nullable HashMap<Location, Shop> getShops(String world, int chunkX, int chunkZ) {
        HashMap<ShopChunk, HashMap<Location, Shop>> inWorld = this.getShops(world);
        if (inWorld == null) {
            return null;
        }
        ShopChunk shopChunk = new ShopChunk(world, chunkX, chunkZ);
        return inWorld.get(shopChunk);
    }

    public void handleChat(@NotNull Player p, @NotNull String msg) {
        handleChat(p, msg, false);
    }

    public void handleChat(@NotNull Player p, @NotNull String msg, boolean bypassProtectionChecks) {
        final String message = ChatColor.stripColor(msg);
        // Use from the main thread, because Bukkit hates life
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            HashMap<UUID, Info> actions = getActions();
            // They wanted to do something.
            Info info = actions.remove(p.getUniqueId());
            if (info == null) {
                return; // multithreaded means this can happen
            }

            if (info.getLocation().getWorld() != p.getLocation().getWorld() || info.getLocation().distanceSquared(p.getLocation()) > 25) {
                p.sendMessage(MsgUtil.getMessage("not-looking-at-shop", p));
                return;
            }
            if (info.getAction() == ShopAction.CREATE) {
                actionCreate(p, actions, info, message, bypassProtectionChecks);
            }
            if (info.getAction() == ShopAction.BUY) {
                actionTrade(p, actions, info, message);
            }
        });
    }

    /**
     * Loads the given shop into storage. This method is used for loading data
     * from the database. Do not use this method to create a shop.
     *
     * @param world The world the shop is in
     * @param shop  The shop to load
     */
    public void loadShop(@NotNull String world, @NotNull Shop shop) {
        this.addShop(world, shop);
    }

    /**
     * Removes a shop from the world. Does NOT remove it from the database. *
     * REQUIRES * the world to be loaded
     * Call shop.onUnload by your self.
     *
     * @param shop The shop to remove
     */
    public void removeShop(@NotNull Shop shop) {
        // shop.onUnload();
        Location loc = shop.getLocation();
        String world = Objects.requireNonNull(loc.getWorld()).getName();
        HashMap<ShopChunk, HashMap<Location, Shop>> inWorld = this.getShops().get(world);
        int x = (int) Math.floor((shop.getLocation().getBlockX()) / 16.0);
        int z = (int) Math.floor((shop.getLocation().getBlockZ()) / 16.0);
        ShopChunk shopChunk = new ShopChunk(world, x, z);
        HashMap<Location, Shop> inChunk = inWorld.get(shopChunk);
        if (inChunk == null) {
            return;
        }
        inChunk.remove(loc);
        // shop.onUnload();
    }

    /**
     * @return Returns the HashMap. Info contains what
     * their last question etc was.
     */
    public HashMap<UUID, Info> getActions() {
        return this.actions;
    }

    /**
     * Returns a new shop iterator object, allowing iteration over shops easily,
     * instead of sorting through a 3D hashmap.
     *
     * @return a new shop iterator object.
     */
    public Iterator<Shop> getShopIterator() {
        return new ShopIterator();
    }

    /**
     * Returns all shops in the whole database, include unloaded.
     * <p>
     * Make sure you have caching this, because this need a while to get all shops
     *
     * @return All shop in the database
     */
    public Collection<Shop> getAllShops() {
        //noinspection unchecked
        HashMap<String, HashMap<ShopChunk, HashMap<Location, Shop>>> worldsMap = (HashMap<String, HashMap<ShopChunk, HashMap<Location, Shop>>>) getShops()
                .clone();
        Collection<Shop> shops = new ArrayList<>();
        for (HashMap<ShopChunk, HashMap<Location, Shop>> shopMapData : worldsMap.values()) {
            for (HashMap<Location, Shop> shopData : shopMapData.values()) {
                shops.addAll(shopData.values());
            }
        }
        return shops;
    }

    /**
     * Returns a hashmap of World - Chunk - Shop
     *
     * @return a hashmap of World - Chunk - Shop
     */
    public @Nullable HashMap<String, HashMap<ShopChunk, HashMap<Location, Shop>>> getShops() {
        return this.shops;
    }

    public class ShopIterator implements Iterator<Shop> {
        private Iterator<HashMap<Location, Shop>> chunks;
        private Iterator<Shop> shops;
        private Iterator<HashMap<ShopChunk, HashMap<Location, Shop>>> worlds;

        public ShopIterator() {
            //noinspection unchecked
            HashMap<String, HashMap<ShopChunk, HashMap<Location, Shop>>> worldsMap = (HashMap<String, HashMap<ShopChunk, HashMap<Location, Shop>>>) getShops()
                    .clone();
            worlds = worldsMap.values().iterator();

        }

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
         * Fetches the next shop. Throws NoSuchElementException if there are no
         * more shops.
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

    /**
     * Get all loaded shops.
     *
     * @return All loaded shops.
     */
    public @Nullable Set<Shop> getLoadedShops() {
        return this.loadedShops;
    }

    /**
     * Get a players all shops.
     *
     * @param playerUUID The player's uuid.
     * @return The list have this player's all shops.
     */
    public @NotNull List<Shop> getPlayerAllShops(@NotNull UUID playerUUID) {
        return getAllShops().stream().filter(shop -> shop.getOwner().equals(playerUUID)).collect(Collectors.toList());
    }

    /**
     * Get the all shops in the world.
     *
     * @param world The world you want get the shops.
     * @return The list have this world all shops
     */
    public @NotNull List<Shop> getShopsInWorld(@NotNull World world) {
        return getAllShops().stream().filter(shop -> Objects.equals(shop.getLocation().getWorld(), world)).collect(Collectors.toList());
    }
}
