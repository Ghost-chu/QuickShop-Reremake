package org.maxgamer.quickshop.api;

import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Shop;

import java.util.Optional;
import java.util.UUID;

public final class QuickShopAPI {

    private QuickShopAPI() {
    }

    @NotNull
    public Optional<Shop> getByUUID(@NotNull UUID uuid) {
        return Optional.empty();
    }

}
