package org.maxgamer.quickshop.util.logging.container;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;
import org.maxgamer.quickshop.api.shop.ShopInfoStorage;

import java.util.UUID;

@AllArgsConstructor
@Data
public class ShopCreationLog {
    private UUID creator;
    private ShopInfoStorage shop;
    private Location location;
}
