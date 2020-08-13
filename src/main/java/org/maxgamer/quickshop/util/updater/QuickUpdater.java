package org.maxgamer.quickshop.util.updater;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface QuickUpdater {
    @NotNull VersionType getCurrentRunning();

    @NotNull String getRemoteServerVersion();

    int getRemoteServerBuildId();

    boolean isLatest(@NotNull VersionType versionType);

    @NotNull byte[] update(@NotNull VersionType versionType) throws IOException;

    void install(byte[] bytes) throws IOException;
}
