package org.maxgamer.quickshop.shop;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.maxgamer.quickshop.api.shop.PriceLimiterCheckResult;
import org.maxgamer.quickshop.api.shop.PriceLimiterStatus;
@AllArgsConstructor
@Data
public class JavaPriceLimiterCheckResult implements PriceLimiterCheckResult {
    PriceLimiterStatus status;
    double min;
    double max;
}
