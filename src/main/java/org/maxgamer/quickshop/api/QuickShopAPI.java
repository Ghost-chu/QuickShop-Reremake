package org.maxgamer.quickshop.api;

import org.maxgamer.quickshop.api.compatibility.CompatibilityManager;
import org.maxgamer.quickshop.api.database.DatabaseHelper;
import org.maxgamer.quickshop.api.shop.ShopManager;

public interface QuickShopAPI {

    CompatibilityManager getCompatibilityManager();

    ShopManager getShopManager();

    boolean isAllowStack();

    boolean isDisplayEnabled();

    boolean isLimit();

    DatabaseHelper getDatabaseHelper();


    default void a(){

    }

}
