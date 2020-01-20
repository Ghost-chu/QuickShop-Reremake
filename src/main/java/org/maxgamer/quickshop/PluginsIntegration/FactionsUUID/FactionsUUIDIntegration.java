/*
 * This file is a part of project QuickShop, the name is FactionsUUIDIntegration.java
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

package org.maxgamer.quickshop.PluginsIntegration.FactionsUUID;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.perms.PermissibleAction;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.PluginsIntegration.IntegratedPlugin;
import org.maxgamer.quickshop.QuickShop;

import java.util.List;

public class FactionsUUIDIntegration implements IntegratedPlugin {
    private List<String> createFlags;
    private List<String> tradeFlags;
    private boolean createRequireOpen;
    private boolean createRequireNormal;
    private boolean createRequireWilderness;
    private boolean createRequirePeaceful;
    private boolean createRequirePermanent;
    private boolean createRequireSafeZone;
    private boolean createRequireOwn;
    private boolean createRequireWarZone;
    private boolean tradeRequireOpen;
    private boolean tradeRequireNormal;
    private boolean tradeRequireWilderness;
    private boolean tradeRequirePeaceful;
    private boolean tradeRequirePermanent;
    private boolean tradeRequireSafeZone;
    private boolean tradeRequireOwn;
    private boolean tradeRequireWarZone;

    public FactionsUUIDIntegration(QuickShop plugin) {
        this.createFlags = plugin.getConfig().getStringList("integration.factions.create.flags");
        this.tradeFlags = plugin.getConfig().getStringList("integration.factions.trade.flags");

        this.createRequireOpen = plugin.getConfig().getBoolean("integration.factions.create.require.open");
        this.createRequireNormal = plugin.getConfig().getBoolean("integration.factions.create.require.normal");
        this.createRequireWilderness = plugin.getConfig().getBoolean("integration.factions.create.require.wilderness");
        this.createRequirePeaceful = plugin.getConfig().getBoolean("integration.factions.create.require.peaceful");
        this.createRequirePermanent = plugin.getConfig().getBoolean("integration.factions.create.require.permanent");
        this.createRequireSafeZone = plugin.getConfig().getBoolean("integration.factions.create.require.safezone");
        this.createRequireOwn = plugin.getConfig().getBoolean("integration.factions.create.require.own");
        this.createRequireWarZone = plugin.getConfig().getBoolean("integration.factions.create.require.warzone");

        this.tradeRequireOpen = plugin.getConfig().getBoolean("integration.factions.trade.require.open");
        this.tradeRequireNormal = plugin.getConfig().getBoolean("integration.factions.trade.require.normal");
        this.tradeRequireWilderness = plugin.getConfig().getBoolean("integration.factions.trade.require.wilderness");
        this.tradeRequirePeaceful = plugin.getConfig().getBoolean("integration.factions.trade.require.peaceful");
        this.tradeRequirePermanent = plugin.getConfig().getBoolean("integration.factions.trade.require.permanent");
        this.tradeRequireSafeZone = plugin.getConfig().getBoolean("integration.factions.trade.require.safezone");
        this.tradeRequireOwn = plugin.getConfig().getBoolean("integration.factions.trade.require.own");
        this.tradeRequireWarZone = plugin.getConfig().getBoolean("integration.factions.trade.require.warzone");

    }

    @Override
    public @NotNull String getName() {
        return "Factions";
    }

    @Override
    public boolean canCreateShopHere(@NotNull Player player, @NotNull Location location) {
        Faction faction = Board.getInstance().getFactionAt(new FLocation(location));
        if (faction == null) {
            return true;
        }
        if (createRequireOpen && !faction.getOpen()) {
            return false;
        }
        if (createRequireSafeZone && !faction.isSafeZone()) {
            return false;
        }
        if (createRequirePermanent && !faction.isPermanent()) {
            return false;
        }
        if (createRequirePeaceful && !faction.isPeaceful()) {
            return false;
        }
        if (createRequireWilderness && !faction.isWilderness()) {
            return false;
        }
        if (createRequireOpen && !faction.getOpen()) {
            return false;
        }
        if (createRequireWarZone && !faction.isWarZone()) {
            return false;
        }
        if (createRequireNormal && !faction.isNormal()) {
            return false;
        }
        if (createRequireOwn && !faction.getOwnerList(new FLocation(location)).contains(player.getName())) {
            return false;
        }
        for (String flag :
                createFlags) {
            if (!faction.hasAccess(FPlayers.getInstance().getByPlayer(player), PermissibleAction.fromString(flag))) {
                return false;
            }

        }
        return true;
    }

    @Override
    public boolean canTradeShopHere(@NotNull Player player, @NotNull Location location) {

        Faction faction = Board.getInstance().getFactionAt(new FLocation(location));
        if (faction == null) {
            return true;
        }
        if (tradeRequireOpen && !faction.getOpen()) {
            return false;
        }
        if (tradeRequireSafeZone && !faction.isSafeZone()) {
            return false;
        }
        if (tradeRequirePermanent && !faction.isPermanent()) {
            return false;
        }
        if (tradeRequirePeaceful && !faction.isPeaceful()) {
            return false;
        }
        if (tradeRequireWilderness && !faction.isWilderness()) {
            return false;
        }
        if (tradeRequireOpen && !faction.getOpen()) {
            return false;
        }
        if (tradeRequireWarZone && !faction.isWarZone()) {
            return false;
        }
        if (tradeRequireNormal && !faction.isNormal()) {
            return false;
        }
        if (tradeRequireOwn && !faction.getOwnerList(new FLocation(location)).contains(player.getName())) {
            return false;
        }
        for (String flag :
                tradeFlags) {
            if (!faction.hasAccess(FPlayers.getInstance().getByPlayer(player), PermissibleAction.fromString(flag))) {
                return false;
            }

        }
        return true;
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }
}