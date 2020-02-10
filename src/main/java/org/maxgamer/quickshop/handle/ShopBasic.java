package org.maxgamer.quickshop.handle;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Shop;

import java.util.UUID;

public final class ShopBasic implements Shop {

    @NotNull
    private final UUID uuid;

    @NotNull
    private final Location location;

    public ShopBasic(@NotNull UUID uuid, @NotNull Location location) {
        this.uuid = uuid;
        this.location = location;
    }

}
