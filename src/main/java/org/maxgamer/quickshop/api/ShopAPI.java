package org.maxgamer.quickshop.api;

import lombok.AllArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.shop.Shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
public class ShopAPI {
    private QuickShop plugin;

    /**
     * Gets shop from location
     * @param location The location
     * @return The shop in that location, may be null
     */
    public @Nullable Shop getShop(@NotNull Location location){
        return plugin.getShopManager().getShop(location,false);
    }
    /**
     * Gets shop from location with caching
     * @param location The location
     * @return The shop in that location, may be null
     */
    public @Nullable Shop getShopWithCaching(@NotNull Location location){
        if(plugin.getShopCache() == null){
            return getShop(location);
        }
        return plugin.getShopCache().getCaching(location,false);
    }

    /**
     * Gets shops in chunk
     * @param chunk The chunk
     * @return The shops in chunk, may be null
     */
    public @Nullable Map<Location, Shop> getShop(@NotNull Chunk chunk){
        return plugin.getShopManager().getShops(chunk);
    }

    /**
     * Gets shop from location
     * @param location The location
     * @return The shop in that location, may be null
     */
    public @Nullable Shop getShopIncludeAttached(@NotNull Location location){
        return plugin.getShopManager().getShopIncludeAttached(location);
    }
    /**
     * Gets shop from location and use caching
     * @param location The location
     * @return The shop in that location, may be null
     */
    public @Nullable Shop getShopIncludeAttachedWithCaching(@NotNull Location location){
        if(plugin.getShopCache() == null){
            return getShopIncludeAttached(location);
        }
        return plugin.getShopCache().getCaching(location,true);
    }

    /**
     * Gets a list of copy for loaded shops
     * @return The copy of loaded shops
     */
    public @NotNull List<Shop> getLoadedShops(){
        return new ArrayList<>(plugin.getShopManager().getLoadedShops());
    }

    /**
     * Gets a list of copy a player's all shops
     * This is a expensive action, please caching the result
     * @param uuid Player UUID
     * @return The list of player's all shops
     */
    public @NotNull List<Shop> getPlayerAllShops(@NotNull UUID uuid){
        return new ArrayList<>(plugin.getShopManager().getPlayerAllShops(uuid));
    }

    /**
     * Gets a list of copy all shops on this server
     * This is a expensive action, please caching the result
     * @return The list of all shops on server
     */
    public @NotNull List<Shop> getAllShops(){
        return new ArrayList<>( plugin.getShopManager().getAllShops());
    }
    /**
     * Gets a list of copy all shops in world
     * This is a expensive action, please caching the result
     * @param world The world that you want get shops
     * @return The list of all shops in world
     */
    public @NotNull List<Shop> getShopsInWorld(@NotNull World world){
        return new ArrayList<>(plugin.getShopManager().getShopsInWorld(world));
    }


}
