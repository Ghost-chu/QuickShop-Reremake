package org.maxgamer.quickshop.api;

import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Shop;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class QuickShopAPI {

    private QuickShopAPI() {
    }

    @NotNull
    public Optional<Shop> getByUUID(@NotNull UUID uuid) {
        return Optional.empty();
    }

    @NotNull
    public List<Shop> getShops() {
        return Collections.emptyList();
    }

}
