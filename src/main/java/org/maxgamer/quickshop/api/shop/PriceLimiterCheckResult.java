package org.maxgamer.quickshop.api.shop;

/**
 * Result of PriceLimiter check
 */
public interface PriceLimiterCheckResult {
    /**
     * Getting final result
     * @return Result
     */
    PriceLimiterStatus getStatus();

    /**
     * Getting this type of item min allowed price is
     * @return Min price
     */
    double getMin();
    /**
     * Getting this type of item max allowed price is
     * @return Max price
     */
    double getMax();
}
