package org.maxgamer.quickshop.Listeners;

import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Cache;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;

@Getter
public class ProtectionListenerBase {
    private QuickShop plugin;

    private Cache cache;

    public ProtectionListenerBase(@NotNull QuickShop plugin, @Nullable Cache cache) {
        this.cache = cache;
        this.plugin = plugin;
    }

    /**
     * Get shop for redstone events, will caching if caching enabled
     *
     * @param location The block location
     * @param includeAttached whether to include the attached shop
     * @return The shop object
     */
    @SuppressWarnings("SpellCheckingInspection")
    @Nullable
    public Shop getShopRedstone(@NotNull Location location, boolean includeAttached) {
        if (cache == null) {
            if (includeAttached) {
                return plugin.getShopManager().getShopIncludeAttached(location);
            } else {
                return plugin.getShopManager().getShop(location);
            }
        }
        return cache.getCaching(location, includeAttached);
    }

    /**
     * Get shop for player events, won't be caching
     *
     * @param location The block location
     * @param includeAttached whether to include the attached shop
     *
     * @return The shop object
     */
    @Nullable
    public Shop getShopPlayer(@NotNull Location location, boolean includeAttached) {
        return includeAttached ? plugin.getShopManager().getShopIncludeAttached(location,false):plugin.getShopManager().getShop(location);
    }

    /**
     * Get shop for nature events, may will caching but usually it doesn't will cached.
     * Because nature events usually won't check same block twice in shore time.
     *
     * @param location The block location
     * @param includeAttached whether to include the attached shop
     * @return The shop object
     */
    @Nullable
    public Shop getShopNature(@NotNull Location location, boolean includeAttached) {
        return includeAttached ? plugin.getShopManager().getShopIncludeAttached(location,false):plugin.getShopManager().getShop(location);
    }

}
