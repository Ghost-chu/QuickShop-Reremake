/*
 * This file is a part of project QuickShop, the name is GriefPreventionIntegration.java
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

package org.maxgamer.quickshop.integration.griefprevention;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import me.ryanhamshire.GriefPrevention.events.ClaimExpirationEvent;
import me.ryanhamshire.GriefPrevention.events.ClaimModifiedEvent;
import me.ryanhamshire.GriefPrevention.events.TrustChangedEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.integration.IntegrateStage;
import org.maxgamer.quickshop.integration.IntegrationStage;
import org.maxgamer.quickshop.integration.QSIntegratedPlugin;
import org.maxgamer.quickshop.shop.Shop;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@IntegrationStage(loadStage = IntegrateStage.onEnableAfter)
public class GriefPreventionIntegration extends QSIntegratedPlugin {

    final GriefPrevention griefPrevention = GriefPrevention.instance;

    private final boolean whiteList;

    private final boolean deleteOnClaimUntrusted;

    private final boolean deleteOnClaimUnclaimed;

    private final boolean deleteOnClaimExpired;

    private final boolean deleteOnClaimResized;

    private final List<Flag> createLimits = new ArrayList<>(3);

    private final List<Flag> tradeLimits = new ArrayList<>(3);

    public GriefPreventionIntegration(QuickShop plugin) {
        super(plugin);
        ConfigurationSection configurationSection = plugin.getConfig();
        this.whiteList = configurationSection.getBoolean("integration.griefprevention.whitelist-mode");
        this.deleteOnClaimUntrusted = configurationSection.getBoolean("integration.griefprevention.delete-on-claim-untrusted");
        this.deleteOnClaimUnclaimed = configurationSection.getBoolean("integration.griefprevention.delete-on-claim-unclaimed");
        this.deleteOnClaimExpired = configurationSection.getBoolean("integration.griefprevention.delete-on-claim-expired");
        this.deleteOnClaimResized = configurationSection.getBoolean("integration.griefprevention.delete-on-claim-resized");
        this.createLimits.addAll(toFlags(configurationSection.getStringList("integration.griefprevention.create")));
        this.tradeLimits.addAll(toFlags(configurationSection.getStringList("integration.griefprevention.trade")));
    }

    @Override
    public @NotNull String getName() {
        return "GriefPrevention";
    }

    @Override
    public void load() {
        registerListener();
    }

    @Override
    public void unload() {
        unregisterListener();
    }

    @Override
    public boolean canCreateShopHere(@NotNull Player player, @NotNull Location location) {
        return checkPermission(player, location, createLimits);
    }

    @Override
    public boolean canTradeShopHere(@NotNull Player player, @NotNull Location location) {
        return checkPermission(player, location, tradeLimits);
    }

    // When player changes trust, we will check if the shops there will be deleted or not.
    // We will not delete the shops of the claim owner.
    // The shops will be deleted if the shop owner doesn't have anymore permission to create a shop there.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClaimUntrusted(TrustChangedEvent event) {
        if (!deleteOnClaimUntrusted) {
            return;
        }
        for (Claim claim : event.getClaims()) {
            for (Chunk chunk : claim.getChunks()) {
                Map<Location, Shop> shops = plugin.getShopManager().getShops(chunk);
                if (shops != null) {
                    for (Shop shop : shops.values()) {
                        if (!shop.getOwner().equals(claim.getOwnerID())) {
                            Claim shopClaim = griefPrevention.dataStore.getClaimAt(shop.getLocation(), false, false, null);
                            if (shopClaim != null &&
                                    shopClaim.getID().equals(claim.getID()) &&
                                    !hasPermissionClaim(shopClaim, Bukkit.getPlayer(shop.getOwner()), createLimits)) {
                                plugin.log("[SHOP DELETE] GP Integration: Single delete (Untrusted) #" + shop.getOwner());
                                shop.delete();
                            }
                        }
                    }
                }
            }
        }
    }

    // Player can unclaim the main claim or the subclaim.
    // So we need to call either the handleMainClaimUnclaimedOrExpired or the handleSubClaimUnclaimed method.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClaimUnclaimed(ClaimDeletedEvent event) {
        if (!deleteOnClaimUnclaimed) {
            return;
        }
        if (event.getClaim().parent == null) {
            handleMainClaimUnclaimedOrExpired(event.getClaim(), "[SHOP DELETE] GP Integration: Single delete (Claim Unclaimed) #");
        } else {
            handleSubClaimUnclaimed(event.getClaim(), event.getClaim().parent);
        }
    }

    // Since only the main claim expires, we will call the handleMainClaimUnclaimedOrExpired method.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClaimExpired(ClaimExpirationEvent event) {
        if (!deleteOnClaimExpired) {
            return;
        }
        handleMainClaimUnclaimedOrExpired(event.getClaim(),"[SHOP DELETE] GP Integration: Single delete (Claim Expired) #");
    }

    // Player can resize the main claim or the subclaim.
    // So we need to call either the handleMainClaimResized or the handleSubClaimResized method.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClaimResized(ClaimModifiedEvent event) {
        if (!deleteOnClaimResized) {
            return;
        }
        Claim oldClaim = event.getFrom();
        Claim newClaim = event.getTo();
        if (oldClaim.parent == null) {
            handleMainClaimResized(oldClaim, newClaim);
        } else {
            handleSubClaimResized(oldClaim, newClaim, oldClaim.parent);
        }
    }

    // If it is the main claim, then we will delete all shops that were inside of it.
    private void handleMainClaimUnclaimedOrExpired(Claim claim, String logMessage) {
        for (Chunk chunk : claim.getChunks()) {
            Map<Location, Shop> shops = plugin.getShopManager().getShops(chunk);
            if (shops != null) {
                for (Shop shop : shops.values()) {
                    if (claim.contains(shop.getLocation(), false, false)) {
                        plugin.log(logMessage + shop.getOwner());
                        shop.delete();
                    }
                }
            }
        }
    }

    // If it is a subclaim, then we will not remove the shops of the main claim owner.
    // Also, we will not remove the shops of the players that have permission to build shops in the main claim.
    private void handleSubClaimUnclaimed(Claim subClaim, Claim mainClaim) {
        for (Chunk chunk : subClaim.getChunks()) {
            Map<Location, Shop> shops = plugin.getShopManager().getShops(chunk);
            if (shops != null) {
                for (Shop shop : shops.values()) {
                    if (subClaim.contains(shop.getLocation(), false, false)) {
                        if (!shop.getOwner().equals(mainClaim.getOwnerID()) &&
                                !hasPermissionClaim(mainClaim, Bukkit.getPlayer(shop.getOwner()), createLimits)) {
                            plugin.log("[SHOP DELETE] GP Integration: Single delete (SubClaim Unclaimed) #" + shop.getOwner());
                            shop.delete();
                        }
                    }
                }
            }
        }
    }

    // If it is a main claim, then we will remove the shop if the main claim was resized (size was decreased).
    // The shop will be removed if the old claim contains the shop but the new claim doesn't have it.
    private void handleMainClaimResized(Claim oldClaim, Claim newClaim) {
        for (Chunk chunk : oldClaim.getChunks()) {
            Map<Location, Shop> shops = plugin.getShopManager().getShops(chunk);
            if (shops != null) {
                for (Shop shop : shops.values()) {
                    if (oldClaim.contains(shop.getLocation(), false, false) &&
                            !newClaim.contains(shop.getLocation(), false, false)) {
                        plugin.log("[SHOP DELETE] GP Integration: Single delete (Claim Resized) #" + shop.getOwner());
                        shop.delete();
                    }
                }
            }
        }
    }

    // If it is a subclaim, then we will remove the shops in 2 situations.
    // We will never remove shops of the claim owner.
    // We will remove the shop if the shop is now outside the subclaim and the shop owner doesn't have permission in the main claim.
    // We will remove the shop if the shop is now inside the subclaim and the shop owner doesn't have permission in the subclaim.
    private void handleSubClaimResized(Claim oldClaim, Claim newClaim, Claim mainClaim) {
        handleSubClaimResizedHelper(oldClaim, newClaim, mainClaim);
        handleSubClaimResizedHelper(newClaim, oldClaim, newClaim);
    }

    private void handleSubClaimResizedHelper(Claim claimVerifyChunks, Claim claimVerifyShop, Claim claimPermissionCheck) {
        for (Chunk chunk : claimVerifyChunks.getChunks()) {
            Map<Location, Shop> shops = plugin.getShopManager().getShops(chunk);
            if (shops != null) {
                for (Shop shop : shops.values()) {
                    if (!shop.getOwner().equals(claimVerifyChunks.getOwnerID())) {
                        if (claimVerifyChunks.contains(shop.getLocation(), false, false) &&
                                !claimVerifyShop.contains(shop.getLocation(), false, false) &&
                                !hasPermissionClaim(claimPermissionCheck, Bukkit.getPlayer(shop.getOwner()), createLimits)) {
                            plugin.log("[SHOP DELETE] GP Integration: Single delete (SubClaim Resized) #" + shop.getOwner());
                            shop.delete();
                        }
                    }
                }
            }
        }
    }

    private boolean checkPermission(@NotNull Player player, @NotNull Location location, List<Flag> limits) {
        if (!griefPrevention.claimsEnabledForWorld(location.getWorld())) {
            return true;
        }
        Claim claim = griefPrevention.dataStore.getClaimAt(location, false, griefPrevention.dataStore.getPlayerData(player.getUniqueId()).lastClaim);
        if (claim == null) {
            return !whiteList;
        }
        return hasPermissionClaim(claim, player, limits);
    }

    private boolean hasPermissionClaim(@NotNull Claim claim, Player player, List<Flag> limits) {
        for (Flag flag : limits) {
            if (!flag.check(claim, player)) {
                return false;
            }
        }
        return true;
    }

    private List<Flag> toFlags(List<String> flags) {
        List<Flag> result = new ArrayList<>(3);
        for (String flagStr : flags) {
            Flag flag = Flag.getFlag(flagStr);
            if (flag != null) {
                result.add(flag);
            }
        }
        return result;
    }

    enum Flag {

        BUILD {
            @Override
            boolean check(Claim claim, Player player) {
                return claim.allowBuild(player, Material.CHEST) == null;
            }
        }, CONTAINER_ACCESS {
            @Override
            boolean check(Claim claim, Player player) {
                return claim.allowContainers(player) == null;
            }
        }, ACCESS {
            @Override
            boolean check(Claim claim, Player player) {
                return claim.allowAccess(player) == null;
            }
        };

        public static Flag getFlag(String flag) {
            flag = flag.toUpperCase();
            for (Flag value : Flag.values()) {
                if (value.name().equals(flag)) {
                    return value;
                }
            }
            return null;
        }

        abstract boolean check(Claim claim, Player player);
    }

}
