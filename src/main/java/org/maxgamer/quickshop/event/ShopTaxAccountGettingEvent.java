package org.maxgamer.quickshop.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.shop.Shop;

import java.util.UUID;
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class ShopTaxAccountGettingEvent extends AbstractQSEvent{
    @Nullable
    private UUID taxAccount;
    private final Shop shop;
}
