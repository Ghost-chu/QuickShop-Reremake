/*
 * This file is a part of project QuickShop, the name is ShopModerator.java
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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Contains shop's moderators infomations, owner, staffs etc.
 */
@EqualsAndHashCode
public class ShopModerator implements Cloneable {
    @NonNull
    private UUID owner;

    @NonNull
    private List<UUID> staffs;

    private ShopModerator(@NotNull ShopModerator shopModerator) {
        this.owner = shopModerator.owner;
        this.staffs = shopModerator.staffs;
    }

    /**
     * Shop moderators, inlucding owner, and empty staffs.
     *
     * @param owner The owner
     */
    public ShopModerator(@NotNull UUID owner) {
        this.owner = owner;
        this.staffs = new ArrayList<>();
    }

    /**
     * Shop moderators, inlucding owner, staffs.
     *
     * @param owner  The owner
     * @param staffs The staffs
     */
    public ShopModerator(@NotNull UUID owner, @NotNull List<UUID> staffs) {
        this.owner = owner;
        this.staffs = staffs;
    }

    public static ShopModerator deserialize(@NotNull String serilized) throws JsonSyntaxException {
        // Use Gson deserialize data
        Gson gson = JsonUtil.getGson();
        return gson.fromJson(serilized, ShopModerator.class);
    }

    public static String serialize(@NotNull ShopModerator shopModerator) {
        Gson gson = JsonUtil.getGson();
        return gson.toJson(shopModerator); // Use Gson serialize this class
    }

    /**
     * Add moderators staff to staff list
     *
     * @param player New staff
     * @return Success
     */
    public boolean addStaff(@NotNull UUID player) {
        if (staffs.contains(player)) {
            return false;
        }
        staffs.add(player);
        return true;
    }

    /**
     * Remove all staffs
     */
    public void clearStaffs() {
        staffs.clear();
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public @NotNull ShopModerator clone() {
        return new ShopModerator(this.owner, this.staffs);
    }

    @Override
    public @NotNull String toString() {
        return serialize(this);
    }

    /**
     * Remove moderators staff from staff list
     *
     * @param player Staff
     * @return Success
     */
    public boolean delStaff(@NotNull UUID player) {
        return staffs.remove(player);
    }

    /**
     * Get a player is or not moderators
     *
     * @param player Player
     * @return yes or no, return true when it is staff or owner
     */
    public boolean isModerator(@NotNull UUID player) {
        return isOwner(player) || isStaff(player);
    }

    /**
     * Get a player is or not moderators owner
     *
     * @param player Player
     * @return yes or no
     */
    public boolean isOwner(@NotNull UUID player) {
        return player.equals(owner);
    }

    /**
     * Get a player is or not moderators a staff
     *
     * @param player Player
     * @return yes or no
     */
    public boolean isStaff(@NotNull UUID player) {
        return staffs.contains(player);
    }

    /**
     * Get moderators owner (Shop Owner).
     *
     * @return Owner's UUID
     */
    public @NotNull UUID getOwner() {
        return owner;
    }

    /**
     * Set moderators owner (Shop Owner)
     *
     * @param player Owner's UUID
     */
    public void setOwner(@NotNull UUID player) {
        this.owner = player;
    }

    /**
     * Get staffs list
     *
     * @return Staffs
     */
    public @NotNull List<UUID> getStaffs() {
        return staffs;
    }

    /**
     * Set moderators staffs
     *
     * @param players staffs list
     */
    public void setStaffs(@NotNull List<UUID> players) {
        this.staffs = players;
    }

}
