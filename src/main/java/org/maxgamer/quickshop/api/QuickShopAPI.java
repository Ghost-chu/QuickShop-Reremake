package org.maxgamer.quickshop.api;

import org.maxgamer.quickshop.api.command.CommandManager;
import org.maxgamer.quickshop.api.compatibility.CompatibilityManager;
import org.maxgamer.quickshop.api.database.DatabaseHelper;
import org.maxgamer.quickshop.api.localization.text.TextManager;
import org.maxgamer.quickshop.api.shop.ItemMatcher;
import org.maxgamer.quickshop.api.shop.ShopManager;
import org.maxgamer.quickshop.util.GameVersion;

import java.util.Map;

/**
 * The unique entry point to allow you to access most features of QuickShop
 */
public interface QuickShopAPI {
    /**
     * Getting Compatibility Manager (usually used for anti-chest)
     * @return Compatibility Manager
     */
    CompatibilityManager getCompatibilityManager();

    /**
     * Getting Shop Manager which managing most of shops
     * @return Shop manager
     */
    ShopManager getShopManager();

    /**
     * Getting QuickShop current stacking item support status
     * @return Stacking Item support enabled
     */
    boolean isAllowStack();

    /**
     * Getting QuickShop current display item support status
     * @return Display item enabled
     */
    boolean isDisplayEnabled();
    /**
     * Getting QuickShop current permission based shop amount limit status
     * @return Limit enabled
     */
    boolean isLimit();
    /**
     * Getting QuickShop current permission based shop amount limits
     * @return Permissions 2 Shop Amounts mapping
     */
    Map<String, Integer> getLimits();

    /**
     * Getting the helper to directly access the database
     * @return The database helper
     */
    DatabaseHelper getDatabaseHelper();

    /**
     * Getting text manager that allow addon to create a user language locale based message
     * @return The text maanger
     */
    TextManager getTextManager();

    /**
     * Getting current using ItemMatcher impl
     * @return The item matcher
     */
    ItemMatcher getItemMatcher();

    /**
     * Getting the status that fee requires if user performing price change
     * @return requires fee
     */
    boolean isPriceChangeRequiresFee();

    /**
     * Getting command manager that allow addon direct access QuickShop sub-command system
     * @return The command manager
     */
    CommandManager getCommandManager();

    /**
     * Getting this server game version
     * @return Game version
     */
    GameVersion getGameVersion();

}
