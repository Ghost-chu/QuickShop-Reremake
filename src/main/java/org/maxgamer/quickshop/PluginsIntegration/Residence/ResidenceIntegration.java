/*
 * This file is a part of project QuickShop, the name is ResidenceIntegration.java
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

package org.maxgamer.quickshop.PluginsIntegration.Residence;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.PluginsIntegration.IntegratedPlugin;
import org.maxgamer.quickshop.QuickShop;

import java.util.List;

@SuppressWarnings("DuplicatedCode")
public class ResidenceIntegration implements IntegratedPlugin {
    List<String> createLimits;
    List<String> tradeLimits;

    public ResidenceIntegration(QuickShop plugin){
        this.createLimits = plugin.getConfig().getStringList("integration.residence.create");
        this.tradeLimits = plugin.getConfig().getStringList("integration.residence.trade");
    }
    @Override
    public @NotNull String getName() {
        return "Residence";
    }

    @Override
    public boolean canCreateShopHere(@NotNull Player player, @NotNull Location location) {
        ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByLoc(location);


        for (String limit:this.createLimits) {
            if("FLAG".equalsIgnoreCase(limit)){
                if(residence == null){
                    //Check world permission
                    if(!Residence.getInstance().getWorldFlags().getPerms(location.getWorld().getName()).playerHas(player, Flags.getFlag("quickshop.create"), false)){
                        return false;
                    }
                }else{
                    if(!residence.getPermissions().playerHas(player,Flags.getFlag("quickshop.create"),false)){
                        return false;
                    }
                }

            }
            //Not flag
            if(residence == null){
                if(!Residence.getInstance().getWorldFlags().getPerms(location.getWorld().getName()).playerHas(player, Flags.getFlag(limit), false)){
                    return false;
                }
            }else{
                if(!residence.getPermissions().playerHas(player,Flags.getFlag(limit),false)){
                    return false;
                }
            }

        }
        return false;
    }

    @Override
    public boolean canTradeShopHere(@NotNull Player player, @NotNull Location location) {
        ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByLoc(location);


        for (String limit:this.createLimits) {
            if("FLAG".equalsIgnoreCase(limit)){
                if(residence == null){
                    //Check world permission
                    if(!Residence.getInstance().getWorldFlags().getPerms(location.getWorld().getName()).playerHas(player, Flags.getFlag("quickshop.trade"), false)){
                        return false;
                    }
                }else{
                    if(!residence.getPermissions().playerHas(player,Flags.getFlag("quickshop.trade"),true)){
                        return false;
                    }
                }

            }
            //Not flag
            if(residence == null){
                if(!Residence.getInstance().getWorldFlags().getPerms(location.getWorld().getName()).playerHas(player, Flags.getFlag(limit), false)){
                    return false;
                }
            }else{
                if(!residence.getPermissions().playerHas(player,Flags.getFlag(limit),false)){
                    return false;
                }
            }

        }
        return false;
    }

    @Override
    public void load() {
        FlagPermissions.addFlag("quickshop.create");
        FlagPermissions.addFlag("quickshop.trade");
    }

    @Override
    public void unload() {

    }
}
