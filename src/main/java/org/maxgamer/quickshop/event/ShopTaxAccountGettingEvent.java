package org.maxgamer.quickshop.event;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.api.shop.Shop;

import java.util.UUID;
@AllArgsConstructor
public class ShopTaxAccountGettingEvent extends AbstractQSEvent{
    @Nullable
    private UUID taxAccount;
    private final Shop shop;

    /**
     * Getting the tax account
     * @return The tax account, null if tax has been disabled
     */
    @Nullable
    public UUID getTaxAccount() {
        return taxAccount;
    }

    /**
     * Sets the tax account
     * @param taxAccount The tax account
     */
    public void setTaxAccount(@Nullable UUID taxAccount) {
        this.taxAccount = taxAccount;
    }

    /**
     * Gets the shop
     * @return The shop
     */
    public Shop getShop() {
        return shop;
    }
}
