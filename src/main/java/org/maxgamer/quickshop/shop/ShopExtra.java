package org.maxgamer.quickshop.shop;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@AllArgsConstructor
@Data
public class ShopExtra {
    private @NotNull String namespace;
    private @NotNull Map<String, String> data;
}
