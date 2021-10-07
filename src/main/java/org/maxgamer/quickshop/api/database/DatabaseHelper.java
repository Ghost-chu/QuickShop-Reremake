package org.maxgamer.quickshop.api.database;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.database.DataType;
import org.maxgamer.quickshop.database.WarpedResultSet;

import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Consumer;

public interface DatabaseHelper {
    boolean createColumn(@NotNull String tableName, @NotNull String columnName, @NotNull DataType type);

    void cleanMessage(long weekAgo);

    void cleanMessageForPlayer(@NotNull UUID player);

    void createShop(@NotNull Shop shop, @Nullable Runnable onSuccess, @Nullable Consumer<SQLException> onFailed);

    void removeShop(Shop shop);

    void removeShop(String world, int x, int y, int z);

    WarpedResultSet selectAllMessages() throws SQLException;

    WarpedResultSet selectTable(String table) throws SQLException;

    WarpedResultSet selectAllShops() throws SQLException;

    void sendMessage(@NotNull UUID player, @NotNull String message, long time);

    void updateOwner2UUID(@NotNull String ownerUUID, int x, int y, int z, @NotNull String worldName);

    void updateExternalInventoryProfileCache(@NotNull Shop shop, int space, int stock);

    void updateShop(@NotNull String owner, @NotNull ItemStack item, int unlimited, int shopType,
                    double price, int x, int y, int z, @NotNull String world, @NotNull String extra,
                    @NotNull String currency, boolean disableDisplay, @Nullable String taxAccount);

    void insertHistoryRecord(Object record);

}
