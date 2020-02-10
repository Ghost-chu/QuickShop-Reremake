package org.maxgamer.quickshop.database;

import com.zaxxer.hikari.HikariConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class DatabaseCredentials {

    private final String address;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public DatabaseCredentials(@NotNull String address, int port, @NotNull String database, @NotNull String username,
                               @NotNull String password) {
        this.address = Objects.requireNonNull(address);
        this.port = port;
        this.database = Objects.requireNonNull(database);
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
    }

    public void setup(@NotNull HikariConfig hikariConfig) {
        hikariConfig.addDataSourceProperty("serverName", address);
        hikariConfig.addDataSourceProperty("port", port);
        hikariConfig.addDataSourceProperty("databaseName", database);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
    }

}