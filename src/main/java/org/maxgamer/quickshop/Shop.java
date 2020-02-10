package org.maxgamer.quickshop;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface Shop {

    /**
     * Compares if given uuid is owner of the shop or not.
     * 
     * @param uuid the uuid that will compare
     * @return returns true if the uuid is owner of the shop.
     */
    boolean isOwner(@NotNull UUID uuid);

}
