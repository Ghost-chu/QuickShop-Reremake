package org.maxgamer.quickshop.api.shop;

public interface PriceLimiterCheckResult {
    PriceLimiterStatus getStatus();

    double getMin();

    double getMax();
}
