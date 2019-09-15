package org.maxgamer.quickshop.Permission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class ProviderIsEmptyException extends RuntimeException {
    @Getter
    @Setter
    private String providerName;
}
