package org.maxgamer.quickshop.api;

import org.maxgamer.quickshop.api.command.CommandManager;
import org.maxgamer.quickshop.api.compatibility.CompatibilityManager;
import org.maxgamer.quickshop.api.database.DatabaseHelper;
import org.maxgamer.quickshop.api.localization.text.TextManager;
import org.maxgamer.quickshop.api.shop.ItemMatcher;
import org.maxgamer.quickshop.api.shop.ShopManager;

public interface QuickShopAPI {

    CompatibilityManager getCompatibilityManager();

    ShopManager getShopManager();

    boolean isAllowStack();

    boolean isDisplayEnabled();

    boolean isLimit();

    DatabaseHelper getDatabaseHelper();

    TextManager getTextManager();

    ItemMatcher getItemMatcher();

    boolean isPriceChangeRequiresFee();

    CommandManager getCommandManager();

}
